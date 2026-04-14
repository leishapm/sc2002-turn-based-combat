package combatarena.engine;

import combatarena.entities.Enemy;
import combatarena.entities.Character;
import combatarena.actions.Action;

public class BasicAttackStrategy implements EnemyActionStrategy {

    @Override
    public Action chooseAction(Enemy enemy, Character target) {

        // simplest logic: return first available action
        return enemy.getAvailableActions().get(0);
    }
}
