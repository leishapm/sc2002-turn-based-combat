package combatarena.effects;

import combatarena.entities.Character;

public class DefenceUp extends StatusEffect {

    private int bonus;

    public DefenceUp(int duration, int bonus) {
        super("DefenceUp", duration);
        this.bonus = bonus;
    }

    @Override
    public void apply(Character target) {
        target.increaseDefense(bonus);
    }

    @Override
    public void tick(Character target) {
        decrementDuration();
    }

    @Override
    public void remove(Character target) {
        target.decreaseDefense(bonus);
    }

    public int getBonus() {
        return bonus;
    }
}
