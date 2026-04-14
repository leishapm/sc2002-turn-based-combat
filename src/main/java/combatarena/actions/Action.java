package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.util.ActionResult;

public abstract class Action {
    public abstract ActionResult execute(ActionContext context);
    public abstract String info();
}
