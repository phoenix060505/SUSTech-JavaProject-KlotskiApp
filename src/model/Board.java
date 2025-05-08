// Board.java
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects; // 导入 Objects 类

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

    /* 深拷贝功能，用于撤销，把当前棋盘完全克隆，所有block的大小，坐标和类型以及当前的move count（移动数）
        然后生成一个全新的board实例，与原对象完全脱离
        每移动一步就会留下快照，undo作用就是弹出上一张快照
        但它只记录当前状况不会记录每个物块的移动过程，因此还需添加一个move对象用来存放不同物块的指令
        record Move(String blockId, Direction dir) {}
        Deque<Move> moveSeq = new ArrayDeque<>();

     */
    public Board copy() {
        Board newBoard = new Board();
        newBoard.moveCount = this.moveCount; // 复制移动计数

        // 深拷贝所有 Block 对象
        for (Block block : blocks) {
            newBoard.addBlock(block.copy());
        }

        return newBoard;
    }

    // 重写 equals 方法，用于比较两个 Board 对象的内容是否相同
    // 重写 equals 方法，基于 Blocks 列表的内容（忽略顺序）进行比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;

        // 比较 blocks 列表的内容，忽略顺序
        if (blocks.size() != board.blocks.size()) return false;

        // 检查 this.blocks 中的每个 block 是否在 board.blocks 中找到一个相等的 block
        List<Block> otherBlocks = new ArrayList<>(board.blocks); // 创建一个副本以便移除找到的匹配项
        for (Block thisBlock : this.blocks) {
            boolean foundMatch = otherBlocks.remove(thisBlock); // 使用 Block.equals 查找并移除
            if (!foundMatch) return false; // 如果在 otherBlocks 中找不到匹配项，则 Board 不相等
        }

        return otherBlocks.isEmpty(); // 如果 otherBlocks 最终为空，说明所有 block 都找到了匹配项
    }


    // 重写 hashCode 方法，使用规范化表示确保哈希码唯一且与顺序无关
    @Override
    public int hashCode() {
        // 构建一个能唯一表示棋盘状态的规范化字符串
        // 将所有 block 的 (x, y, width, height, type) 信息按固定顺序组合
        // 先对 blocks 列表进行排序以确保顺序一致
        List<Block> sortedBlocks = new ArrayList<>(blocks);
        Collections.sort(sortedBlocks, (b1, b2) -> {
            // 按行、列、宽、高、类型排序
            if (b1.getY() != b2.getY()) return Integer.compare(b1.getY(), b2.getY());
            if (b1.getX() != b2.getX()) return Integer.compare(b1.getX(), b2.getX());
            if (b1.getWidth() != b2.getWidth()) return Integer.compare(b1.getWidth(), b2.getWidth());
            if (b1.getHeight() != b2.getHeight()) return Integer.compare(b1.getHeight(), b2.getHeight());
            return b1.getType().compareTo(b2.getType());
        });

        StringBuilder sb = new StringBuilder();
        for (Block block : sortedBlocks) {
            sb.append(block.getX()).append(",")
                    .append(block.getY()).append(",")
                    .append(block.getWidth()).append(",")
                    .append(block.getHeight()).append(",")
                    .append(block.getType()).append(";");
        }
        return sb.toString().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Board{\n");
        // 为了方便调试，可以打印出 Blocks 列表
        for (Block block : blocks) {
            sb.append("  ").append(block).append("\n");
        }
        sb.append("  moveCount=").append(moveCount).append("\n");
        sb.append("}");
        return sb.toString();
    }
}