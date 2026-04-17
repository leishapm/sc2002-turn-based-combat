package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.util.ActionResult;


public abstract class SpecialSkill extends Action {

    @Override
    public abstract ActionResult execute(ActionContext context);

    public abstract ActionResult executeEffect(ActionContext context);

    @Override
    public String info() {
        return "Special Skill";
    }
}
