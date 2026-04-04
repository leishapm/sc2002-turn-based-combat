package combatarena.engine;

import combatarena.entities.Character;

import java.util.List;

public interface TurnOrderStrategy {

    // determines the order of turns based on some logic (e.g. speed)
    List<Character> determineTurnOrder(List<Character> combatants);
}
