package combatarena.engine;

import combatarena.actions.Action;
import combatarena.actions.BasicAttack;
import combatarena.entities.Character;
import combatarena.entities.Enemy;

public class BasicAttackStrategy implements EnemyActionStrategy {

    @Override
    public Action chooseAction(Enemy enemy, Character target) {
        return new BasicAttack();
    }
}
