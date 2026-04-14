package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.util.ActionResult;

public abstract class SpecialSkill {
    public abstract ActionResult execute(ActionContext context);
    public abstract ActionResult executeEffect(ActionContext context);
}
