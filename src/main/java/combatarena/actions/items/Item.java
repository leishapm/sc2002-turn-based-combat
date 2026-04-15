package combatarena.actions.items;

import combatarena.engine.ActionContext;
import combatarena.util.ActionResult;

public abstract class Item {

    protected int quantity = 1;

    public abstract ActionResult use(ActionContext context);

    public boolean requiresTarget() {
        return false;
    }

    public void consume() {
        if (quantity > 0) {
            quantity--;
        }
    }

    public boolean isAvailable() {
        return quantity > 0;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }


    public void addQuantity(int amount) {
        if (amount > 0) {
            this.quantity += amount;
        }
    }
}
