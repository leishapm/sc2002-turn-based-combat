package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.entities.Character;
import combatarena.util.ActionResult;

public class BasicAttack extends Action {

    @Override
    public ActionResult execute(ActionContext context) {
        if (context == null || context.getUser() == null ||
            context.getTargets() == null || context.getTargets().isEmpty()) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        Character attacker = context.getUser();
        Character target = context.getTargets().get(0);

        int damage = attacker.getAttack();

        ActionResult result = new ActionResult();
        result.setDamageGiven(damage);

        return result;
    }

    @Override
    public String info() {
        return "Basic Attack";
    }
}
