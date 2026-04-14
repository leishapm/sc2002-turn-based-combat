package combatarena.actions.items;

import combatarena.engine.ActionContext;
import combatarena.effects.SmokeBombInvulnerability;
import combatarena.util.ActionResult;

public class SmokeBomb extends Item {

    @Override
    public ActionResult use(ActionContext context) {
        if (context == null || context.getUser() == null) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        ActionResult result = new ActionResult();
        result.addEffect(new SmokeBombInvulnerability(2));
        consume();
        return result;
    }
}
