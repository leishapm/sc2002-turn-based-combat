package combatarena.actions.items;

import combatarena.engine.ActionContext;
import combatarena.util.ActionResult;

public class Potion extends Item {

    @Override
    public ActionResult use(ActionContext context) {
        if (context == null || context.getUser() == null) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        ActionResult result = new ActionResult();
        result.setHealAmount(100);
        consume();
        return result;
    }
}
