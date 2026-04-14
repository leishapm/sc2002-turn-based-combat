package combatarena.entities;

import combatarena.effects.StatusEffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Character {

    protected int hp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    protected int speed;

    protected List<StatusEffect> activeEffects;

    private boolean stunned;

    public Character(int hp, int attack, int defense, int speed) {
        this.maxHp = hp;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.activeEffects = new ArrayList<>();
        this.stunned = false;
    }

 
    public void addEffect(StatusEffect effect) {
        if (effect == null) return;

        effect.apply(this); // apply once
        activeEffects.add(effect);
    }

    public void updateEffects() {

        Iterator<StatusEffect> iterator = activeEffects.iterator();

        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();

            effect.tick(this);

            if (effect.isExpired()) {
                effect.remove(this);
                iterator.remove();
            }
        }
    }

    public void removeEffect(StatusEffect effect) {
        if (effect == null) return;

        effect.remove(this);
        activeEffects.remove(effect);
    }

   
    public void takeDamage(int damage) {
        int finalDamage = Math.max(0, damage - defense);
        hp -= finalDamage;

        if (hp < 0) {
            hp = 0;
        }
    }

    public void heal(int amount) {
        if (amount <= 0) return;

        hp += amount;

        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

 
    public boolean isStunned() {
        return stunned;
    }

    public void setStunned(boolean stunned) {
        this.stunned = stunned;
    }


    public void increaseDefense(int amount) {
        defense += amount;
    }

    public void decreaseDefense(int amount) {
        defense = Math.max(0, defense - amount);
    }

    public void increaseAttack(int amount) {
        attack += amount;
    }

    public void decreaseAttack(int amount) {
        attack = Math.max(0, attack - amount);
    }


    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }

    public List<StatusEffect> getActiveEffects() {
        return activeEffects;
    }

 
    public String getStatusSummary() {
        return "HP: " + hp + "/" + maxHp +
               " | ATK: " + attack +
               " | DEF: " + defense +
               " | SPD: " + speed +
               " | Effects: " + activeEffects.size();
    }
}
