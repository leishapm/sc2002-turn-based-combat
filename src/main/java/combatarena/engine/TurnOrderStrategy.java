package combatarena.engine;

import combatarena.entities.Character;

import java.util.List;

public interface TurnOrderStrategy {
    List<Character> determineTurnOrder(List<Character> combatants);
}
