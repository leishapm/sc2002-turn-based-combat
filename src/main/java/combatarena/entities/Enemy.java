package combatarena.entities;

import combatarena.engine.EnemyActionStrategy;

public class Enemy extends Character {

    private EnemyActionStrategy actionStrategy;

    public Enemy(int hp, int attack, int defense, int speed,
                 EnemyActionStrategy strategy) {
        super(hp, attack, defense, speed);
        this.actionStrategy = strategy;
    }

    public EnemyActionStrategy getActionStrategy() {
        return actionStrategy;
    }
}
