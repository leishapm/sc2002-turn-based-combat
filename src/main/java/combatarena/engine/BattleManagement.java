package combatarena.engine;

import combatarena.actions.Action;
import combatarena.actions.BasicAttack;
import combatarena.actions.UseItemAction;
import combatarena.actions.items.Item;
import combatarena.entities.Character;
import combatarena.entities.Enemy;
import combatarena.entities.Player;
import combatarena.effects.StatusEffect;
import combatarena.level.Level;
import combatarena.util.ActionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BattleManagement {

    private Player player;
    private List<Enemy> enemies;
    private List<Character> turnOrderList;
    private int currentTurnIndex;
    private int roundNumber;
    private TurnOrderStrategy turnOrderStrategy;
    private Level selectedLevel;
    private Scanner scanner;

    public BattleManagement(Player player, List<Enemy> enemies,
                            TurnOrderStrategy strategy, Level level,
                            Scanner scanner) {
        this.player = player;
        this.enemies = enemies;
        this.turnOrderStrategy = strategy;
        this.selectedLevel = level;
        this.turnOrderList = new ArrayList<>();
        this.currentTurnIndex = 0;
        this.roundNumber = 1;
        this.scanner = scanner != null ? scanner : new Scanner(System.in);
    }

    public void startBattle() {
        turnOrderList = determineTurnOrder();
        System.out.println("=== Battle Start ===");
        printRoundHeader();

        while (!isBattleOver()) {
            checkAndSpawnBackup();

            Character current = nextTurn();
            if (current == null || !current.isAlive()) {
                continue;
            }

            current.updateEffects();

            if (current.isStunned()) {
                System.out.println(current.getClass().getSimpleName() + " is stunned and skips the turn.");
                continue;
            }

            if (current instanceof Player playerEntity) {
                executePlayerTurn(playerEntity);
            } else {
                if (player == null || !player.isAlive()) {
                    continue;
                }

                List<Character> targets = new ArrayList<>();
                targets.add(player);

                ActionContext context = new ActionContext(current, targets, null);
                executeTurn(context);
            }
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
        } else {
            return;
        }

        if (action == null) {
            return;
        }

        ActionResult result = action.execute(context);
        applyResult(target, result);
        printActionOutcome(action.info(), user, target, result);
    }

    public void applyResult(Character target, ActionResult result) {
        if (target == null || result == null) {
            return;
        }

        int actualDamage = target.takeDamage(result.getDamageGiven());
        result.setDamageGiven(actualDamage);

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
            printRoundHeader();
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

    private void executePlayerTurn(Player player) {
        List<Enemy> aliveEnemies = getAliveEnemies();
        if (aliveEnemies.isEmpty()) {
            return;
        }

        System.out.println();
        System.out.println("Player HP: " + player.getHp());
        System.out.println("Enemies alive: " + aliveEnemies.size());

        while (true) {
            System.out.println();
            System.out.println("Choose your action:");
            System.out.println("1. Basic Attack");
            System.out.println("2. Use Item");
            System.out.println("3. Special Skill" + (player.isSkillAvailable() ? "" : " (cooldown " + player.getSpecialSkillCd() + ")"));
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();
            Character target;
            ActionResult result;

            if ("1".equals(input)) {
                target = chooseTarget(aliveEnemies);
                Action action = new BasicAttack();
                result = action.execute(new ActionContext(player, List.of(target), null));
                applyResult(target, result);
                printActionOutcome("Basic Attack", player, target, result);
                break;
            }

            if ("2".equals(input)) {
                Item item = chooseItem(player);
                if (item == null) {
                    System.out.println("No item selected.");
                    continue;
                }
                if (item.requiresTarget()) {
                    target = chooseTarget(aliveEnemies);
                } else {
                    target = player;
                }
                Action action = new UseItemAction();
                result = action.execute(new ActionContext(player, List.of(target), item));
                applyResult(target, result);
                printActionOutcome(item.getClass().getSimpleName(), player, target, result);
                break;
            }

            if ("3".equals(input)) {
                if (!player.isSkillAvailable() || player.getSpecialSkill() == null) {
                    System.out.println("Special Skill not available.");
                    continue;
                }
                target = chooseTarget(aliveEnemies);
                result = player.getSpecialSkill().execute(new ActionContext(player, List.of(target), null));
                applyResult(target, result);
                player.setSpecialSkillCd(3);
                printActionOutcome("Special Skill", player, target, result);
                break;
            }

            System.out.println("Invalid choice. Try again.");
        }

        player.decrementCooldown();
    }

    private Item chooseItem(Player player) {
        List<Item> inventory = new ArrayList<>();
        if (player.getInventory() != null) {
            for (Item item : player.getInventory()) {
                if (item != null && item.isAvailable()) {
                    inventory.add(item);
                }
            }
        }

        if (inventory.isEmpty()) {
            System.out.println("No items available.");
            return null;
        }

        while (true) {
            System.out.println();
            System.out.println("Choose an item:");
            for (int i = 0; i < inventory.size(); i++) {
                Item item = inventory.get(i);
                System.out.printf("%d. %s (x%d)%n", i + 1, item.getClass().getSimpleName(), item.getQuantity());
            }
            System.out.println("0. Cancel");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();
            if ("0".equals(input)) {
                return null;
            }

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= inventory.size()) {
                    return inventory.get(choice - 1);
                }
            } catch (NumberFormatException ignored) {
            }

            System.out.println("Invalid choice. Try again.");
        }
    }

    private void printActionOutcome(String actionName, Character user, Character target, ActionResult result) {
        System.out.println();
        System.out.println("Action result:");
        System.out.println("- User: " + user.getClass().getSimpleName());
        System.out.println("- Action: " + actionName);
        System.out.println("- Target: " + target.getClass().getSimpleName());

        if (result == null) {
            System.out.println("- No result returned.");
            return;
        }

        if (result.getDamageGiven() > 0) {
            System.out.println("- Damage dealt: " + result.getDamageGiven());
        }
        if (result.getHealAmount() > 0) {
            System.out.println("- Healed: " + result.getHealAmount());
        }
        if (!result.getEffectsApplied().isEmpty()) {
            System.out.print("- Effects applied: ");
            for (int i = 0; i < result.getEffectsApplied().size(); i++) {
                System.out.print(result.getEffectsApplied().get(i).getClass().getSimpleName());
                if (i < result.getEffectsApplied().size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }

        System.out.println("- " + target.getClass().getSimpleName() + " HP: " + target.getHp());
        if (!target.isAlive()) {
            System.out.println("- " + target.getClass().getSimpleName() + " was defeated.");
        }
        System.out.println();
    }

    private void printRoundHeader() {
        System.out.println();
        System.out.println("=== Round " + roundNumber + " ===");
        if (player != null && player.isAlive()) {
            System.out.println("Player HP: " + player.getHp());
        }
        if (enemies != null && !enemies.isEmpty()) {
            int aliveCount = 0;
            for (Enemy enemy : enemies) {
                if (enemy != null && enemy.isAlive()) {
                    aliveCount++;
                }
            }
            System.out.println("Enemies alive: " + aliveCount);
        }
    }

    private Enemy chooseTarget(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }

        if (enemies.size() == 1) {
            return enemies.get(0);
        }

        while (true) {
            System.out.println();
            System.out.println("Choose target:");
            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);
                System.out.printf("%d. %s - HP: %d%n", i + 1, enemy.getClass().getSimpleName(), enemy.getHp());
            }
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= enemies.size()) {
                    return enemies.get(choice - 1);
                }
            } catch (NumberFormatException ignored) {
            }

            System.out.println("Invalid choice. Try again.");
        }
    }

    private List<Enemy> getAliveEnemies() {
        List<Enemy> alive = new ArrayList<>();
        if (enemies == null) {
            return alive;
        }

        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                alive.add(enemy);
            }
        }

        return alive;
    }
}
