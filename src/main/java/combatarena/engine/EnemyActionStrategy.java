package combatarena.engine;

import combatarena.entities.Enemy;
import combatarena.entities.Character;
import combatarena.actions.Action;

public interface EnemyActionStrategy {
    Action chooseAction(Enemy enemy, Character target);
}
