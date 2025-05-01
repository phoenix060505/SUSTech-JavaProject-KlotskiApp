// GameState.java
package model;
import java.io.Serializable;
import java.util.Deque;
import java.util.ArrayDeque;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Board board;
    // private int moveCount; // Board already has moveCount, so this might be redundant
    private long timeElapsed;
    private String username;
    private int currentLevel;
    private Deque<Board> moveHistory; // 添加移动历史
    private boolean isGameWon; // 添加游戏胜利状态

    // 更新构造函数以包含所有状态信息
    public GameState(Board board, String username, int currentLevel, Deque<Board> moveHistory, boolean isGameWon) {
        this.board = board;
        // this.moveCount = board.getMoveCount(); // 如果Board存储了，这里不需要
        this.timeElapsed = 0; //  elapsed time should be set when saving
        this.username = username;
        this.currentLevel = currentLevel;
        this.moveHistory = moveHistory;
        this.isGameWon = isGameWon;
    }

    // Getters
    public Board getBoard() { return board; }
    // public int getMoveCount() { return moveCount; } // 如果Board有此方法，这里不需要
    public long getTimeElapsed() { return timeElapsed; }
    public void setTimeElapsed(long timeElapsed) { this.timeElapsed = timeElapsed; } // Setter for time elapsed
    public String getUsername() { return username; }
    public int getCurrentLevel() { return currentLevel; }
    public Deque<Board> getMoveHistory() { return moveHistory; } // 获取移动历史
    public boolean isGameWon() { return isGameWon; } // 获取游戏胜利状态
}