package combatarena.entities;

import combatarena.engine.EnemyActionStrategy;

public class Enemy extends Character {

    private EnemyActionStrategy actionStrategy;
    private String name;

    public Enemy(int hp, int attack, int defense, int speed,
                 EnemyActionStrategy strategy) {
        this(hp, attack, defense, speed, strategy, null);
    }

    public Enemy(int hp, int attack, int defense, int speed,
                 EnemyActionStrategy strategy, String name) {
        super(hp, attack, defense, speed);
        this.actionStrategy = strategy;
        this.name = name;
    }

    public EnemyActionStrategy getActionStrategy() {
        return actionStrategy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        if (name == null || name.isBlank()) {
            return getClass().getSimpleName();
        }
        return name;
    }
}
