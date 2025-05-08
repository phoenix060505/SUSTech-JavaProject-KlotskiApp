// Block.java
package model;
import java.io.Serializable;
import java.util.Objects; // 导入 Objects 类

public class Block implements Serializable  {
    private static final long serialVersionUID = 1L;
    private int x, y;
    private int width, height;
    private String type;
    private String colorString;

    public Block(int x, int y, int width, int height, String type, String colorString) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.colorString = colorString;
    }

    public Block copy() {
        return new Block(x, y, width, height, type, colorString);
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

    // 重写 equals 方法，基于位置、大小和类型比较 Block 对象的内容
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return x == block.x &&
                y == block.y &&
                width == block.width &&
                height == block.height &&
                Objects.equals(type, block.type);
    }

    // 重写 hashCode 方法，保证相等的 Block 对象具有相同的哈希码
    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height, type);
    }

    @Override
    public String toString() {
        return "Block{" +
                "x=" + x +
                ", y=" + y +
                ", w=" + width +
                ", h=" + height +
                ", type='" + type + '\'' +
                '}';
    }
}