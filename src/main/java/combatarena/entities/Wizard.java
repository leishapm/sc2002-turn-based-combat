package combatarena.entities;

import combatarena.actions.SpecialSkill;

public class Wizard extends Player {

    public Wizard(SpecialSkill skill) {
        super(200, 50, 10, 20, skill);
    }
}
