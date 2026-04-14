package combatarena.util;

import combatarena.effects.StatusEffect;
import combatarena.entities.Character;

import java.util.ArrayList;
import java.util.List;

public class ActionResult {

    private int damageGiven;
    private int healAmount;
    private int buffAmount;
    private List<StatusEffect> effectsApplied;
    private List<Character> defeatedTargets;

    public ActionResult() {
        this.effectsApplied = new ArrayList<>();
        this.defeatedTargets = new ArrayList<>();
    }

    public int getDamageGiven() {
        return damageGiven;
    }

    public void setDamageGiven(int damageGiven) {
        this.damageGiven = damageGiven;
    }

    public int getHealAmount() {
        return healAmount;
    }

    public void setHealAmount(int healAmount) {
        this.healAmount = healAmount;
    }

    public int getBuffAmount() {
        return buffAmount;
    }

    public void setBuffAmount(int buffAmount) {
        this.buffAmount = buffAmount;
    }

    public List<StatusEffect> getEffectsApplied() {
        return effectsApplied;
    }

    public void addEffect(StatusEffect effect) {
        this.effectsApplied.add(effect);
    }

    public List<Character> getDefeatedTargets() {
        return defeatedTargets;
    }

    public void addDefeatedTarget(Character target) {
        this.defeatedTargets.add(target);
    }
}
