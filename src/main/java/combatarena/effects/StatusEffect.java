package combatarena.effects;

import combatarena.entities.Character;

public abstract class StatusEffect {

    protected String typeOfEffect;
    protected int duration;

    public StatusEffect(String typeOfEffect, int duration) {
        this.typeOfEffect = typeOfEffect;
        this.duration = duration;
    }

    public abstract void apply(Character target);

    public abstract void tick(Character target);

    public abstract void remove(Character target);

    public void decrementDuration() {
        duration--;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    public int getDuration() {
        return duration;
    }

    public String getTypeOfEffect() {
        return typeOfEffect;
    }
}
