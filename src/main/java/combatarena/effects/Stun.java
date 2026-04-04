package combatarena.effects;

import combatarena.entities.Character;

public class Stun extends Effects {

    public Stun(int duration) {
        super(duration);
    }

    @Override
    protected void applyEffect(Character character) {
        // set stunned to true so character skips turn
        character.setStunned(true);
    }
}
