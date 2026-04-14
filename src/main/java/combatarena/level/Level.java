package combatarena.level;

import combatarena.entities.Enemy;

import java.util.List;

public abstract class Level {

    protected int levelNumber;
    protected String difficulty;
    protected List<Enemy> startingEnemyPool;
    protected List<Enemy> backupSpawn;
    protected boolean backupSpawnTriggered;

    public abstract List<Enemy> getInitialWave();

    public abstract List<Enemy> getBackupWave();

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public boolean isBackupSpawnTriggered() {
        return backupSpawnTriggered;
    }

    public void setBackupSpawnTriggered(boolean backupSpawnTriggered) {
        this.backupSpawnTriggered = backupSpawnTriggered;
    }

    public List<Enemy> getStartingEnemyPool() {
        return startingEnemyPool;
    }

    public List<Enemy> getBackupSpawn() {
        return backupSpawn;
    }
}
