package combatarena.effects;

import combatarena.entities.Character;

public abstract class StatusEffect {

    protected int duration;

    public StatusEffect(int duration) {
        this.duration = duration;
    }

    // applies the effect and reduces duration each turn
    public void tick(Character character) {
        applyEffect(character);
        duration--;
    }

    // each effect defines its own behaviour
    protected abstract void applyEffect(Character character);

    // checks if the effect has expired
    public boolean isExpired() {
        return duration <= 0;
    }
}
