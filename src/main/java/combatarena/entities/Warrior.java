package combatarena.entities;

import combatarena.actions.SpecialSkill;

public class Warrior extends Player {

    public Warrior(SpecialSkill skill) {
        super(260, 40, 20, 30, skill);
    }
}
