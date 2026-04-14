package combatarena.effects;

import combatarena.entities.Character;

public class DefenceUp extends StatusEffect {

    private final int amount;

    public DefenceUp(int duration, int amount) {
        super("DefenceUp", duration);
        this.amount = amount;
    }

    @Override
    public void apply(Character target) {
        target.increaseDefense(amount);
    }

    @Override
    public void tick(Character target) {
        decrementDuration();
    }

    @Override
    public void remove(Character target) {
        target.decreaseDefense(amount);
    }
}
