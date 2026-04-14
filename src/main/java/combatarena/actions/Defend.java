package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.effects.DefenceUp;
import combatarena.util.ActionResult;

public class Defend extends Action {

    @Override
    public ActionResult execute(ActionContext context) {
        ActionResult result = new ActionResult();

        result.addEffect(new DefenceUp(2, 10));

        return result;
    }

    @Override
    public String info() {
        return "Defend";
    }
}
