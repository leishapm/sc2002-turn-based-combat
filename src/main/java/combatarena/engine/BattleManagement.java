package combatarena.engine;

import combatarena.actions.Action;
import combatarena.actions.BasicAttack;
import combatarena.actions.UseItemAction;
import combatarena.actions.items.Item;
import combatarena.actions.items.Potion;
import combatarena.actions.items.PowerStone;
import combatarena.actions.items.SmokeBomb;
import combatarena.entities.Character;
import combatarena.entities.Enemy;
import combatarena.entities.Player;
import combatarena.effects.SmokeBombInvulnerability;
import combatarena.effects.Stun;
import combatarena.effects.StatusEffect;
import combatarena.level.Level;
import combatarena.util.ActionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BattleManagement {

    private final Player player;
    private final List<Enemy> enemies;
    private List<Character> turnOrderList;
    private int currentTurnIndex;
    private int roundNumber;
    private final TurnOrderStrategy turnOrderStrategy;
    private final Level selectedLevel;
    private final Scanner scanner;
    private String lastConsumedItemName;
    private String lastPrintedTurnOrderLine;

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
        this.lastConsumedItemName = null;
        this.lastPrintedTurnOrderLine = null;
    }

    public void startBattle() {
        turnOrderList = determineTurnOrder();
        lastPrintedTurnOrderLine = formatTurnOrderLine(turnOrderList);
        printRoundHeader();

        while (!isBattleOver()) {
            Character current = nextTurn();
            if (current == null) {
                continue;
            }

            if (!current.isAlive()) {
                printEliminatedSkip(current, false);
                continue;
            }

            boolean stunnedBeforeUpdate = current.isStunned();
            current.updateEffects();

            if (!current.isAlive()) {
                printEliminatedSkip(current, stunnedBeforeUpdate);
                continue;
            }

            if (current.isStunned()) {

                boolean willExpire = false;

                if (current.getActiveEffects() != null) {
                    for (StatusEffect effect : current.getActiveEffects()) {
                        if (effect instanceof Stun && effect.getDuration() == 1) {
                            willExpire = true;
                            break;
                        }
                    }
                }

                if (willExpire) {
                    System.out.println(getDisplayName(current) + " → STUNNED: Turn skipped | Stun expires");
                } else {
                    System.out.println(getDisplayName(current) + " → STUNNED: Turn skipped");
                }

                continue;
            }

            if (current instanceof Player playerEntity) {
                executePlayerTurn(playerEntity);
            } else if (current instanceof Enemy enemy) {
                executeEnemyTurn(enemy);
            }

            if (isBattleOver()) {
                break;
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

    private void executeEnemyTurn(Enemy enemy) {
        if (player == null || !player.isAlive()) {
            return;
        }

        List<Character> targets = new ArrayList<>();
        targets.add(player);

        ActionContext context = new ActionContext(enemy, targets, null);
        Action action = enemy.getActionStrategy() == null ? null : enemy.getActionStrategy().chooseAction(enemy, player);
        if (action == null) {
            return;
        }

        int beforeHp = player.getHp();
        ActionResult result = action.execute(context);
        applyResult(player, result);
        int afterHp = player.getHp();

        printDamageLine(
                getDisplayName(enemy),
                "BasicAttack",
                getDisplayName(player),
                beforeHp,
                afterHp,
                enemy.getAttack(),
                player.getDefense(),
                !player.isAlive() ? " | " + getDisplayName(player) + " was defeated." : ""
        );
    }

    private void executePlayerTurn(Player playerEntity) {
        playerEntity.decrementCooldown();

        List<Enemy> aliveEnemies = getAliveEnemies();
        if (aliveEnemies.isEmpty()) {
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("Choose your action:");
            System.out.println("1. Basic Attack");
            System.out.println("2. Use Item");
            System.out.println("3. Special Skill" + (playerEntity.isSkillAvailable() ? "" : " (cooldown " + playerEntity.getSpecialSkillCd() + ")"));
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();

            if ("1".equals(input)) {
                Enemy target = chooseTarget(aliveEnemies);
                if (target == null) {
                    return;
                }

                int beforeHp = target.getHp();
                Action action = new BasicAttack();
                ActionResult result = action.execute(new ActionContext(playerEntity, List.of(target), null));
                applyResult(target, result);
                int afterHp = target.getHp();

                printDamageLine(
                        getDisplayName(playerEntity),
                        "BasicAttack",
                        getDisplayName(target),
                        beforeHp,
                        afterHp,
                        playerEntity.getAttack(),
                        target.getDefense(),
                        !target.isAlive() ? " | " + getDisplayName(target) + " was defeated." : ""
                );
                return;
            }

            if ("2".equals(input)) {
                Item item = chooseItem(playerEntity);
                if (item == null) {
                    continue;
                }

                if (item instanceof Potion) {
                    Character target = playerEntity;
                    int beforeHp = target.getHp();
                    ActionResult result = item.use(new ActionContext(playerEntity, List.of(target), item));
                    applyResult(target, result);
                    int afterHp = target.getHp();

                    printPotionLine(getDisplayName(playerEntity), beforeHp, afterHp, result.getHealAmount());
                    lastConsumedItemName = "Potion";
                    return;
                }

                if (item instanceof SmokeBomb) {
                    Character target = playerEntity;
                    ActionResult result = item.use(new ActionContext(playerEntity, List.of(target), item));
                    applyResult(target, result);
                    printSmokeBombLine(getDisplayName(playerEntity));
                    lastConsumedItemName = "Smoke Bomb";
                    return;
                }

                if (item instanceof PowerStone) {
                    Enemy target = chooseTarget(aliveEnemies);
                    if (target == null) {
                        return;
                    }

                    int beforeHp = target.getHp();
                    ActionResult result = item.use(new ActionContext(playerEntity, List.of(target), item));
                    applyResult(target, result);
                    int afterHp = target.getHp();

                    printPowerStoneLine(
                            getDisplayName(playerEntity),
                            getSkillDisplayName(playerEntity),
                            getDisplayName(target),
                            beforeHp,
                            afterHp,
                            playerEntity.getAttack(),
                            target.getDefense(),
                            result,
                            playerEntity.getSpecialSkillCd()
                    );
                    lastConsumedItemName = "Power Stone";
                    return;
                }

                return;
            }

            if ("3".equals(input)) {
                if (!playerEntity.isSkillAvailable() || playerEntity.getSpecialSkill() == null) {
                    System.out.println("Special Skill not available.");
                    continue;
                }

                Enemy target = chooseTarget(aliveEnemies);
                if (target == null) {
                    return;
                }

                int beforeHp = target.getHp();
                ActionResult result = playerEntity.getSpecialSkill().execute(new ActionContext(playerEntity, List.of(target), null));
                applyResult(target, result);
                int afterHp = target.getHp();

                playerEntity.setSpecialSkillCd(3);

                printSpecialSkillLine(
                        getDisplayName(playerEntity),
                        getSkillDisplayName(playerEntity),
                        getDisplayName(target),
                        beforeHp,
                        afterHp,
                        playerEntity.getAttack(),
                        target.getDefense(),
                        result,
                        playerEntity.getSpecialSkillCd(),
                        !target.isAlive()
                );
                return;
            }

            System.out.println("Invalid choice. Try again.");
        }
    }

    private void printDamageLine(String actorName, String actionName, String targetName,
                                 int beforeHp, int afterHp, int attackerAtk, int targetDef, String suffix) {
        int damage = Math.max(0, beforeHp - afterHp);
        System.out.println(actorName + " → " + actionName + " → " + targetName + ": HP: " +
                beforeHp + " → " + afterHp + " (dmg: " + attackerAtk + "−" + targetDef + "=" + damage + ")" +
                (suffix == null ? "" : suffix));
    }

    private void printSpecialSkillLine(String actorName, String skillName, String targetName,
                                       int beforeHp, int afterHp, int attackerAtk, int targetDef,
                                       ActionResult result, int cooldown, boolean defeated) {
        int damage = Math.max(0, beforeHp - afterHp);
        StringBuilder line = new StringBuilder();
        line.append(actorName)
                .append(" → ")
                .append(skillName)
                .append(" → ")
                .append(targetName)
                .append(": HP: ")
                .append(beforeHp)
                .append(" → ")
                .append(afterHp)
                .append(" (dmg: ")
                .append(attackerAtk)
                .append("−")
                .append(targetDef)
                .append("=")
                .append(damage)
                .append(")");

        if (hasStunEffect(result)) {
            line.append(" | ").append(targetName).append(" STUNNED (2 turns)");
        }


        if (defeated) {
            line.append(" | ").append(targetName).append(" was defeated.");
        }

        System.out.println(line);
    }

    private void printPotionLine(String actorName, int beforeHp, int afterHp, int healAmount) {
        System.out.println(actorName + " → Item → Potion used: HP: " + beforeHp + " → " + afterHp + " (+" + healAmount + ")");
    }

    private void printSmokeBombLine(String actorName) {
        System.out.println(actorName + " → Item → Smoke Bomb used: Enemy attacks deal 0 damage this turn + next");
    }

    private void printPowerStoneLine(String actorName, String skillName, String targetName,
                                     int beforeHp, int afterHp, int attackerAtk, int targetDef,
                                     ActionResult result, int cooldown) {
        int damage = Math.max(0, beforeHp - afterHp);
        StringBuilder line = new StringBuilder();
        line.append(actorName)
                .append(" → Item → Power Stone used → ")
                .append(skillName)
                .append(" triggered → ")
                .append(targetName)
                .append(": HP: ")
                .append(beforeHp)
                .append(" → ")
                .append(afterHp)
                .append(" (dmg: ")
                .append(attackerAtk)
                .append("−")
                .append(targetDef)
                .append("=")
                .append(damage)
                .append(")");

        if (hasStunEffect(result)) {
            line.append(" | ").append(targetName).append(" STUNNED (2 turns)");
        }

        line.append(" | Cooldown unchanged → ")
                .append(cooldown)
                .append(" (Power Stone does not affect cooldown)")
                .append(" | Power Stone consumed");

        System.out.println(line);
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
            if (isWaveCleared() && hasPendingBackup()) {
                spawnBackupWaveAnnouncement();
            }

            printEndOfRound();

            currentTurnIndex = 0;
            roundNumber++;
            turnOrderList = determineTurnOrder();

            if (!isBattleOver()) {
                printRoundHeader();
            }
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

    private boolean hasPendingBackup() {
        if (selectedLevel == null) {
            return false;
        }

        List<Enemy> backupWave = selectedLevel.getBackupWave();
        return backupWave != null && !backupWave.isEmpty() && !selectedLevel.isBackupSpawnTriggered();
    }

    private void spawnBackupWaveAnnouncement() {
        if (selectedLevel == null || enemies == null || !hasPendingBackup()) {
            return;
        }

        List<Enemy> backupWave = selectedLevel.getBackupWave();

        String joined = backupWave.stream()
                .map(e -> getDisplayName(e) + " (HP: " + e.getHp() + ")")
                .collect(Collectors.joining(" + "));

        System.out.println("All initial enemies eliminated → Backup Spawn triggered! " + joined + " enter simultaneously");

        enemies.addAll(backupWave);
        selectedLevel.setBackupSpawnTriggered(true);
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

    public int getRoundNumber() {
        return roundNumber;
    }

    public int getAliveEnemyCount() {
        int count = 0;
        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                count++;
            }
        }
        return count;
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
                System.out.printf("%d. %s (x%d)%n", i + 1, formatItemName(item), item.getQuantity());
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
                System.out.printf("%d. %s - HP: %d%n", i + 1, getDisplayName(enemy), enemy.getHp());
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

    private void printRoundHeader() {
        System.out.println();
        System.out.println("Round " + roundNumber);

        if (roundNumber > 1) {
            String currentTurnOrderLine = formatTurnOrderLine(turnOrderList);
            if (!currentTurnOrderLine.equals(lastPrintedTurnOrderLine)) {
                System.out.println(currentTurnOrderLine);
                lastPrintedTurnOrderLine = currentTurnOrderLine;
            }
        }
    }

    private void printEndOfRound() {
        System.out.print("End of Round " + roundNumber + ": ");
        System.out.print(player.getClass().getSimpleName() + " HP: " + player.getHp() + "/" + player.getMaxHp());

        for (Enemy enemy : enemies) {
            System.out.print(" | " + getDisplayName(enemy) + " HP: " + (enemy.isAlive() ? enemy.getHp() : 0));
            if (enemy.isStunned() && enemy.isAlive()) {
                System.out.print(" [STUNNED]");
            }
        }

        for (Item item : player.getInventory()) {
            System.out.print(" | " + formatItemName(item) + ": " + item.getQuantity());
            if (item.getQuantity() == 0 && formatItemName(item).equals(lastConsumedItemName)) {
                System.out.print(" ← consumed");
            }
        }

        boolean anyItemAvailable = false;
        for (Item item : player.getInventory()) {
            if (item != null && item.isAvailable()) {
                anyItemAvailable = true;
                break;
            }
        }
        if (!anyItemAvailable) {
            System.out.print(" | Item action no longer available");
        }

        SmokeBombInvulnerability smokeBomb = null;
        for (StatusEffect effect : player.getActiveEffects()) {
            if (effect instanceof SmokeBombInvulnerability inv) {
                smokeBomb = inv;
                break;
            }
        }

        if (smokeBomb != null) {
            System.out.print(" | Effect: " + smokeBomb.getDuration() + " turn" + (smokeBomb.getDuration() == 1 ? "" : "s") + " remaining");
        }

        System.out.print(" | Special Skills Cooldown: " + player.getSpecialSkillCd() + " round" + (player.getSpecialSkillCd() == 1 ? "" : "s"));
        System.out.println();
        lastConsumedItemName = null;
    }

    private String formatTurnOrderLine(List<Character> combatants) {
        if (combatants == null || combatants.isEmpty()) {
            return "Turn Order: None";
        }

        List<Character> sorted = new ArrayList<>(combatants);
        sorted.sort((a, b) -> Integer.compare(b.getSpeed(), a.getSpeed()));

        List<String> parts = new ArrayList<>();
        for (Character character : sorted) {
            parts.add(getDisplayName(character) + " (SPD " + character.getSpeed() + ")");
        }

        return "Turn Order: " + String.join(" → ", parts);
    }

    private boolean hasStunEffect(ActionResult result) {
        if (result == null || result.getEffectsApplied() == null) {
            return false;
        }

        for (StatusEffect effect : result.getEffectsApplied()) {
            if (effect instanceof Stun) {
                return true;
            }
        }

        return false;
    }

    private String formatItemName(Item item) {
        if (item == null) {
            return "Unknown";
        }
        return item.getClass().getSimpleName()
                .replace("SmokeBomb", "Smoke Bomb")
                .replace("PowerStone", "Power Stone")
                .replace("ArcaneBlast", "Arcane Blast");
    }

    private String getDisplayName(Character character) {
        if (character instanceof Enemy enemy) {
            return enemy.getDisplayName();
        }
        return character.getClass().getSimpleName();
    }

    private String getSkillDisplayName(Player playerEntity) {
        if (playerEntity == null) {
            return "Special Skill";
        }

        String name = playerEntity.getClass().getSimpleName();
        if ("Warrior".equals(name)) {
            return "Shield Bash";
        }
        if ("Wizard".equals(name)) {
            return "Arcane Blast";
        }
        return "Special Skill";
    }

    private void printEliminatedSkip(Character current, boolean stunnedAtStart) {
        String name = getDisplayName(current);

        boolean hadStun = stunnedAtStart;

        if (!hadStun && current.getActiveEffects() != null) {
            for (StatusEffect effect : current.getActiveEffects()) {
                if (effect instanceof Stun) {
                    hadStun = true;
                    break;
                }
            }
        }

        if (hadStun) {
            System.out.println(name + " → ELIMINATED: Skipped | Stun expires");
        } else {
            System.out.println(name + " → ELIMINATED: Skipped");
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
