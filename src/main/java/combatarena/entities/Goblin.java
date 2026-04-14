package combatarena.entities;

import combatarena.engine.EnemyActionStrategy;

public class Goblin extends Enemy {

    public Goblin(EnemyActionStrategy strategy) {
        super(55, 35, 15, 25, strategy);
    }
}
