// Level.java
package model;

import javafx.scene.paint.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Level implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<BlockData> blockData;

    public Level(String name) {
        this.name = name;
        this.blockData = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<BlockData> getBlockData() {
        return blockData;
    }

    public void addBlock(int x, int y, int width, int height, String type, Color color) {
        blockData.add(new BlockData(x, y, width, height, type, String.valueOf(color)));
    }

    // Apply level data to a board
// Apply level data to a board
    public void applyToBoard(Board board) {
        board.getBlocks().clear();
        board.resetMoveCount();

        for (BlockData data : blockData) {
            // Color color = Color.valueOf(data.color); // Remove this line
            Block block = new Block(data.x, data.y, data.width, data.height, data.type, data.color); // Use data.color directly
            board.addBlock(block);
        }
    }

    // Inner class to store block data in a serializable format
    public static class BlockData implements Serializable {
        private static final long serialVersionUID = 1L;

        public int x, y, width, height;
        public String type;
        public String color;

        public BlockData(int x, int y, int width, int height, String type, String color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
            this.color = color;
        }
    }
}