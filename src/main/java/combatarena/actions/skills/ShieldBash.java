package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.effects.Stun;
import combatarena.util.ActionResult;

public class ShieldBash extends Action {

    @Override
    public ActionResult execute(ActionContext context) {
        ActionResult result = new ActionResult();
        result.setDamageGiven(20);

        result.addEffect(new Stun(1));

        return result;
    }

    public ActionResult executeEffect(ActionContext context) {
        return execute(context);
    }

    @Override
    public String info() {
        return "Shield Bash";
    }
}
