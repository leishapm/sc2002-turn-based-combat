package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.entities.Character;
import combatarena.util.ActionResult;

public class BasicAttack extends Action {

    @Override
    public ActionResult execute(ActionContext context) {
        if (context == null || context.getUser() == null || context.getTarget() == null) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        Character attacker = context.getUser();
        Character target = context.getTarget();

        int damage = attacker.getAttack() - target.getDefense();
        damage = Math.max(1, damage);

        return new ActionResult(damage, 0);
    }

    @Override
    public String info() {
        return "Basic Attack";
    }
}
