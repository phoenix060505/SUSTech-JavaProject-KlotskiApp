// Block.java
package model;
import java.io.Serializable;
// Removed: import javafx.scene.paint.Color;

public class Block implements Serializable  {
    private static final long serialVersionUID = 1L; // Add serialVersionUID
    private int x, y;           // 记录位置
    private int width, height;  // 记录大小
    private String type;        // 记录类型
    private String colorString; // 记录颜色，用String表示，如"RED","FF0000"
    public Block(int x, int y, int width, int height, String type, String colorString) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.colorString = colorString;
    }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getType() { return type; }
    public String getColorString() { return colorString; }
    public void moveUp() { y--; }
    public void moveDown() { y++; }
    public void moveLeft() { x--; }
    public void moveRight() { x++; }
    //深拷贝
    public Block copy() {
        return new Block(x, y, width, height, type, colorString);
    }
}