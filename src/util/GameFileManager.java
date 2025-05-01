package util;

import model.GameState;
import user.User;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class GameFileManager {
    private static final String USER_FILE = "users.dat";
    private static final String SAVE_DIR = "saves/";
    private ScheduledExecutorService scheduler;
    private static final long SAVE_INTERVAL = 5; // 5 seconds interval
    private ScheduledFuture<?> currentAutoSaveTask;

    public GameFileManager() {
        // Create save directory if it doesn't exist
        File saveDir = new File(SAVE_DIR);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        // Initialize scheduled executor service for periodic save
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    // Save game state to file
    public boolean saveGame(String username, GameState state) {
        if (username == null || state == null) {
            System.err.println("Cannot save game: username or state is null");
            return false;
        }

        try {
            FileOutputStream fileOut = new FileOutputStream(SAVE_DIR + username + ".save");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(state);
            out.close();
            fileOut.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving game for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Load game state from file
    public GameState loadGame(String username) {
        try {
            FileInputStream fileIn = new FileInputStream(SAVE_DIR + username + ".save");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            GameState state = (GameState) in.readObject();
            in.close();
            fileIn.close();
            return state;
        } catch (IOException | ClassNotFoundException e) {
            // File doesn't exist or is corrupted
            return null;
        }
    }

    // Save users to file
    public boolean saveUsers(Map<String, User> users) {
        try {
            FileOutputStream fileOut = new FileOutputStream(USER_FILE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(users);
            out.close();
            fileOut.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Load users from file
    @SuppressWarnings("unchecked")
    public Map<String, User> loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try {
            FileInputStream fileIn = new FileInputStream(USER_FILE);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Map<String, User> users = (Map<String, User>) in.readObject();
            in.close();
            fileIn.close();
            return users;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    // Start periodic save every 5 seconds
    // Uses a supplier to always get the current game state, not just the initial state
    public void startAutoSave(User user, Supplier<GameState> stateSupplier) {
        // Cancel any existing auto-save task
        stopAutoSave();

        if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) {
            System.err.println("Cannot start auto-save: user is null or has no username");
            return;
        }

        currentAutoSaveTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                GameState currentState = stateSupplier.get();
                if (currentState != null) {
                    if (saveGame(user.getUsername(), currentState)) {
                        System.out.println("Game auto-saved for user: " + user.getUsername());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during auto-save: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, SAVE_INTERVAL, TimeUnit.SECONDS);
    }

    // Stop the periodic save task
    public void stopAutoSave() {
        if (currentAutoSaveTask != null) {
            currentAutoSaveTask.cancel(false);
            currentAutoSaveTask = null;
            System.out.println("Auto-save stopped");
        }
    }

    // Shutdown the scheduler completely (call this when the application closes)
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            stopAutoSave();
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("GameFileManager scheduler shutdown");
        }
    }
}