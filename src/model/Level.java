package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class Level implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final List<BlockData> blockData = new ArrayList<>();

    public Level(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public List<BlockData> getBlockData() {
        return blockData;
    }

    /** 关卡内新增一个方块（颜色改为 16 进制 / CSS 颜色字符串） */
    public void addBlock(int x,int y,int w,int h,String type,String colorHex){
        blockData.add(new BlockData(x,y,w,h,type,colorHex));
    }

    /** 把关卡布局写入棋盘 */
    public void applyToBoard(Board board){
        board.getBlocks().clear();
        board.resetMoveCount();
        for(BlockData d:blockData){
            board.addBlock(new Block(d.x,d.y,d.w,d.h,d.type,d.colorHex));
        }
    }

    /* ---------- 内部可序列化结构 ---------- */
    public static class BlockData implements Serializable{
        private static final long serialVersionUID = 1L;
        public int x,y,w,h; public String type,colorHex;
        public BlockData(int x,int y,int w,int h,String type,String colorHex){
            this.x=x; this.y=y; this.w=w; this.h=h; this.type=type; this.colorHex=colorHex;
        }
    }
}
