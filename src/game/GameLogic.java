// GameLogic.java
package game;
import model.Block;
import model.Board;
import game.LevelManager;
import java.awt.*;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;
import model.Level;
public class GameLogic {
    private Board board;
    private boolean isGameWon;
    private Deque<Board> moveHistory;
    private final Solver solver;
    private List<Board> activeSolutionHintPath = null;
    private int currentHintStepInActivePath = 0;

    public GameLogic() {
        moveHistory = new ArrayDeque<>();
        solver = new Solver();
    }

    public void setBoard(Board board) {
        this.board = board;
        if (this.board != null) {
            this.isGameWon = checkWinConditionInternal();
        } else {
            this.isGameWon = false;
        }
    }

    public void clearActiveHintPath() {
        this.activeSolutionHintPath = null;
        this.currentHintStepInActivePath = 0;
    }

    public void setMoveHistory(Deque<Board> moveHistory) {
        this.moveHistory = moveHistory;
    }

    public void setIsGameWon(boolean isGameWon) {
        this.isGameWon = isGameWon;
    }

    public void initializeGame(int levelNumber) {
        board = new Board();
        isGameWon = false;
        moveHistory.clear();
        clearActiveHintPath();
        LevelManager levelManager = new LevelManager();
        Level level = levelManager.getLevel(levelNumber);
        level.applyToBoard(board);
        this.board.resetMoveCount(); // 确保初始步数为0
        saveCurrentState(); // 保存初始状态 (moveCount 为 0)
    }

    public Board getBoard() {
        return board;
    }

    private boolean checkWinConditionInternal() {
        if (board == null || board.getBlocks() == null) return false;
        for (Block block : board.getBlocks()) {
            if (block != null && "CaoCao".equals(block.getType()) && block.getX() == 1 && block.getY() == 3) {
                return true;
            }
        }
        return false;
    }

    public boolean isGameWon() {
        this.isGameWon = checkWinConditionInternal();
        return this.isGameWon;
    }

    private void saveCurrentState() {
        if (board != null && moveHistory != null) {
            // 关键: Board.equals() 现在应该能正确区分包含不同 moveCount 的状态
            if (moveHistory.isEmpty() || !moveHistory.peek().equals(board)) {
                moveHistory.push(board.copy());
            }
        } else {
            System.err.println("Warning: Cannot save state, board or moveHistory is null.");
        }
    }

    public Deque<Board> getMoveHistory() {
        return moveHistory;
    }

    public boolean moveBlock(Block blockFromUI, Direction direction) { // 重命名参数以示区分
        if (board == null || blockFromUI == null || isGameWon()) {
            return false;
        }

        // 确保操作的是 board 内部的 Block 实例
        Block blockToMove = null;
        if (board.getBlocks() != null) {
            for (Block b : board.getBlocks()) {
                // Block.equals 比较位置、大小、类型
                if (b != null && b.equals(blockFromUI)) {
                    blockToMove = b;
                    break;
                }
            }
        }

        if (blockToMove == null) {
            System.err.println("Warning: blockFromUI not found in current board's blocks via equals.");
            blockToMove = blockFromUI; // 退回到使用传入的引用，但这可能不安全
        }


        int currentX = blockToMove.getX();
        int currentY = blockToMove.getY();
        int newX = currentX;
        int newY = currentY;

        switch (direction) {
            case UP: newY--; break;
            case DOWN: newY++; break;
            case LEFT: newX--; break;
            case RIGHT: newX++; break;
        }

        if (!isValidMove(this.board, blockToMove, newX, newY)) {
            return false;
        }

        // 关键修改：先更新棋盘状态，然后保存
        blockToMove.setX(newX);
        blockToMove.setY(newY);
        board.incrementMoveCount(); // 步数增加

        saveCurrentState(); // 保存移动完成之后的新状态 (此时 moveCount 已经更新)

        return true;
    }

    public boolean isValidMove(Board currentBoardState, Block blockToMove, int newX, int newY) {
        if (currentBoardState == null || blockToMove == null || currentBoardState.getBlocks() == null) return false;
        if (newX < 0 || newY < 0 || newX + blockToMove.getWidth() > currentBoardState.getCols() || newY + blockToMove.getHeight() > currentBoardState.getRows()) return false;

        for (int checkX = newX; checkX < newX + blockToMove.getWidth(); checkX++) {
            for (int checkY = newY; checkY < newY + blockToMove.getHeight(); checkY++) {
                for (Block occupyingBlock : currentBoardState.getBlocks()) {
                    if (occupyingBlock == blockToMove) continue;
                    if (occupyingBlock != null &&
                            checkX >= occupyingBlock.getX() && checkX < occupyingBlock.getX() + occupyingBlock.getWidth() &&
                            checkY >= occupyingBlock.getY() && checkY < occupyingBlock.getY() + occupyingBlock.getHeight()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean undoMove() {
        if (moveHistory == null || moveHistory.size() <= 1 ) {
            return false;
        }

        moveHistory.pop();
        Board previousBoard = moveHistory.peek();

        if (previousBoard != null) {
            this.board = previousBoard.copy();
            this.isGameWon = checkWinConditionInternal();
            return true;
        }
        return false;
    }

    public Board getHint() {
        if (isGameWon()) return null;
        if (board == null) return null;
        List<Board> solutionPath = solver.solve(board.copy());
        Label moveCountLabel = new Label();
        moveCountLabel.setText("Moves: " + getBoard().getMoveCount()); // 更新步数显示
        if (solutionPath != null && solutionPath.size() > 1) return solutionPath.get(1);
        return null;
    }

    public void applyHintState(Board hintBoard) {
        if (hintBoard == null) return;
        this.board = hintBoard.copy(); // 使用提示棋盘的副本更新当前棋盘
        this.isGameWon = checkWinConditionInternal(); // 检查胜利条件
        saveCurrentState(); // 调用现有的方法将当前棋盘状态（即hintBoard的副本）保存到历史记录
    }
    public void restartGame(int currentLevel) {
        if (board != null) {
            board.resetMoveCount();
            initializeGame(currentLevel);
        }
    }
}
