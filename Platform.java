package platform;

import platform.models.User;
import platform.models.Recipe;
import platform.models.Comment;
import platform.utils.IOUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Platform implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, User> users; // username -> User
    private Map<Integer, Recipe> recipes; // recipeId -> Recipe
    private int nextRecipeId = 1;

    private AuthService authService;

    public Platform() {
        users = new HashMap<>();
        recipes = new HashMap<>();
        authService = new AuthService(this);
    }

    public AuthService getAuthService() {
        return authService;
    }

    /* --------- Persistence --------- */
    public void loadData() {
        PlatformData pd = IOUtils.load();
        if (pd != null) {
            this.users = pd.users;
            this.recipes = pd.recipes;
            this.nextRecipeId = pd.nextRecipeId;
            System.out.println("Loaded data: users=" + users.size() + " recipes=" + recipes.size());
        } else {
            System.out.println("No saved data found. Starting fresh.");
        }
    }

    public void saveData() {
        PlatformData pd = new PlatformData();
        pd.users = this.users;
        pd.recipes = this.recipes;
        pd.nextRecipeId = this.nextRecipeId;
        IOUtils.save(pd);
    }

    /* --------- User operations --------- */
    public boolean usernameExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public void addUser(User user) {
        users.put(user.getUsername().toLowerCase(), user);
    }

    public User getUser(String username) {
        return users.get(username.toLowerCase());
    }

    /* --------- Recipe operations --------- */
    public Recipe addRecipe(String authorUsername, String title, String description, List<String> ingredients, List<String> steps, List<String> tags) {
        int id = nextRecipeId++;
        Recipe r = new Recipe(id, title, description, authorUsername, ingredients, steps, tags, LocalDateTime.now());
        recipes.put(id, r);
        return r;
    }

    public boolean deleteRecipe(int id, String requester) {
        Recipe r = recipes.get(id);
        if (r == null) return false;
        if (!r.getAuthorUsername().equalsIgnoreCase(requester)) return false;
        recipes.remove(id);
        return true;
    }

    public boolean editRecipe(int id, String requester, String newTitle, String newDescription, List<String> newIngredients, List<String> newSteps, List<String> newTags) {
        Recipe r = recipes.get(id);
        if (r == null) return false;
        if (!r.getAuthorUsername().equalsIgnoreCase(requester)) return false;
        r.setTitle(newTitle);
        r.setDescription(newDescription);
        r.setIngredients(newIngredients);
        r.setSteps(newSteps);
        r.setTags(newTags);
        r.setLastUpdated(LocalDateTime.now());
        return true;
    }

    public List<Recipe> listAllRecipes() {
        return new ArrayList<>(recipes.values()).stream()
                .sorted(Comparator.comparing(Recipe::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Recipe getRecipeById(int id) {
        return recipes.get(id);
    }

    public List<Recipe> searchByTitle(String q) {
        String ql = q.toLowerCase();
        return recipes.values().stream()
                .filter(r -> r.getTitle().toLowerCase().contains(ql))
                .sorted(Comparator.comparing(Recipe::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Recipe> searchByIngredient(String ingredient) {
        String ql = ingredient.toLowerCase();
        return recipes.values().stream()
                .filter(r -> r.getIngredients().stream().anyMatch(ing -> ing.toLowerCase().contains(ql)))
                .sorted(Comparator.comparing(Recipe::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Recipe> searchByTag(String tag) {
        String ql = tag.toLowerCase();
        return recipes.values().stream()
                .filter(r -> r.getTags().stream().anyMatch(t -> t.toLowerCase().equals(ql)))
                .sorted(Comparator.comparing(Recipe::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public boolean likeRecipe(int id, String username) {
        Recipe r = recipes.get(id);
        if (r == null) return false;
        return r.toggleLike(username);
    }

    public boolean commentRecipe(int id, String username, String text) {
        Recipe r = recipes.get(id);
        if (r == null) return false;
        Comment c = new Comment(username, text, LocalDateTime.now());
        r.getComments().add(c);
        return true;
    }

    public void userMenu(java.util.Scanner sc, User user) {
        while (true) {
            System.out.println("\nUser Menu - Logged in as: " + user.getUsername());
            System.out.println("1. Add recipe");
            System.out.println("2. My recipes");
            System.out.println("3. Browse all recipes");
            System.out.println("4. Search recipes");
            System.out.println("5. Follow user");
            System.out.println("6. Logout");
            System.out.print("Choose: ");
            String opt = sc.nextLine().trim();
            switch (opt) {
                case "1":
                    addRecipeInteractive(sc, user);
                    break;
                case "2":
                    myRecipesMenu(sc, user);
                    break;
                case "3":
                    browseRecipes(sc, user);
                    break;
                case "4":
                    searchInteractive(sc, user);
                    break;
                case "5":
                    followUserInteractive(sc, user);
                    break;
                case "6":
                    saveData();
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void addRecipeInteractive(Scanner sc, User user) {
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Short description: ");
        String desc = sc.nextLine().trim();
        System.out.println("Enter ingredients (one per line). Empty line to finish:");
        List<String> ingredients = readLines(sc);
        System.out.println("Enter steps (one per line). Empty line to finish:");
        List<String> steps = readLines(sc);
        System.out.println("Enter tags separated by comma (eg: breakfast,veg):");
        String tagsLine = sc.nextLine().trim();
        List<String> tags = Arrays.stream(tagsLine.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        Recipe r = addRecipe(user.getUsername(), title, desc, ingredients, steps, tags);
        System.out.println("Recipe added with id: " + r.getId());
    }

    private List<String> readLines(Scanner sc) {
        List<String> lines = new ArrayList<>();
        while (true) {
            String line = sc.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) break;
            lines.add(line);
        }
        return lines;
    }

    public void browseRecipes(Scanner sc, User user) {
        List<Recipe> all = listAllRecipes();
        if (all.isEmpty()) {
            System.out.println("Koi recipe nahi mila.");
            return;
        }
        printRecipeSummaries(all);
        System.out.print("Enter recipe id to see details, or blank to return: ");
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return;
        try {
            int id = Integer.parseInt(s);
            showRecipeDetailMenu(sc, id, user);
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
        }
    }

    private void printRecipeSummaries(List<Recipe> list) {
        System.out.println("\nRecipes:");
        for (Recipe r : list) {
            System.out.printf("[%d] %s (by %s) - likes: %d comments: %d\n",
                    r.getId(), r.getTitle(), r.getAuthorUsername(), r.getLikes().size(), r.getComments().size());
        }
    }

    private void showRecipeDetailMenu(Scanner sc, int id, User user) {
        Recipe r = getRecipeById(id);
        if (r == null) {
            System.out.println("Recipe not found.");
            return;
        }
        printRecipeDetail(r);

        System.out.println("\nOptions:");
        System.out.println("1. Like/Unlike");
        System.out.println("2. Comment");
        if (user != null && user.getUsername().equalsIgnoreCase(r.getAuthorUsername())) {
            System.out.println("3. Edit (owner)");
            System.out.println("4. Delete (owner)");
        }
        System.out.println("0. Back");
        System.out.print("Choose: ");
        String opt = sc.nextLine().trim();
        switch (opt) {
            case "1":
                if (user == null) {
                    System.out.println("Login required to like.");
                } else {
                    boolean nowLiked = likeRecipe(id, user.getUsername());
                    System.out.println(nowLiked ? "You liked the recipe." : "You unliked the recipe.");
                }
                break;
            case "2":
                if (user == null) {
                    System.out.println("Login required to comment.");
                } else {
                    System.out.print("Enter comment: ");
                    String text = sc.nextLine().trim();
                    if (!text.isEmpty()) {
                        commentRecipe(id, user.getUsername(), text);
                        System.out.println("Comment added.");
                    } else {
                        System.out.println("Empty comment ignored.");
                    }
                }
                break;
            case "3":
                if (user != null && user.getUsername().equalsIgnoreCase(r.getAuthorUsername())) {
                    editRecipeInteractive(sc, r.getId(), user);
                } else System.out.println("Not allowed.");
                break;
            case "4":
                if (user != null && user.getUsername().equalsIgnoreCase(r.getAuthorUsername())) {
                    boolean ok = deleteRecipe(r.getId(), user.getUsername());
                    if (ok) System.out.println("Deleted.");
                    else System.out.println("Failed to delete.");
                } else System.out.println("Not allowed.");
                break;
            case "0":
            default:
                // do nothing
        }
    }

    private void editRecipeInteractive(Scanner sc, int id, User user) {
        Recipe r = getRecipeById(id);
        if (r == null) return;
        System.out.println("Editing recipe id: " + id + " (leave blank to keep value)");
        System.out.print("Title [" + r.getTitle() + "]: ");
        String t = sc.nextLine().trim();
        String title = t.isEmpty() ? r.getTitle() : t;
        System.out.print("Description [" + r.getDescription() + "]: ");
        String d = sc.nextLine().trim();
        String desc = d.isEmpty() ? r.getDescription() : d;
        System.out.println("Ingredients (enter to keep existing). Enter new list, empty line to finish):");
        List<String> newIng = readLines(sc);
        if (newIng.isEmpty()) newIng = r.getIngredients();
        System.out.println("Steps (enter to keep existing):");
        List<String> newSteps = readLines(sc);
        if (newSteps.isEmpty()) newSteps = r.getSteps();
        System.out.print("Tags (comma) [" + String.join(",", r.getTags()) + "]: ");
        String tagLine = sc.nextLine().trim();
        List<String> newTags = tagLine.isEmpty() ? r.getTags() : Arrays.stream(tagLine.split(",")).map(String::trim).filter(s->!s.isEmpty()).collect(Collectors.toList());
        boolean ok = editRecipe(id, user.getUsername(), title, desc, newIng, newSteps, newTags);
        System.out.println(ok ? "Updated." : "Failed to update.");
    }

    private void printRecipeDetail(Recipe r) {
        System.out.println("\n--- Recipe Detail ---");
        System.out.println("ID: " + r.getId());
        System.out.println("Title: " + r.getTitle());
        System.out.println("Author: " + r.getAuthorUsername());
        System.out.println("Created: " + r.getCreatedAt());
        if (r.getLastUpdated() != null) System.out.println("Last updated: " + r.getLastUpdated());
        System.out.println("Description: " + r.getDescription());
        System.out.println("Ingredients:");
        for (String ing : r.getIngredients()) System.out.println(" - " + ing);
        System.out.println("Steps:");
        int idx = 1;
        for (String s : r.getSteps()) System.out.println(" " + (idx++) + ". " + s);
        System.out.println("Tags: " + String.join(", ", r.getTags()));
        System.out.println("Likes: " + r.getLikes().size());
        System.out.println("Comments:");
        for (Comment c : r.getComments()) {
            System.out.println(" - " + c.getAuthor() + " (" + c.getCreatedAt() + "): " + c.getText());
        }
    }

    private void myRecipesMenu(Scanner sc, User user) {
        List<Recipe> mine = recipes.values().stream()
                .filter(r -> r.getAuthorUsername().equalsIgnoreCase(user.getUsername()))
                .sorted(Comparator.comparing(Recipe::getCreatedAt).reversed())
                .collect(Collectors.toList());
        if (mine.isEmpty()) {
            System.out.println("You haven't added recipes yet.");
            return;
        }
        printRecipeSummaries(mine);
        System.out.print("Enter recipe id to view details, or blank: ");
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return;
        try {
            int id = Integer.parseInt(s);
            showRecipeDetailMenu(sc, id, user);
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
        }
    }

    private void followUserInteractive(Scanner sc, User user) {
        System.out.print("Enter username to follow: ");
        String other = sc.nextLine().trim();
        if (other.equalsIgnoreCase(user.getUsername())) {
            System.out.println("Can't follow yourself.");
            return;
        }
        User target = getUser(other);
        if (target == null) {
            System.out.println("User not found.");
            return;
        }
        boolean added = user.follow(target.getUsername());
        System.out.println(added ? "Now following " + target.getUsername() : "You already follow " + target.getUsername());
    }

    public void searchInteractive(Scanner sc, User user) {
        System.out.println("\nSearch by: 1) Title 2) Ingredient 3) Tag");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();
        List<Recipe> res = new ArrayList<>();
        switch (c) {
            case "1":
                System.out.print("Query title: ");
                res = searchByTitle(sc.nextLine().trim());
                break;
            case "2":
                System.out.print("Ingredient: ");
                res = searchByIngredient(sc.nextLine().trim());
                break;
            case "3":
                System.out.print("Tag: ");
                res = searchByTag(sc.nextLine().trim());
                break;
            default:
                System.out.println("Invalid.");
                return;
        }
        if (res.isEmpty()) {
            System.out.println("No results.");
            return;
        }
        printRecipeSummaries(res);
        System.out.print("Enter id to view detail, or blank: ");
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return;
        try {
            int id = Integer.parseInt(s);
            showRecipeDetailMenu(sc, id, user);
        } catch (NumberFormatException e) {
            System.out.println("Invalid id.");
        }
    }

    /* ---- Internal persistence container ---- */
    public static class PlatformData implements Serializable {
        private static final long serialVersionUID = 1L;
        Map<String, User> users;
        Map<Integer, Recipe> recipes;
        int nextRecipeId;
    }
}
