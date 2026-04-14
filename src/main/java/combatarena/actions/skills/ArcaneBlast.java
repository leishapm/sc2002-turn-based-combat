package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.effects.ArcaneBuff;
import combatarena.util.ActionResult;

public class ArcaneBlast extends Action {

    @Override
    public ActionResult execute(ActionContext context) {
        ActionResult result = new ActionResult();
        result.setDamageGiven(30);

        result.addEffect(new ArcaneBuff());

        return result;
    }

    public ActionResult executeEffect(ActionContext context) {
        return execute(context);
    }

    @Override
    public String info() {
        return "Arcane Blast";
    }
}
