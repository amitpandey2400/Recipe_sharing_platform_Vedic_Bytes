import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class User {
    private String name;
    private String password;
    private String email;

    public User(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
}

class Recipe {
    private String title, description;

    public Recipe(String title, String description) {
        this.title = title;
        this.description = description;
    }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}

class Platform {
    private AuthService auth = new AuthService();
    private List<Recipe> recipes = new ArrayList<>();

    public Platform() {
        loadData();
    }

    public void loadData() {
        recipes.add(new Recipe("Paneer Butter Masala", "Creamy paneer dish with spices."));
        recipes.add(new Recipe("Dal Makhani", "Slow-cooked black lentils."));
        recipes.add(new Recipe("Aloo Tamatar", "Potato curry with tomato gravy."));
        recipes.add(new Recipe("Tea", "Classic Indian chai."));
    }

    public void saveData() {
        // For now, nothing to do.
    }

    public AuthService getAuthService() { return auth; }
    public List<Recipe> getAllRecipes() { return recipes; }

    public void browseRecipesGUI(JFrame parent, User user) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Recipe r : recipes)
            listModel.addElement(r.getTitle());
        JList<String> list = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(list);

        JTextArea details = new JTextArea(5, 30);
        details.setEditable(false);

        list.addListSelectionListener(e -> {
            String selected = list.getSelectedValue();
            for (Recipe r : recipes)
                if (r.getTitle().equals(selected)) {
                    details.setText(r.getDescription());
                }
        });

        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.add(new JLabel("Recipe List:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(details, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(parent, panel, "Browse Recipes", JOptionPane.PLAIN_MESSAGE);
    }

    public void searchInteractiveGUI(JFrame parent, User user) {
        JPanel searchPanel = new JPanel(new BorderLayout(5,5));
        JTextField searchField = new JTextField();
        JTextArea resultArea = new JTextArea(8, 30);
        resultArea.setEditable(false);

        JButton searchBtn = new JButton("Search");
        searchPanel.add(new JLabel("Enter keyword:"), BorderLayout.NORTH);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);
        searchPanel.add(resultArea, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(parent, "Search Recipes", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(searchPanel, BorderLayout.CENTER);
        dialog.setSize(400,250);

        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            StringBuilder sb = new StringBuilder();
            for (Recipe r : recipes) {
                if (r.getTitle().toLowerCase().contains(keyword) || r.getDescription().toLowerCase().contains(keyword)) {
                    sb.append(r.getTitle()).append(": ").append(r.getDescription()).append("\n");
                }
            }
            resultArea.setText(sb.length()>0 ? sb.toString() : "No recipes found.");
        });

        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public void userMenuGUI(JFrame parent, User user) {
        JPanel userPanel = new JPanel(new GridLayout(0,1,4,4));
        userPanel.add(new JLabel("Welcome, " + user.getName()));
        userPanel.add(new JLabel("Email: " + user.getEmail()));
        userPanel.add(new JLabel("Member options: (to add)"));
        JOptionPane.showMessageDialog(parent, userPanel, "User Menu", JOptionPane.PLAIN_MESSAGE);
    }
}

class AuthService {
    private List<User> users = new ArrayList<>();

    public void registerUser(String username, String password, String email) {
        users.add(new User(username, password, email));
    }

    public User loginUser(String username, String password) {
        for (User u : users)
            if (u.getName().equals(username) && u.getEmail() != null)
                return u;
        return null;
    }
}

public class MainGUI {
    private Platform platform;
    private AuthService auth;

    public MainGUI() {
        platform = new Platform();
        platform.loadData();
        auth = platform.getAuthService();

        JFrame frame = new JFrame("Vedic Bytes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 360);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(7, 1, 5, 7));

        JLabel welcomeLabel = new JLabel("=== Welcome to Vedic Bytes ===", JLabel.CENTER);
        menuPanel.add(welcomeLabel);

        JButton registerBtn = new JButton("Register");
        JButton loginBtn = new JButton("Login");
        JButton browseBtn = new JButton("Browse recipes (no login)");
        JButton searchBtn = new JButton("Search recipes");
        JButton addRecipeBtn = new JButton("Add Recipe"); // NEW BUTTON
        JButton exitBtn = new JButton("Exit");

        menuPanel.add(registerBtn);
        menuPanel.add(loginBtn);
        menuPanel.add(browseBtn);
        menuPanel.add(searchBtn);
        menuPanel.add(addRecipeBtn); // ADDED TO MENU
        menuPanel.add(exitBtn);

        registerBtn.addActionListener(e -> showRegisterDialog(frame));
        loginBtn.addActionListener(e -> showLoginDialog(frame));
        browseBtn.addActionListener(e -> platform.browseRecipesGUI(frame, null));
        searchBtn.addActionListener(e -> platform.searchInteractiveGUI(frame, null));
        addRecipeBtn.addActionListener(e -> showAddRecipeDialog(frame)); // ACTION FOR ADD
        exitBtn.addActionListener(e -> {
            platform.saveData();
            JOptionPane.showMessageDialog(frame, "Data saved. Bye!");
            frame.dispose();
        });

        frame.setContentPane(menuPanel);
        frame.setVisible(true);
    }

    private void showRegisterDialog(JFrame parent) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JPasswordField();
        JTextField emailField = new JTextField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(parent, panel,
            "Register", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String email = emailField.getText().trim();
            auth.registerUser(username, password, email);
            JOptionPane.showMessageDialog(parent, "Registered user: " + username);
        }
    }

    private void showLoginDialog(JFrame parent) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(parent, panel,
            "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            User user = auth.loginUser(username, password);
            if (user != null) {
                JOptionPane.showMessageDialog(parent, "Welcome, " + user.getName() + "!");
                platform.userMenuGUI(parent, user);
            } else {
                JOptionPane.showMessageDialog(parent, "Login failed.");
            }
        }
    }

    // ADD RECIPE DIALOG
    private void showAddRecipeDialog(JFrame parent) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField titleField = new JTextField();
        JTextArea descField = new JTextArea(3, 20);
        panel.add(new JLabel("Recipe Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descField));

        int result = JOptionPane.showConfirmDialog(parent, panel,
            "Add Recipe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String desc = descField.getText().trim();
            if (!title.isEmpty() && !desc.isEmpty()) {
                platform.getAllRecipes().add(new Recipe(title, desc));
                JOptionPane.showMessageDialog(parent, "Recipe added: " + title);
            } else {
                JOptionPane.showMessageDialog(parent, "All fields required.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGUI::new);
    }
}
