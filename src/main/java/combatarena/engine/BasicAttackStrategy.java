package combatarena.engine;

import combatarena.entities.Enemy;
import combatarena.entities.Character;
import combatarena.actions.Actions;

import java.util.List;

public class BasicAttackStrategy implements EnemyActionStrategy {

    @Override
    public Actions chooseAction(Enemy enemy, List<Character> targets) {

        // simplest logic: just return the first available action
        return enemy.getAvailableActions().get(0);
    }
}
