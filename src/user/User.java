// User.java
package user;

import model.GameState;
import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private GameState savedGame;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.savedGame = null;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public GameState getSavedGame() {
        return savedGame;
    }

    public void setSavedGame(GameState savedGame) {
        this.savedGame = savedGame;
    }
}