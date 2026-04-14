package combatarena.util;

import combatarena.effects.Effects;

import java.util.ArrayList;
import java.util.List;

public class ActionResult {

    private int damageGiven;
    private int buffReceived;
    private List<Effects> effectsDone;

    public ActionResult() {
        this.damageGiven = 0;
        this.buffReceived = 0;
        this.effectsDone = new ArrayList<>();
    }

    public ActionResult(int damageGiven) {
        this.damageGiven = damageGiven;
        this.buffReceived = 0;
        this.effectsDone = new ArrayList<>();
    }

    public ActionResult(int damageGiven, int buffReceived) {
        this.damageGiven = damageGiven;
        this.buffReceived = buffReceived;
        this.effectsDone = new ArrayList<>();
    }

    // add effect to result
    public void addEffect(Effects effect) {
        effectsDone.add(effect);
    }

    public int getDamageGiven() {
        return damageGiven;
    }

    public int getBuffReceived() {
        return buffReceived;
    }

    public List<Effects> getEffectsDone() {
        return effectsDone;
    }
}
