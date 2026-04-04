package combatarena.actions;

import combatarena.entities.Character;

public abstract class Actions {

    protected String name;

    public Actions(String name) {
        this.name = name;
    }

    // executes the action and returns the result (damage, effects, etc.)
    public abstract SkillsResult execute(Character user, Character target);

    public String getName() {
        return name;
    }
}
