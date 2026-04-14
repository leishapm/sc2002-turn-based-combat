package combatarena.engine;

import combatarena.actions.items.Item;
import combatarena.entities.Character;

import java.util.List;

public class ActionContext {

    private Character user;
    private List<Character> targets;
    private Item selectedItem;

    public ActionContext(Character user, List<Character> targets, Item selectedItem) {
        this.user = user;
        this.targets = targets;
        this.selectedItem = selectedItem;
    }

    public Character getUser() {
        return user;
    }

    public List<Character> getTargets() {
        return targets;
    }

    public Item getSelectedItem() {
        return selectedItem;
    }
}
