package combatarena.level;

import combatarena.entities.Enemy;

import java.util.List;

public class Level2 extends Level {

    public Level2(List<Enemy> startingEnemyPool, List<Enemy> backupSpawn) {
        this.levelNumber = 2;
        this.difficulty = "Medium";
        this.startingEnemyPool = startingEnemyPool;
        this.backupSpawn = backupSpawn;
        this.backupSpawnTriggered = false;
    }

    @Override
    public List<Enemy> getInitialWave() {
        return startingEnemyPool;
    }

    @Override
    public List<Enemy> getBackupWave() {
        return backupSpawn;
    }
}
