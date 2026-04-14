package combatarena.effects;

import combatarena.entities.Character;

public class Stun extends StatusEffect {

    public Stun(int duration) {
        super("Stun", duration);
    }

    @Override
    public void apply(Character target) {
        target.setStunned(true);
    }

    @Override
    public void tick(Character target) {
        decrementDuration();
    }

    @Override
    public void remove(Character target) {
        target.setStunned(false);
    }
}
