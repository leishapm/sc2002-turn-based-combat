package combatarena.effects;

import combatarena.entities.Character;

public class DefenceUp extends StatusEffect {

    private int bonus = 10;

    public DefenseUp() {
        this.duration = 2;
        this.typeOfEffect = "DefenceUp";
    }

    @Override
    public void apply(Character target) {}

    @Override
    public void tick(Character target) {
        duration--;
    }

    @Override
    public void remove(Character target) {}

    @Override
    public int getDefenceBonus() {
        return bonus;
    }
}
