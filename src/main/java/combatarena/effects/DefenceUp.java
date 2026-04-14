package combatarena.effects;
import combatarena.entities.Character;
public class DefenceUp extends StatusEffect {
    private int bonus = 10;
    public DefenceUp() {
        super("DefenceUp", 2);
    }
    @Override
    public void apply(Character target) {}
    @Override
    public void tick(Character target) {
        decrementDuration();
    }
    @Override
    public void remove(Character target) {}
    public int getDefenceBonus() {
        return bonus;
    }
}
