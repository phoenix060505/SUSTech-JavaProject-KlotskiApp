// UserManager.java
package user;

import model.GameState;
import util.GameFileManager;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private Map<String, User> users;
    private User currentUser;
    private GameFileManager fileManager;

    public UserManager() {
        users = new HashMap<>();
        fileManager = new GameFileManager();
        loadUsers();
    }
    // Register a new user

    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;  // User already exists
        }

        User newUser = new User(username, password);
        users.put(username, newUser);
        saveUsers();
        return true;
    }

    // Login a user
    public boolean loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    // Login as guest
    public void loginAsGuest() {
        currentUser = null;
    }

    // Check if current user is guest
    public boolean isGuest() {
        return currentUser == null;
    }

    // Get current user
    public User getCurrentUser() {
        return currentUser;
    }

    // Save game for current user
    public boolean saveGameForCurrentUser(GameState state) {
        if (isGuest()) {
            return false;
        }

        currentUser.setSavedGame(state);
        return fileManager.saveGame(currentUser.getUsername(), state);
    }

    // Load game for current user
    public GameState loadGameForCurrentUser() {
        if (isGuest()) {
            return null;
        }

        GameState state = fileManager.loadGame(currentUser.getUsername());
        if (state != null) {
            currentUser.setSavedGame(state);
        }
        return state;
    }

    // Save users to file
    private void saveUsers() {
        fileManager.saveUsers(users);
    }

    // Load users from file
    private void loadUsers() {
        Map<String, User> loadedUsers = fileManager.loadUsers();
        if (loadedUsers != null) {
            users = loadedUsers;
        }
    }

    // Logout
    public void logout() {
        currentUser = null;
    }
}