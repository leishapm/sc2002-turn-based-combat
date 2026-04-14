package combatarena.effects;

import combatarena.entities.Character;

public class DefenceUp extends StatusEffect {

    private int amount;
    private boolean applied;

    public DefenceUp(int duration, int amount) {
        super("DefenceUp", duration);
        this.amount = amount;
        this.applied = false;
    }

    @Override
    public void apply(Character target) {
        // Apply only once
        if (!applied) {
            target.increaseDefense(amount);
            applied = true;
        }
    }

    @Override
    public void tick(Character target) {
        // Reduce duration each turn
        decrementDuration();
    }

    @Override
    public void remove(Character target) {
        // Revert the defense increase
        target.decreaseDefense(amount);
    }
}
