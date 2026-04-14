package combatarena.actions;

import combatarena.engine.ActionContext;
import combatarena.util.ActionResult;

public abstract class Action {

    protected String name;

    public Action(String name) {
        this.name = name;
    }

    public abstract ActionResult execute(ActionContext context);

    public String info() {
        return name;
    }

    public String getName() {
        return name;
    }
}
