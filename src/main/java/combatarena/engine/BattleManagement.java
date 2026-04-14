package combatarena.engine;

import combatarena.entities.Character;
import combatarena.entities.Player;
import combatarena.entities.Enemy;
import combatarena.actions.Action;
import combatarena.actions.ActionContext;
import combatarena.util.ActionResult;
import combatarena.effects.StatusEffect;
import combatarena.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BattleManagement {

    private Player player;
    private List<Enemy> enemies;
    private List<Character> turnOrderList;
    private int currentTurnIndex;
    private int roundNumber;
    private TurnOrderStrategy turnOrderStrategy;
    private Level selectedLevel;

    public BattleManagement(Player player, List<Enemy> enemies,
                            TurnOrderStrategy strategy, Level level) {
        this.player = player;
        this.enemies = enemies;
        this.turnOrderStrategy = strategy;
        this.selectedLevel = level;
        this.turnOrderList = new ArrayList<>();
        this.currentTurnIndex = 0;
        this.roundNumber = 1;
    }

    public void startBattle() {
        turnOrderList = determineTurnOrder();

        while (!isBattleOver()) {

            checkAndSpawnBackup();

            Character current = nextTurn();

            current.updateEffects();

            if (current.isStunned()) continue;

            Character target = (current instanceof Enemy) ? player : getFirstAliveEnemy();
            if (target == null) continue;

            List<Character> targets = new ArrayList<>();
            targets.add(target);

            ActionContext context = new ActionContext(current, targets, null);

            executeTurn(context);
        }
    }

    public List<Character> determineTurnOrder() {
        List<Character> all = new ArrayList<>();
        all.add(player);
        all.addAll(enemies);
        return turnOrderStrategy.determineTurnOrder(all);
    }

    public void executeTurn(ActionContext context) {

        if (context == null || context.getTargets().isEmpty()) return;

        Character target = context.getTargets().get(0);

        Action action = context.getUser().getAvailableActions().get(0);

        ActionResult result = action.execute(context);

        applyResult(target, result);
    }

    public void applyResult(Character target, ActionResult result) {

        if (target == null || result == null) return;

        target.takeDamage(result.getDamageGiven());

        if (result.getHealAmount() > 0) {
            target.heal(result.getHealAmount());
        }

        for (StatusEffect effect : result.getEffectsApplied()) {
            target.addEffect(effect);
        }
    }

    public Character nextTurn() {
        if (currentTurnIndex >= turnOrderList.size()) {
            currentTurnIndex = 0;
            roundNumber++;
        }

        Character next = turnOrderList.get(currentTurnIndex);
        currentTurnIndex++;
        return next;
    }

    public boolean isWaveCleared() {
        for (Enemy e : enemies) {
            if (e.isAlive()) return false;
        }
        return true;
    }

    public void checkAndSpawnBackup() {
        if (isWaveCleared() && !selectedLevel.getBackupWave().isEmpty()) {
            enemies.addAll(selectedLevel.getBackupWave());
            turnOrderList = determineTurnOrder();
        }
    }

    public boolean isBattleOver() {
        return !player.isAlive() ||
               (isWaveCleared() && selectedLevel.getBackupWave().isEmpty());
    }

    private Character getFirstAliveEnemy() {
        for (Enemy e : enemies) {
            if (e.isAlive()) return e;
        }
        return null;
    }
}
