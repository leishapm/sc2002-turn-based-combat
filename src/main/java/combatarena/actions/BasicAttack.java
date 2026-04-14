package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.entities.Character;
import combatarena.util.ActionResult;

public class BasicAttack extends Action {

    @Override
    public ActionResult execute(ActionContext context) {

        // defensive check
        if (context == null || context.getUser() == null || context.getTarget() == null) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        Character attacker = context.getUser();
        Character target = context.getTarget();

        // basic damage calculation with minimum damage = 1
        int damage = attacker.getAttack() - target.getDefense();
        damage = Math.max(1, damage);

        return new ActionResult(damage, 0);
    }
}
