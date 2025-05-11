// GameLogic.java
package game;
import model.Block;
import model.Board;
import javafx.scene.paint.Color;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;

public class GameLogic {
    private Board board;
    private boolean isGameWon;
    private Deque<Board> moveHistory;
    private Solver solver;
    // Fields for step-by-step hints
    private List<Board> activeSolutionHintPath = null;
    private int currentHintStepInActivePath = 0; // Index of the board state *to be shown next* from activeSolutionHintPath

    public GameLogic() {
        moveHistory = new ArrayDeque<>();
        solver = new Solver();
    }

    public void setBoard(Board board) {
        this.board = board;
        // 设置新棋盘后，重新检查胜利状态
        this.isGameWon = isGameWon();
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
    public void initializeGame() {
        board = new Board();
        isGameWon = false;
        moveHistory.clear();
        clearActiveHintPath(); // Reset hints for a new game

        // Initialize with standard layout
        Block caoBlock = new Block(1, 0, 2, 2, "CaoCao", "RED");
        board.addBlock(caoBlock);

        Block guanBlock = new Block(1, 2, 2, 1, "GuanYu", "GREEN");
        board.addBlock(guanBlock);

        board.addBlock(new Block(0, 0, 1, 2, "General", "BLUE"));
        board.addBlock(new Block(3, 0, 1, 2, "General", "BLUE"));
        board.addBlock(new Block(0, 2, 1, 2, "General", "BLUE"));
        board.addBlock(new Block(3, 2, 1, 2, "General", "BLUE"));

        board.addBlock(new Block(0, 4, 1, 1, "Soldier", "ORANGE"));
        board.addBlock(new Block(1, 3, 1, 1, "Soldier", "ORANGE"));
        board.addBlock(new Block(2, 3, 1, 1, "Soldier", "ORANGE"));
        board.addBlock(new Block(3, 4, 1, 1, "Soldier", "ORANGE"));

        saveCurrentState(); // 保存初始状态
    }

    public Board getBoard() {
        return board;
    }

    // 检查游戏是否胜利（Getter 方法）
    public boolean isGameWon() {
        if (board == null) {
            return false;
        }
        // 检查胜利条件 (曹操块 (类型为 "CaoCao") 在棋盘底部中间 (1, 3))
        for (Block block : board.getBlocks()) {
            if ("CaoCao".equals(block.getType()) && block.getX() == 1 && block.getY() == 3) {
                this.isGameWon = true; // 更新内部状态
                return true;
            }
        }
        this.isGameWon = false; // 更新内部状态
        return false;
    }

    // 保存当前状态到历史
    private void saveCurrentState() {
        if (board != null && moveHistory != null) {
            // 在保存前，先检查历史栈的顶部是否与当前棋盘状态相同
            // 避免连续保存相同状态（例如，尝试移动失败后不应新增历史）
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

    // 移动一个物块
    public boolean moveBlock(Block block, Direction direction) {
        if (board == null || block == null) return false;

        int currentX = block.getX();
        int currentY = block.getY();
        int newX = currentX;
        int newY = currentY;

        switch (direction) {
            case UP: newY--; break;
            case DOWN: newY++; break;
            case LEFT: newX--; break;
            case RIGHT: newX++; break;
        }

        // 检查移动是否合法，使用接受 Board 参数的方法
        if (!isValidMove(this.board, block, newX, newY)) {
            return false; // 移动不合法
        }

        // 移动合法，先保存当前状态，然后执行移动
        saveCurrentState();

        // 更新物块位置
        block.setX(newX);
        block.setY(newY);

        // 增加移动计数
        board.incrementMoveCount();

        // 检查是否胜利（isGameWon() getter 会处理）

        return true; // 移动成功
    }

    // 检查移动是否合法 (接受 Board 参数)
    // 这是 Solver 将使用的主要验证方法
    public boolean isValidMove(Board board, Block block, int newX, int newY) {
        if (board == null || block == null) return false;

        // 检查棋盘边界
        if (newX < 0 || newY < 0 ||
                newX + block.getWidth() > board.getCols() ||
                newY + block.getHeight() > board.getRows()) {
            return false;
        }

        // 检查与其它物块的碰撞
        for (int checkX = newX; checkX < newX + block.getWidth(); checkX++) {
            for (int checkY = newY; checkY < newY + block.getHeight(); checkY++) {
                // 遍历棋盘上所有的块，检查目标位置是否被除了当前移动块以外的任何块占据
                for (Block occupyingBlock : board.getBlocks()) {
                    // 如果正在检查的块是我们要移动的块，跳过它
                    if (occupyingBlock == block) {
                        continue;
                    }

                    // 检查目标坐标 (checkX, checkY) 是否在 occupyingBlock 的范围内
                    if (checkX >= occupyingBlock.getX() && checkX < occupyingBlock.getX() + occupyingBlock.getWidth() &&
                            checkY >= occupyingBlock.getY() && checkY < occupyingBlock.getY() + occupyingBlock.getHeight()) {
                        return false; // 目标位置被另一个物块占据
                    }
                }
            }
        }

        return true; // 移动合法
    }


    // 撤销上一步移动
    public boolean undoMove() {
        if (moveHistory == null || moveHistory.size() <= 1) {
            return false;  // 无法撤销（只剩初始状态或无历史）
        }

        moveHistory.pop();  // 移除当前状态
        Board previousBoard = moveHistory.peek(); // 获取上一个状态
        if (previousBoard != null) {
            // 使用上一个状态的副本来更新当前棋盘
            board = previousBoard.copy();
            // 棋盘的 moveCount 在 copy 时已经被复制，所以无需单独设置
            // 胜利状态会在 isGameWon() getter 中重新检查
            return true; // 撤销成功
        }
        return false; // 不应该发生
    }

    /**
     * 使用求解器获取下一步提示
     * @return 下一步的棋盘状态，如果无解或已是最终状态则返回 null
     */
    public Board getHint() {
        if (isGameWon()) { // 如果已胜利，不提供提示
            return null;
        }

        // 传递当前棋盘的副本来进行求解，避免求解过程修改当前棋盘
        List<Board> solutionPath = solver.solve(board.copy());

        // 如果找到了解决方案且路径长度大于 1 (第一步是当前状态)，返回下一步
        if (solutionPath != null && solutionPath.size() > 1) {
            return solutionPath.get(1); // 返回路径中的第二个状态
        }

        // 无解或已是最终状态
        return null;
    }
}