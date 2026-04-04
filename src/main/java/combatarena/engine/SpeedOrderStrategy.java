package combatarena.engine;

import combatarena.entities.Character;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpeedOrderStrategy implements TurnOrderStrategy {

    @Override
    public List<Character> determineTurnOrder(List<Character> combatants) {

        // create a copy so original list is not modified
        List<Character> sorted = new ArrayList<>(combatants);

        // sort by speed in descending order (higher speed goes first)
        sorted.sort(Comparator.comparingInt(Character::getSpeed).reversed());

        return sorted;
    }
}
