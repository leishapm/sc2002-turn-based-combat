package combatarena.actions.skills;

import combatarena.actions.SpecialSkill;
import combatarena.engine.ActionContext;
import combatarena.effects.Stun;
import combatarena.util.ActionResult;

public class ShieldBash extends SpecialSkill {

    @Override
    public ActionResult execute(ActionContext context) {
        return executeEffect(context);
    }

    @Override
    public ActionResult executeEffect(ActionContext context) {
        if (context == null || context.getUser() == null
                || context.getTargets() == null || context.getTargets().isEmpty()) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        ActionResult result = new ActionResult();
        result.setDamageGiven(context.getUser().getAttack());
        result.addEffect(new Stun(2));
        return result;
    }

    @Override
    public String info() {
        return "Shield Bash";
    }
}
