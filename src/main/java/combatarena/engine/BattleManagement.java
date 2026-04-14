package combatarena.engine;

import combatarena.actions.Action;
import combatarena.actions.UseItemAction;
import combatarena.entities.Character;
import combatarena.entities.Enemy;
import combatarena.entities.Player;
import combatarena.effects.StatusEffect;
import combatarena.level.Level;
import combatarena.util.ActionResult;

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
            if (current == null || !current.isAlive()) {
                continue;
            }

           current.updateEffects();

           if (current.isStunned()) {
               continue;
            }

            Character target = (current instanceof Enemy) ? player : getFirstAliveEnemy();
            if (target == null) {
                continue;
            }

            List<Character> targets = new ArrayList<>();
            targets.add(target);

            ActionContext context = new ActionContext(current, targets, null);
            executeTurn(context);
        }
    }

    public List<Character> determineTurnOrder() {
        List<Character> all = new ArrayList<>();

        if (player != null && player.isAlive()) {
            all.add(player);
        }

        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy != null && enemy.isAlive()) {
                    all.add(enemy);
                }
            }
        }

        if (turnOrderStrategy == null) {
            return all;
        }

        return turnOrderStrategy.determineTurnOrder(all);
    }

    public void executeTurn(ActionContext context) {
        if (context == null || context.getUser() == null ||
            context.getTargets() == null || context.getTargets().isEmpty()) {
            return;
        }

        Character user = context.getUser();
        Character target = context.getTargets().get(0);

        Action action;

        if (user instanceof Enemy enemy) {
            if (enemy.getActionStrategy() == null) {
                return;
            }
            action = enemy.getActionStrategy().chooseAction(enemy, target);
        } else if (user instanceof Player playerEntity) {
            if (context.getSelectedItem() != null) {
                action = new UseItemAction();
            } else if (playerEntity.getAvailableActions() != null && !playerEntity.getAvailableActions().isEmpty()) {
                action = playerEntity.getAvailableActions().get(0);
            } else {
                return;
            }
        } else {
            return;
        }

        if (action == null) {
            return;
        }

        ActionResult result = action.execute(context);
        applyResult(target, result);
    }

    public void applyResult(Character target, ActionResult result) {
        if (target == null || result == null) {
            return;
        }

        target.takeDamage(result.getDamageGiven());

        if (result.getHealAmount() > 0) {
            target.heal(result.getHealAmount());
        }

        for (StatusEffect effect : result.getEffectsApplied()) {
            target.addEffect(effect);
        }

        if (!target.isAlive()) {
            result.addDefeatedTarget(target);
        }
    }

    public Character nextTurn() {
        if (turnOrderList == null || turnOrderList.isEmpty()) {
            return null;
        }

        if (currentTurnIndex >= turnOrderList.size()) {
            currentTurnIndex = 0;
            roundNumber++;
            turnOrderList = determineTurnOrder();
        }

        return turnOrderList.get(currentTurnIndex++);
    }

    public boolean isWaveCleared() {
        if (enemies == null) {
            return true;
        }

        for (Enemy e : enemies) {
            if (e != null && e.isAlive()) {
                return false;
            }
        }

        return true;
    }

    public void checkAndSpawnBackup() {
        if (selectedLevel == null || enemies == null) {
            return;
        }

        List<Enemy> backupWave = selectedLevel.getBackupWave();

        if (isWaveCleared() &&
            backupWave != null &&
            !backupWave.isEmpty() &&
            !selectedLevel.isBackupSpawnTriggered()) {

            enemies.addAll(backupWave);
            selectedLevel.setBackupSpawnTriggered(true);
            turnOrderList = determineTurnOrder();
        }
    }

    public boolean isBattleOver() {
        if (player == null || !player.isAlive()) {
            return true;
        }

        if (!isWaveCleared()) {
            return false;
        }

        if (selectedLevel == null) {
            return true;
        }

        List<Enemy> backupWave = selectedLevel.getBackupWave();
        boolean hasBackup = backupWave != null && !backupWave.isEmpty();

        return !hasBackup || selectedLevel.isBackupSpawnTriggered();
    }

    private Character getFirstAliveEnemy() {
        if (enemies == null) {
            return null;
        }

        for (Enemy e : enemies) {
            if (e != null && e.isAlive()) {
                return e;
            }
        }

        return null;
    }
}
