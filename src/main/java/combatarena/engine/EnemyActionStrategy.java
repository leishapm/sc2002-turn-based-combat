package combatarena.engine;

import combatarena.entities.Enemy;
import combatarena.entities.Character;
import combatarena.actions.Action;

import java.util.List;

public interface EnemyActionStrategy {

    Actions chooseAction(Enemy enemy, List<Character> targets);
}
