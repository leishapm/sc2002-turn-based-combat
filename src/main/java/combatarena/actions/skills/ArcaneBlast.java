package combatarena.actions.skills;

import combatarena.actions.SpecialSkill;
import combatarena.effects.ArcaneBuff;
import combatarena.engine.ActionContext;
import combatarena.entities.Character;
import combatarena.util.ActionResult;

public class ArcaneBlast extends SpecialSkill {

    @Override
    public ActionResult execute(ActionContext context) {
        return executeEffect(context);
    }

    @Override
    public ActionResult executeEffect(ActionContext context) {
        if (context == null || context.getUser() == null) {
            throw new IllegalArgumentException("Invalid ActionContext");
        }

        ActionResult result = new ActionResult();
        int totalDamage = 0;
        int kills = 0;

        if (context.getTargets() != null) {
            for (Character target : context.getTargets()) {
                if (target != null && target.isAlive()) {
                    int rawDamage = context.getUser().getAttack();
                    int mitigatedDamage = Math.max(0, rawDamage - target.getDefense());
                    totalDamage += rawDamage;
                    if (target.getHp() - mitigatedDamage <= 0) {
                        kills++;
                    }
                }
            }
        }

        result.setDamageGiven(totalDamage);
        for (int i = 0; i < kills; i++) {
            result.addEffect(new ArcaneBuff());
        }

        return result;
    }

    @Override
    public String info() {
        return "Arcane Blast";
    }
}
