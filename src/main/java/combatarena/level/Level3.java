package combatarena.level;

import combatarena.entities.Enemy;

import java.util.List;

public class Level3 extends Level {

    public Level3(List<Enemy> startingEnemyPool, List<Enemy> backupSpawn) {
        this.levelNumber = 3;
        this.difficulty = "Hard";
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
