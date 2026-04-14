package combatarena.engine;

import combatarena.entities.*;
import combatarena.actions.Action;
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
            if (current == null || !current.isAlive()) continue;

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

        Character user = context.getUser();
        Character target = context.getTargets().get(0);

        Action action;

        if (user instanceof Enemy enemy) {
            action = enemy.getActionStrategy()
                          .chooseAction(enemy, target);
        } else {
            action = user.getAvailableActions().get(0);
        }

        if (action == null) return;

        ActionResult result = action.execute(context);

        applyResult(target, result);
    }

    public void applyResult(Character target, ActionResult result) {

        if (target == null || result == null) return;

        // Damage
        target.takeDamage(result.getDamageGiven());

        // Heal
        if (result.getHealAmount() > 0) {
            target.heal(result.getHealAmount());
        }

        // Effects
        for (StatusEffect effect : result.getEffectsApplied()) {
            effect.apply(target); // APPLY ONCE
            target.addEffect(effect);
        }

        // Defeat tracking
        if (!target.isAlive()) {
            result.addDefeatedTarget(target);
        }
    }

    public Character nextTurn() {
        if (turnOrderList.isEmpty()) return null;

        if (currentTurnIndex >= turnOrderList.size()) {
            currentTurnIndex = 0;
            roundNumber++;
        }

        return turnOrderList.get(currentTurnIndex++);
    }

    public boolean isWaveCleared() {
        return enemies.stream().noneMatch(Enemy::isAlive);
    }

    public void checkAndSpawnBackup() {

        if (isWaveCleared() &&
            !selectedLevel.getBackupWave().isEmpty() &&
            !selectedLevel.isBackupSpawnTriggered()) {

            enemies.addAll(selectedLevel.getBackupWave());
            selectedLevel.setBackupSpawnTriggered(true);
            turnOrderList = determineTurnOrder();
        }
    }

    public boolean isBattleOver() {
        return !player.isAlive() ||
               (isWaveCleared() && selectedLevel.getBackupWave().isEmpty());
    }

    private Character getFirstAliveEnemy() {
        return enemies.stream().filter(Enemy::isAlive).findFirst().orElse(null);
    }
}
