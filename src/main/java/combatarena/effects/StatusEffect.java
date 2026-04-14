package combatarena.effects;

import combatarena.entities.Character;

public abstract class StatusEffect {

    protected int duration;
    protected String typeOfEffect;

    public abstract void apply(Character target);
    public abstract void tick(Character target);
    public abstract void remove(Character target);

    public boolean isExpired() {
        return duration <= 0;
    }

    public int getAttackBonus() { return 0; }
    public int getDefenseBonus() { return 0; }
    public boolean isStun() { return false; }

    public String getTypeOfEffect() {
        return typeOfEffect;
    }
}
