package combatarena.effects;

import combatarena.entities.Character;

public class DefenceUp extends StatusEffect {

    private int bonus = 10;
    private boolean applied = false;

    public DefenceUp() {
        super("DefenceUp", 2);
    }

    @Override
    public void apply(Character target) {
        if (!applied) {
            target.increaseDefense(bonus);
            applied = true;
        }
    }

    @Override
    public void tick(Character target) {
        decrementDuration();
    }

    @Override
    public void remove(Character target) {
        target.decreaseDefense(bonus);
    }

    public int getDefenseBonus() {
        return bonus;
    }
}
