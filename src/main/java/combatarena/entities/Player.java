package combatarena.entities;

import combatarena.actions.Action;
import combatarena.actions.BasicAttack;
import combatarena.actions.UseItemAction;
import combatarena.actions.items.Item;
import combatarena.actions.SpecialSkill;

import java.util.ArrayList;
import java.util.List;

public class Player extends Character {

    private List<Item> inventory;
    private SpecialSkill specialSkill;
    private int specialSkillCd;

    public Player(int hp, int attack, int defense, int speed, SpecialSkill skill) {
        super(hp, attack, defense, speed);
        this.inventory = new ArrayList<>();
        this.specialSkill = skill;
        this.specialSkillCd = 0;
    }

    public List<Action> getAvailableActions() {
        List<Action> actions = new ArrayList<>();
        actions.add(new BasicAttack());
        actions.add(new UseItemAction());
        return actions;
    }

    public boolean isSkillAvailable() {
        return specialSkillCd == 0;
    }

    public int getSpecialSkillCd() {
        return specialSkillCd;
    }

    public void setSpecialSkillCd(int specialSkillCd) {
        this.specialSkillCd = Math.max(0, specialSkillCd);
    }

    public void decrementCooldown() {
        if (specialSkillCd > 0) {
            specialSkillCd--;
        }
    }

    public void reset() {
        this.hp = this.maxHp;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public void setInventory(List<Item> items) {
        if (items == null) {
            this.inventory = new ArrayList<>();
        } else {
            this.inventory = new ArrayList<>(items);
        }
    }

    public SpecialSkill getSpecialSkill() {
        return specialSkill;
    }
}
