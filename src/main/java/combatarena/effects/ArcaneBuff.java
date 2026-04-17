package combatarena.effects;

import combatarena.entities.Character;

public class ArcaneBuff extends StatusEffect {

    private final int bonus = 10;

    public ArcaneBuff() {
 
        super("ArcaneBuff", Integer.MAX_VALUE);
    }

    @Override
    public void apply(Character target) {
        target.increaseAttack(bonus);
    }

    @Override
    public void tick(Character target) {
    
    }

    @Override
    public void remove(Character target) {
        
    }
}
