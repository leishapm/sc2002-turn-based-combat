package combatarena.util;

import combatarena.effects.Effects;

import java.util.ArrayList;
import java.util.List;

public class ActionResult {

    private int damageGiven;
    private int healAmount;
    private int buffAmount;
    private List<StatusEffect> effectsApplied;
    private List<Character> defeatedTargets;

    public ActionResult(int damage, int heal) {
        this.damageGiven = damage;
        this.healAmount = heal;
        this.buffAmount = 0;
        this.effectsApplied = new ArrayList<>();
        this.defeatedTargets = new ArrayList<>();
    }

    public ActionResult(int damage, int heal, List<StatusEffect> effects) {
        this.damageGiven = damage;
        this.healAmount = heal;
        this.buffAmount = 0;
        this.effectsApplied = (effects != null) ? effects : new ArrayList<>();
        this.defeatedTargets = new ArrayList<>();
    }

    public int getDamageGiven() {
        return damageGiven;
    }

    public int getHealAmount() {
        return healAmount;
    }

    public int getBuffAmount() {
        return buffAmount;
    }

    public List<StatusEffect> getEffectsApplied() {
        return effectsApplied;
    }

    public List<Character> getDefeatedTargets() {
        return defeatedTargets;
    }
}
