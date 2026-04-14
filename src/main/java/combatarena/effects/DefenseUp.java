package combatarena.effects;

import combatarena.entities.Character;

public class DefenseUp extends Effects {

    private int bonus;

    public DefenseUp(int duration, int bonus) {
        super(duration);
        this.bonus = bonus;
    }

    @Override
    protected void applyEffect(Character character) {
        // temporarily increase defense
        // NOTE: this stacks every turn unless handled carefully
        character.addDefense(bonus);
    }
}
