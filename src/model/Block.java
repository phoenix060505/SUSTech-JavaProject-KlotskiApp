// Block.java
package model;
import java.io.Serializable;
// Removed: import javafx.scene.paint.Color;

public class Block implements Serializable  {
    private static final long serialVersionUID = 1L; // Add serialVersionUID

    private int x, y;           // Position on the board
    private int width, height;  // Size of the block
    private String type;        // Type of block (Cao Cao, Guan Yu, etc.)
    private String colorString; // Color of the block as a String (changed from Color)

    // Modified constructor to accept String for color
    public Block(int x, int y, int width, int height, String type, String colorString) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.colorString = colorString; // Assign the color string
    }

    // Getters and setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getType() { return type; }
    // New getter for color string
    public String getColorString() { return colorString; }

    // Removed the old public Color getColor() method

    // Move methods
    public void moveUp() { y--; }
    public void moveDown() { y++; }
    public void moveLeft() { x--; }
    public void moveRight() { x++; }

    // Deep copy for undo functionality
    public Block copy() {
        return new Block(x, y, width, height, type, colorString); // Use colorString in copy
    }
}