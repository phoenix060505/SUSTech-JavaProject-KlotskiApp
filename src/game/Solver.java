// Solver.java
package game;

import model.Board;
import model.Block;
import java.util.*;

public class Solver {

    private final Set<Board> visitedStates;
    private final Queue<Node> queue;

    public Solver() {
        visitedStates = new HashSet<>();
        queue = new LinkedList<>();
    }

    /**
     * 寻找从起始棋盘状态到胜利状态的最短路径
     * @param startBoard 起始棋盘状态
     * @return 包含从起始状态到胜利状态的棋盘状态序列，如果无解则返回 null
     */
    public List<Board> solve(Board startBoard) {
        if (startBoard == null) {
            return null;
        }

        // 清空之前的求解状态
        visitedStates.clear();
        queue.clear();
        // 创建初始节点并加入队列和已访问集合
        Node initialNode = new Node(startBoard.copy(), null); // 使用副本作为起始节点
        queue.add(initialNode);
        visitedStates.add(initialNode.board); // 使用副本加入已访问集合

        // 广度优先搜索
        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            Board currentBoard = currentNode.board;
            // 检查当前状态是否为胜利状态
            // 使用 GameLogic 的 isGameWon 方法进行检查
            GameLogic tempLogicCheck = new GameLogic();
            tempLogicCheck.setBoard(currentBoard); // 设置当前棋盘给临时 GameLogic
            if (tempLogicCheck.isGameWon()) {
                return reconstructPath(currentNode);
            }
            // 查找所有可能的合法移动，生成下一个状态
            List<Board> nextStates = getNextStates(currentBoard);

            // 遍历所有可能的下一个状态
            for (Board nextBoard : nextStates) {
                // 如果该状态未被访问过
                if (!visitedStates.contains(nextBoard)) {
                    visitedStates.add(nextBoard);
                    Node nextNode = new Node(nextBoard, currentNode);
                    queue.add(nextNode);
                }
            }
        }
        // 队列为空，但未找到解决方案
        return null;
    }

    /**
     * 获取当前棋盘状态下所有可能的下一个合法棋盘状态
     * @param currentBoard 当前棋盘状态
     * @return 所有可能的下一个合法棋盘状态列表
     */
    private List<Board> getNextStates(Board currentBoard) {
        List<Board> nextStates = new ArrayList<>();
        // GameLogic 实例用于调用 isValidMove
        GameLogic gameLogic = new GameLogic();

        // 遍历当前棋盘上的所有物块
        for (Block blockToMove : currentBoard.getBlocks()) {
            // Store original position to find it in the copied board
            int originalX = blockToMove.getX();
            int originalY = blockToMove.getY();

            // Possible directions
            Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

            for (Direction direction : directions) {
                int newX = originalX;
                int newY = originalY;

                switch (direction) {
                    case UP:    newY--; break;
                    case DOWN:  newY++; break;
                    case LEFT:  newX--; break;
                    case RIGHT: newX++; break;
                }

                // 检查移动是否合法 on the current board layout
                // 使用 GameLogic 的 isValidMove 并传入 currentBoard
                if (gameLogic.isValidMove(currentBoard, blockToMove, newX, newY)) {
                    // 创建一个新棋盘状态 by copying the current one
                    Board nextBoard = currentBoard.copy();

                    // Find the corresponding block in the copied board using its original position and properties
                    Block blockInNextBoard = null;
                    // 遍历 copied board 中的 blocks 来找到与 original block 匹配的那个
                    for (Block b : nextBoard.getBlocks()) {
                        // 这里的比较应该基于 Block 的 equals 方法，该方法现在比较物理属性
                        // 找到在 copied board 中与 original block 相同（在 original state 的位置上）的那个 block
                        if (b.equals(blockToMove)) { // Use Block.equals which compares physical properties
                            blockInNextBoard = b;
                            break;
                        }
                    }


                    if (blockInNextBoard != null) {
                        // Move the block in the copied board
                        switch (direction) {
                            case UP:    blockInNextBoard.moveUp(); break;
                            case DOWN:  blockInNextBoard.moveDown(); break;
                            case LEFT:  blockInNextBoard.moveLeft(); break;
                            case RIGHT: blockInNextBoard.moveRight(); break;
                        }
                        // 增加移动计数 for the new state
                        nextBoard.incrementMoveCount();
                        // Add the new valid state to the list
                        nextStates.add(nextBoard);
                    } else {
                        // This case should ideally not happen if copy() and block finding logic is correct
                        System.err.println("Error: Could not find block in copied board during getNextStates.");
                    }
                }
            }
        }
        return nextStates;
    }
    /**
     * 从胜利节点回溯重建路径
     * @param victoryNode 胜利状态对应的节点
     * @return 从起始状态到胜利状态的棋盘状态序列
     */
    private List<Board> reconstructPath(Node victoryNode) {
        List<Board> path = new LinkedList<>();
        Node currentNode = victoryNode;
        while (currentNode != null) {
            path.add(0, currentNode.board); // 将当前状态添加到路径的开头
            currentNode = currentNode.parent; // 移动到父节点
        }
        return path;
    }

    /**
     * BFS 节点，存储棋盘状态和到达该状态的父节点
     */
    private static class Node {
        Board board;
        Node parent;

        Node(Board board, Node parent) {
            this.board = board;
            this.parent = parent;
        }
    }
}