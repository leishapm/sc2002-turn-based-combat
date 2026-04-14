package combatarena.entities;

import combatarena.effects.StatusEffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Character {

    protected int hp;
    protected int attack;
    protected int defense;
    protected int speed;
    protected int maxHp;
    protected List<StatusEffect> activeEffects;

    public Character(int hp, int attack, int defense, int speed) {
        this.hp = hp;
        this.maxHp = hp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.activeEffects = new ArrayList<>();
    }

    public void updateEffects() {

        Iterator<StatusEffect> iterator = activeEffects.iterator();

        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();

            effect.apply(this);
            effect.decrementDuration();

            if (effect.isExpired()) {
                iterator.remove();
            }
        }
    }

    public void addEffect(StatusEffect effect) {
        activeEffects.add(effect);
    }

    public void removeEffect(StatusEffect effect) {
        activeEffects.remove(effect);
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeDamage(int dmg) {

        int reducedDamage = Math.max(0, dmg - defense);
        hp -= reducedDamage;

        if (hp < 0) {
            hp = 0;
        }
    }

    public void heal(int amount) {

        hp += amount;

        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    public int getSpeed() {
        return speed;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getHp() {
        return hp;
    }

    public List<StatusEffect> getActiveEffects() {
        return activeEffects;
    }
}
