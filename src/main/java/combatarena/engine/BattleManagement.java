package combatarena.engine;

import combatarena.entities.Character;
import combatarena.entities.Player;
import combatarena.entities.Enemy;
import combatarena.actions.Action;
import combatarena.util.ActionResult;
import combatarena.effects.Effects;
import combatarena.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BattleManagement {

    private List<Player> players;
    private List<Enemy> enemies;
    private TurnOrderStrategy turnOrderStrategy;
    private Level selectedLevel;

    private List<Character> turnOrderList;
    private int currentTurnIndex;

    public BattleManagement(List<Player> players, List<Enemy> enemies,
                            TurnOrderStrategy strategy, Level level) {
        this.players = players;
        this.enemies = enemies;
        this.turnOrderStrategy = strategy;
        this.selectedLevel = level;

        this.turnOrderList = new ArrayList<>();
        this.currentTurnIndex = 0;

        initializeTurnOrder();
    }

    // initializes turn order using strategy
    private void initializeTurnOrder() {
        List<Character> allCharacters = new ArrayList<>();
        allCharacters.addAll(players);
        allCharacters.addAll(enemies);

        turnOrderList = turnOrderStrategy.determineTurnOrder(allCharacters);
    }

    // main battle loop
    public void startBattle() {
        while (!isWinCleared() && playersAlive()) {

            checkAndSpawnBackup();

            Character current = nextTurn();

            // apply effects at start of turn
            current.applyEffects();

            // skip turn if stunned
            if (current.isStunned()) {
                continue;
            }

            // reduce cooldown only if character gets to act
            current.decrementCooldown();

            // minimal safe execution (no dependency on teammate code)
            Action action = current.getAvailableActions().get(0);
            Character target;

            if (current instanceof Enemy) {
                target = players.get(0);
            } else {
                target = enemies.get(0);
            }

            executeTurn(current, action, target);
        }
    }

    // executes an action and applies result
    public void executeTurn(Character attacker, Action action, Character target) {
        ActionResult result = action.execute(attacker, target);
        applyResult(target, result);
    }

    // applies damage, buffs, and effects
    private void applyResult(Character target, SkillsResult result) {

        target.takeDamage(result.getDamageGiven());

        if (result.getBuffReceived() > 0) {
            target.heal(result.getBuffReceived());
        }

        for (Effects effect : result.getEffectsDone()) {
            target.addEffect(effect);
        }
    }

    // returns next character in turn order
    public Character nextTurn() {
        if (currentTurnIndex >= turnOrderList.size()) {
            currentTurnIndex = 0;
        }

        Character next = turnOrderList.get(currentTurnIndex);
        currentTurnIndex++;
        return next;
    }

    // checks if a character is defeated
    public boolean isDefeated(Character character) {
        return !character.isAlive();
    }

    // checks if all enemies are defeated
    public boolean isWinCleared() {
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                return false;
            }
        }
        return true;
    }

    // checks if at least one player is alive
    private boolean playersAlive() {
        for (Player player : players) {
            if (player.isAlive()) {
                return true;
            }
        }
        return false;
    }

    // spawns backup enemies when all current enemies are defeated
    public void checkAndSpawnBackup() {
        if (!selectedLevel.getBackupWave().isEmpty() && enemiesAliveCount() == 0) {
            enemies.addAll(selectedLevel.getBackupWave());
            initializeTurnOrder();
        }
    }

    // helper to count alive enemies
    private int enemiesAliveCount() {
        int count = 0;
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                count++;
            }
        }
        return count;
    }
}
