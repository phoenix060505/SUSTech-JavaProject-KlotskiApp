// Board.java
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rows = 5;
    private int cols = 4;
    private List<Block> blocks;//储存所有的blocks
    private int moveCount;//记录移动次数

    public Board() {
        blocks = new ArrayList<>();
        moveCount = 0;
    }

    public void addBlock(Block block) {
        blocks.add(block);
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void incrementMoveCount() {
        moveCount++;
    }
    public void resetMoveCount() {
        moveCount = 0;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    // Check if a position is occupied by any block
    public boolean isOccupied(int x, int y) {
        for (Block block : blocks) {
            if (x >= block.getX() && x < block.getX() + block.getWidth() &&
                    y >= block.getY() && y < block.getY() + block.getHeight()) {
                return true;
            }
        }
        return false;
    }

    // Find block at position
    public Block getBlockAt(int x, int y) {
        for (Block block : blocks) {
            if (x >= block.getX() && x < block.getX() + block.getWidth() &&
                    y >= block.getY() && y < block.getY() + block.getHeight()) {
                return block;
            }
        }
        return null;
    }

    // Deep copy the board for undo functionality(深拷贝功能，用于撤销)
    public Board copy() {
        Board newBoard = new Board();
        newBoard.moveCount = this.moveCount;

        for (Block block : blocks) {
            newBoard.addBlock(block.copy());
        }

        return newBoard;
    }
}