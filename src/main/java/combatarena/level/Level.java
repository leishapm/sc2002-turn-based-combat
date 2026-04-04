package combatarena.level;

import combatarena.entities.Character;
import combatarena.entities.Enemy;

import java.util.ArrayList;
import java.util.List;

public abstract class Level {

    protected int levelNumber;
    protected String difficulty;
    protected List<Character> startingEnemyPool;
    protected List<Character> backupSpawn;

    public Level(int levelNumber, String difficulty) {
        this.levelNumber = levelNumber;
        this.difficulty = difficulty;
        this.startingEnemyPool = new ArrayList<>();
        this.backupSpawn = new ArrayList<>();
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<Character> getStartingEnemyPool() {
        return startingEnemyPool;
    }

    // returns only enemies from backupSpawn
    public List<Enemy> getBackupWave() {
        List<Enemy> enemies = new ArrayList<>();

        for (Character c : backupSpawn) {
            if (c instanceof Enemy) {
                enemies.add((Enemy) c);
            }
        }

        return enemies;
    }
}
