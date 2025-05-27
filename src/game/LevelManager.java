package game;

import model.Level;

/* 预置关卡集合 */
public class LevelManager {

    /* 简单的颜色常量（CSS / HTML 格式） */
    private static final String RED    ="#FF0000";
    private static final String BLUE   ="#0000FF";
    private static final String GREEN  ="#00A000";
    private static final String ORANGE ="#FFA500";

    private final java.util.List<Level> levels = new java.util.ArrayList<>();

    public LevelManager(){ init(); }
    private void init(){
        /* ---------- Level 1 ---------- */
        Level lv1=new Level("Classic");
        lv1.addBlock(1,0,2,2,"CaoCao",RED);
        lv1.addBlock(1,2,2,1,"GuanYu",GREEN);
        lv1.addBlock(0,0,1,2,"General",BLUE);
        lv1.addBlock(3,0,1,2,"General",BLUE);
        lv1.addBlock(0,2,1,2,"General",BLUE);
        lv1.addBlock(3,2,1,2,"General",BLUE);
        lv1.addBlock(0,4,1,1,"Soldier",ORANGE);
        lv1.addBlock(1,3,1,1,"Soldier",ORANGE);
        lv1.addBlock(2,3,1,1,"Soldier",ORANGE);
        lv1.addBlock(3,4,1,1,"Soldier",ORANGE);
        levels.add(lv1);

        /* ---------- Level 2 ---------- */
        Level lv2=new Level("Advanced");
        lv2.addBlock(1,0,2,2,"CaoCao",RED);
        lv2.addBlock(0,0,1,2,"General",BLUE);
        lv2.addBlock(3,0,1,2,"General",BLUE);
        lv2.addBlock(0,2,1,2,"General",BLUE);
        lv2.addBlock(3,2,1,2,"General",BLUE);
        lv2.addBlock(1,2,1,1,"Soldier",ORANGE);
        lv2.addBlock(2,2,1,1,"Soldier",ORANGE);
        lv2.addBlock(1,3,2,1,"GuanYu",GREEN);
        lv2.addBlock(0,4,1,1,"Soldier",ORANGE);
        lv2.addBlock(3,4,1,1,"Soldier",ORANGE);
        levels.add(lv2);

        /* ---------- Level 3 ---------- */
        Level lv3=new Level("Expert");
        lv3.addBlock(1,0,2,2,"CaoCao",RED);
        lv3.addBlock(0,0,1,1,"Soldier",ORANGE);
        lv3.addBlock(3,0,1,1,"Soldier",ORANGE);
        lv3.addBlock(0,1,1,2,"General",BLUE);
        lv3.addBlock(3,1,1,2,"General",BLUE);
        lv3.addBlock(1,2,2,1,"GuanYu",GREEN);
        lv3.addBlock(0,3,1,2,"General",BLUE);
        lv3.addBlock(1,3,1,1,"Soldier",ORANGE);
        lv3.addBlock(2,3,1,1,"Soldier",ORANGE);
        lv3.addBlock(3,3,1,2,"General",BLUE);
        levels.add(lv3);
    }

    public Level getLevel(int idx){return levels.get(Math.max(0,Math.min(idx-1,levels.size()-1)));}
    public int   getLevelCount(){return levels.size();}
}
