package combatarena.actions;

import combatarena.actions.items.Item;
import combatarena.engine.ActionContext;
import combatarena.util.ActionResult;

public class UseItemAction extends Action {

    @Override
    public ActionResult execute(ActionContext context) {
        if (context == null || context.getUser() == null || context.getSelectedItem() == null) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        Item item = context.getSelectedItem();

        if (!item.isAvailable()) {
            throw new IllegalStateException("Selected item is not available");
        }

        return item.use(context);
    }

    @Override
    public String info() {
        return "Use Item";
    }
}
