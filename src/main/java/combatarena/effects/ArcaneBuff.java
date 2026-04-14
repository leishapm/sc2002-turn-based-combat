package combatarena.effects;

import combatarena.entities.Character;

public class ArcaneBuff extends StatusEffect {

    private final int bonus = 10;

    public ArcaneBuff() {
        super("ArcaneBuff", 2);
    }

    @Override
    public void apply(Character target) {
        target.increaseAttack(bonus);
    }

    @Override
    public void tick(Character target) {
        decrementDuration();
    }

    @Override
    public void remove(Character target) {
        target.decreaseAttack(bonus);
    }
}
