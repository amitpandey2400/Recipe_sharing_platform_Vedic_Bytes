package platform;

import platform.models.User;
import platform.utils.IOUtils;

import java.util.Scanner;

public class AuthService {
    private Platform platform;

    public AuthService(Platform platform) {
        this.platform = platform;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void registerInteractive(Scanner sc) {
        System.out.print("Choose username: ");
        String username = sc.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username can't be empty.");
            return;
        }
        if (platform.usernameExists(username)) {
            System.out.println("Username already taken.");
            return;
        }
        System.out.print("Choose password: ");
        String pass = sc.nextLine().trim();
        if (pass.length() < 4) {
            System.out.println("Password too short (min 4).");
            return;
        }
        System.out.print("Display name (optional): ");
        String disp = sc.nextLine().trim();
        User u = new User(username, IOUtils.hash(pass), disp.isEmpty() ? username : disp);
        platform.addUser(u);
        platform.saveData();
        System.out.println("Registered successfully. You can login now.");
    }

    public User loginInteractive(Scanner sc) {
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String pass = sc.nextLine().trim();
        User u = platform.getUser(username);
        if (u == null) {
            System.out.println("No such user.");
            return null;
        }
        if (u.getPasswordHash().equals(IOUtils.hash(pass))) {
            System.out.println("Login successful. Welcome " + u.getDisplayName());
            return u;
        } else {
            System.out.println("Wrong password.");
            return null;
        }
    }
}
