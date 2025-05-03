// LevelManager.java
package game;
import model.Level;
import model.Board;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private List<Level> levels;

    public LevelManager() {
        levels = new ArrayList<>();
        initializeLevels();
    }

    // Initialize built-in levels
    private void initializeLevels() {
        // Level 1: Standard layout
        Level level1 = new Level("Classic");
        level1.addBlock(1, 0, 2, 2, "CaoCao", Color.RED);
        level1.addBlock(1, 2, 2, 1, "GuanYu", Color.GREEN);
        level1.addBlock(0, 0, 1, 2, "General", Color.BLUE);
        level1.addBlock(3, 0, 1, 2, "General", Color.BLUE);
        level1.addBlock(0, 2, 1, 2, "General", Color.BLUE);
        level1.addBlock(3, 2, 1, 2, "General", Color.BLUE);
        level1.addBlock(0, 4, 1, 1, "Soldier", Color.ORANGE);
        level1.addBlock(1, 3, 1, 1, "Soldier", Color.ORANGE);
        level1.addBlock(2, 3, 1, 1, "Soldier", Color.ORANGE);
        level1.addBlock(3, 4, 1, 1, "Soldier", Color.ORANGE);
        levels.add(level1);

        // Level 2: Advanced layout
        Level level2 = new Level("Advanced");
        level2.addBlock(1, 0, 2, 2, "CaoCao", Color.RED);
        level2.addBlock(0, 0, 1, 2, "General", Color.BLUE);
        level2.addBlock(3, 0, 1, 2, "General", Color.BLUE);
        level2.addBlock(0, 2, 1, 2, "General", Color.BLUE);
        level2.addBlock(3, 2, 1, 2, "General", Color.BLUE);
        level2.addBlock(1, 2, 1, 1, "Soldier", Color.ORANGE);
        level2.addBlock(2, 2, 1, 1, "Soldier", Color.ORANGE);
        level2.addBlock(1, 3, 2, 1, "GuanYu", Color.GREEN);
        level2.addBlock(0, 4, 1, 1, "Soldier", Color.ORANGE);
        level2.addBlock(3, 4, 1, 1, "Soldier", Color.ORANGE);
        levels.add(level2);

        // Level 3: Expert layout
        Level level3 = new Level("Expert");
        level3.addBlock(1, 0, 2, 2, "CaoCao", Color.RED);
        level3.addBlock(0, 0, 1, 1, "Soldier", Color.ORANGE);
        level3.addBlock(3, 0, 1, 1, "Soldier", Color.ORANGE);
        level3.addBlock(0, 1, 1, 2, "General", Color.BLUE);
        level3.addBlock(3, 1, 1, 2, "General", Color.BLUE);
        level3.addBlock(1, 2, 2, 1, "GuanYu", Color.GREEN);
        level3.addBlock(0, 3, 1, 2, "General", Color.BLUE);
        level3.addBlock(1, 3, 1, 1, "Soldier", Color.ORANGE);
        level3.addBlock(2, 3, 1, 1, "Soldier", Color.ORANGE);
        level3.addBlock(3, 3, 1, 2, "General", Color.BLUE);
        levels.add(level3);
    }

    // Get level by index
    public Level getLevel(int index) {
        if (index < 1 || index > levels.size()) {
            return levels.get(0);  // Default to first level
        }
        return levels.get(index - 1);
    }

    // Get number of levels
    public int getLevelCount() {
        return levels.size();
    }

    // Apply level to board
    public void loadLevel(Board board, int levelIndex) {
        Level level = getLevel(levelIndex);
        level.applyToBoard(board);
    }
}