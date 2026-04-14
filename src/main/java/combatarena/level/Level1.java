package combatarena.level;

import combatarena.entities.Enemy;

import java.util.List;

public class Level1 extends Level {

    public Level1(List<Enemy> enemies, List<Enemy> backup) {
        this.levelNumber = 1;
        this.difficulty = "Easy";
        this.startingEnemyPool = enemies;
        this.backupSpawn = backup;
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
