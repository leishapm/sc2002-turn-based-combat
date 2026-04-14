package combatarena.util;

import combatarena.effects.Effects;

import java.util.ArrayList;
import java.util.List;

public class ActionResult {

    private int damageGiven;
    private int healAmount;
    private List<StatusEffect> effectsApplied;

    public ActionResult(int damage, int heal) {
        this.damageGiven = damage;
        this.healAmount = heal;
        this.effectsApplied = new ArrayList<>();
    }

    public ActionResult(int damage, int heal, List<StatusEffect> effects) {
        this.damageGiven = damage;
        this.healAmount = heal;
        this.effectsApplied = (effects != null) ? effects : new ArrayList<>();
    }

    public int getDamageGiven() {
        return damageGiven;
    }

    public int getHealAmount() {
        return healAmount;
    }

    public List<StatusEffect> getEffectsApplied() {
        return effectsApplied;
    }
}
