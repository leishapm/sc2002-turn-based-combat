package combatarena.entities;

import combatarena.effects.Effects;
import combatarena.actions.Actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Character {

    protected int hp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    protected int speed;

    private boolean stunned;

    // store base defense to prevent infinite stacking
    protected int baseDefense;

    protected List<Effects> activeEffects;
    protected List<Actions> availableActions;

    protected int specialSkillCooldown;

    public Character(int hp, int attack, int defense, int speed) {
        this.hp = hp;
        this.maxHp = hp;
        this.attack = attack;
        this.defense = defense;
        this.baseDefense = defense; // store original value
        this.speed = speed;

        this.activeEffects = new ArrayList<>();
        this.availableActions = new ArrayList<>();

        this.specialSkillCooldown = 0;
    }

    // applies all active effects at the start of the character's turn
    public void applyEffects() {
        // reset temporary states before applying effects
        this.stunned = false;

        // reset defense to base before reapplying buffs
        this.defense = baseDefense;

        Iterator<Effects> iterator = activeEffects.iterator();

        while (iterator.hasNext()) {
            Effects effect = iterator.next();

            effect.tick(this);

            if (effect.isExpired()) {
                iterator.remove();
            }
        }
    }

    // adds a new effect to this character
    public void addEffect(Effects effect) {
        activeEffects.add(effect);
    }

    // removes a specific effect if needed
    public void removeEffect(Effects effect) {
        activeEffects.remove(effect);
    }

    // reduces cooldown only when this character actually takes a turn
    public void decrementCooldown() {
        if (specialSkillCooldown > 0) {
            specialSkillCooldown--;
        }
    }

    // sets cooldown when special skill is used
    public void setCooldown(int turns) {
        this.specialSkillCooldown = turns;
    }

    // checks if special skill is available
    public boolean isSkillAvailable() {
        return specialSkillCooldown == 0;
    }

    // handles incoming damage and ensures hp doesn't go below 0
    public void takeDamage(int damage) {
        int finalDamage = Math.max(0, damage - defense);
        hp -= finalDamage;

        if (hp < 0) {
            hp = 0;
        }
    }

    // heals the character but does not exceed max hp
    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    // checks if character is still alive
    public boolean isAlive() {
        return hp > 0;
    }

    // stun handling

    public void setStunned(boolean stunned) {
        this.stunned = stunned;
    }

    public boolean isStunned() {
        return stunned;
    }

    // defense buff support

    public void addDefense(int amount) {
        this.defense += amount;
    }

    // getters

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

    public List<Actions> getAvailableActions() {
        return availableActions;
    }
}
