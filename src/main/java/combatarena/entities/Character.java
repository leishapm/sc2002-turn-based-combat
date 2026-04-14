package combatarena.entities;

import combatarena.effects.StatusEffect;

import java.util.ArrayList;
import java.util.List;

public abstract class Character {

    protected int hp;
    protected int maxHp;

    protected int baseAttack;
    protected int baseDefense;
    protected int speed;

    protected List<StatusEffect> activeEffects = new ArrayList<>();

    public boolean isAlive() {
        return hp > 0;
    }

    public int getAttack() {
        int bonus = 0;
        for (StatusEffect e : activeEffects) {
            bonus += e.getAttackBonus();
        }
        return baseAttack + bonus;
    }

    public int getDefense() {
        int bonus = 0;
        for (StatusEffect e : activeEffects) {
            bonus += e.getDefenseBonus();
        }
        return baseDefense + bonus;
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    public void heal(int amount) {
        hp += amount;
        if (hp > maxHp) hp = maxHp;
    }

    public void addEffect(StatusEffect effect) {
        effect.apply(this);
        activeEffects.add(effect);
    }

    public void applyEffects() {
        List<StatusEffect> toRemove = new ArrayList<>();

        for (StatusEffect e : activeEffects) {
            e.tick(this);
            if (e.isExpired()) {
                e.remove(this);
                toRemove.add(e);
            }
        }

        activeEffects.removeAll(toRemove);
    }

    public boolean isStunned() {
        for (StatusEffect e : activeEffects) {
            if (e.isStun()) return true;
        }
        return false;
    }
}
