package combatarena.engine;

import combatarena.entities.Enemy;
import combatarena.entities.Character;
import combatarena.actions.Action;

import java.util.List;

public class BasicAttackStrategy implements EnemyActionStrategy {

    @Override
    public Action chooseAction(Enemy enemy, Character target) {

        List<Action> actions = enemy.getAvailableActions();

        if (actions == null || actions.isEmpty()) {
            return null;
        }

        return actions.get(0);
    }
}
