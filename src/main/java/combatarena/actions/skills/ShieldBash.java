package combatarena.actions.skills;

import combatarena.actions.Action;
import combatarena.engine.ActionContext;
import combatarena.effects.Stun;
import combatarena.util.ActionResult;

public class ShieldBash extends Action {

    @Override
    public ActionResult execute(ActionContext context) {
        if (context == null || context.getUser() == null || context.getTargets() == null || context.getTargets().isEmpty()) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        ActionResult result = new ActionResult();
        result.setDamageGiven(context.getUser().getAttack());
        result.addEffect(new Stun(3));
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
