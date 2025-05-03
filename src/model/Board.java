// Board.java
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private int rows = 5; //行为5
    private int cols = 4; //列为4
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

    // 检查位置是否被其他物块占据
    public boolean isOccupied(int x, int y) {
        for (Block block : blocks) {
            if (x >= block.getX() && x < block.getX() + block.getWidth() &&
                    y >= block.getY() && y < block.getY() + block.getHeight()) {
                return true;
            }
        }
        return false;
    }

    // 找到这个坐标的物块
    public Block getBlockAt(int x, int y) {
        for (Block block : blocks) {
            if (x >= block.getX() && x < block.getX() + block.getWidth() &&
                    y >= block.getY() && y < block.getY() + block.getHeight()) {
                return block;
            }
        }
        return null;
    }

    /*  深拷贝功能，用于撤销，把当前棋盘完全克隆，所有block的大小，坐标和类型以及当前的move count（移动数）
        然后生成一个全新的board实例，与原对象完全脱离
        每移动一步就会留下快照，undo作用就是弹出上一张快照
        但它只记录当前状况不会记录每个物块的移动过程，因此还需添加一个move对象用来存放不同物块的指令
        record Move(String blockId, Direction dir) {}
        Deque<Move> moveSeq = new ArrayDeque<>();

     */
    public Board copy() {
        Board newBoard = new Board();
        newBoard.moveCount = this.moveCount;

        for (Block block : blocks) {
            newBoard.addBlock(block.copy());
        }

        return newBoard;
    }
}