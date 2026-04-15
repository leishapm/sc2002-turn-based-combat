package combatarena.actions.items;

import combatarena.engine.ActionContext;
import combatarena.entities.Player;
import combatarena.util.ActionResult;

public class PowerStone extends Item {

    @Override
    public ActionResult use(ActionContext context) {
        if (context == null || context.getUser() == null) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        if (!(context.getUser() instanceof Player player)) {
            throw new IllegalStateException("PowerStone can only be used by a Player");
        }

        if (player.getSpecialSkill() == null) {
            throw new IllegalStateException("Player has no special skill");
        }

        ActionResult result = player.getSpecialSkill().executeEffect(context);
        consume();
        return result;
    }
    @Override
    public Item copy() {
        PowerStone p = new PowerStone();
        p.setQuantity(this.getQuantity());
        return p;
    }
}
