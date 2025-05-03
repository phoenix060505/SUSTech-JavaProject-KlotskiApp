// GameLogic.java
package game;
import model.Block;
import model.Board;
import javafx.scene.paint.Color;
import java.util.Deque;
import java.util.ArrayDeque;
public class GameLogic {
    private Board board;
    private boolean isGameWon;
    private Deque<Board> moveHistory;

    public GameLogic() {
        moveHistory = new ArrayDeque<>();
    }
    // Method to set the board state - NEW
    public void setBoard(Board board) {
        this.board = board;
    }

    // Method to set the move history state - NEW
    public void setMoveHistory(Deque<Board> moveHistory) {
        this.moveHistory = moveHistory;
    }

    // Method to set the game won state - NEW
    public void setIsGameWon(boolean isGameWon) {
        this.isGameWon = isGameWon;
    }
    public void initializeGame() {
        board = new Board();
        isGameWon = false;
        moveHistory.clear();

        // Initialize with standard layout
        // Cao Cao block (2x2) - Pass color as String
        Block caoBlock = new Block(1, 0, 2, 2, "CaoCao", "RED");
        board.addBlock(caoBlock);

        // Guan Yu block (2x1) - Pass color as String
        Block guanBlock = new Block(1, 2, 2, 1, "GuanYu", "GREEN");
        board.addBlock(guanBlock);

        // General blocks (1x2) - Pass color as String
        board.addBlock(new Block(0, 0, 1, 2, "General", "BLUE"));
        board.addBlock(new Block(3, 0, 1, 2, "General", "BLUE"));
        board.addBlock(new Block(0, 2, 1, 2, "General", "BLUE"));
        board.addBlock(new Block(3, 2, 1, 2, "General", "BLUE"));

        // Soldier blocks (1x1) - Pass color as String
        board.addBlock(new Block(0, 4, 1, 1, "Soldier", "ORANGE"));
        board.addBlock(new Block(1, 3, 1, 1, "Soldier", "ORANGE"));
        board.addBlock(new Block(2, 3, 1, 1, "Soldier", "ORANGE"));
        board.addBlock(new Block(3, 4, 1, 1, "Soldier", "ORANGE"));

        // Save initial state for undo
        saveCurrentState();
    }

    public Board getBoard() {
        return board;
    }

    public boolean isGameWon() {
        return isGameWon;
    }

    // Save current state for undo
    private void saveCurrentState() {
        moveHistory.push(board.copy());
    }

    public Deque<Board> getMoveHistory() {
        return moveHistory;
    }

    // Move a block in a direction
    public boolean moveBlock(Block block, Direction direction) {
        if (block == null) return false;

        int newX = block.getX();
        int newY = block.getY();

        switch (direction) {
            case UP:
                newY--;
                break;
            case DOWN:
                newY++;
                break;
            case LEFT:
                newX--;
                break;
            case RIGHT:
                newX++;
                break;
        }

        // Check if move is valid
        if (!isValidMove(block, newX, newY)) {
            return false;
        }

        // Update position
        block.setX(newX);
        block.setY(newY);

        // Increment move count
        board.incrementMoveCount();

        // Check for victory
        checkVictory();
        moveHistory.push(board.copy());
        return true;
    }

    // Check if a move is valid
    private boolean isValidMove(Block block, int newX, int newY) {
        // Check board boundaries
        if (newX < 0 || newY < 0 ||
                newX + block.getWidth() > board.getCols() ||
                newY + block.getHeight() > board.getRows()) {
            return false;
        }

        // Check collision with other blocks
        for (int x = newX; x < newX + block.getWidth(); x++) {
            for (int y = newY; y < newY + block.getHeight(); y++) {
                Block occupyingBlock = board.getBlockAt(x, y);
                if (occupyingBlock != null && occupyingBlock != block) {
                    return false;
                }
            }
        }

        return true;
    }

    // Check if game is won (Cao Cao block at exit)
    private void checkVictory() {
        for (Block block : board.getBlocks()) {
            if (block.getType().equals("CaoCao") &&
                    block.getX() == 1 && block.getY() == 3) {
                isGameWon = true;
                return;
            }
        }
    }

    // Undo last move
    public boolean undoMove() {
        if (moveHistory.size() <=1) {
            return false;  // Nothing to undo
        }

        moveHistory.pop();  // Remove current state
        board = moveHistory.peek().copy();  // Restore previous state
        isGameWon = false;  // Reset victory status
        return true;
    }
}