package combatarena.ui;

import combatarena.actions.Action;
import combatarena.actions.skills.ArcaneBlast;
import combatarena.actions.skills.ShieldBash;
import combatarena.actions.items.Item;
import combatarena.actions.items.Potion;
import combatarena.actions.items.PowerStone;
import combatarena.actions.items.SmokeBomb;
import combatarena.engine.BattleManagement;
import combatarena.engine.BasicAttackStrategy;
import combatarena.engine.EnemyActionStrategy;
import combatarena.entities.Character;
import combatarena.entities.Goblin;
import combatarena.entities.Enemy;
import combatarena.entities.Player;
import combatarena.entities.Warrior;
import combatarena.entities.Wizard;
import combatarena.entities.Wolf;
import combatarena.effects.SmokeBombInvulnerability;
import combatarena.effects.StatusEffect;
import combatarena.effects.Stun;
import combatarena.level.Level;
import combatarena.level.Level1;
import combatarena.level.Level2;
import combatarena.level.Level3;
import combatarena.util.ActionResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class UIGame {

    private BattleManagement gameSnapshot;
    private final Scanner scanner;
    private String lastConsumedItemName;
    private String lastPrintedTurnOrderLine;

    public UIGame(Scanner scanner) {
        this.gameSnapshot = null;
        this.scanner = scanner;
        this.lastConsumedItemName = null;
        this.lastPrintedTurnOrderLine = null;
    }

    public UIGame(BattleManagement gameSnapshot) {
        this(new Scanner(System.in));
        this.gameSnapshot = gameSnapshot;
    }

    public UIGame() {
        this(new Scanner(System.in));
    }

    public BattleManagement getGameSnapshot() {
        return gameSnapshot;
    }

    public void setGameSnapshot(BattleManagement gameSnapshot) {
        this.gameSnapshot = gameSnapshot;
    }

    public void showLoadingScreen() {
        System.out.println("Loading game...");
        System.out.println("Preparing battle arena...");
    }

    public void showBattleState() {
        System.out.println("Battle state requested.");
        System.out.println("Current snapshot: " + (gameSnapshot == null ? "none" : "available"));
    }

    public void showPlayerActions(Player player) {
        if (player == null) {
            System.out.println("No player available.");
            return;
        }

        System.out.println("Player actions:");
        List<Action> actions = player.getAvailableActions();
        if (actions == null || actions.isEmpty()) {
            System.out.println("No actions available.");
            return;
        }

        for (int i = 0; i < actions.size(); i++) {
            System.out.println((i + 1) + ". " + actions.get(i).info());
        }
    }

    public Player choosePlayer() {
        while (true) {
            System.out.println("Choose your player:");
            System.out.println("1. Warrior");
            System.out.println("2. Wizard");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();
            if ("1".equals(input)) {
                return new Warrior(new ShieldBash());
            }
            if ("2".equals(input)) {
                return new Wizard(new ArcaneBlast());
            }
            System.out.println("Invalid choice. Try again.");
        }
    }

    public List<Item> chooseItems() {
        List<Item> items = new ArrayList<>();
        System.out.println();
        System.out.println("Choose 2 items:");

        for (int pick = 1; pick <= 2; pick++) {
            while (true) {
                System.out.println();
                System.out.println("Pick item " + pick + ":");
                System.out.println("1. Potion");
                System.out.println("2. Smoke Bomb");
                System.out.println("3. Power Stone");
                System.out.print("Enter choice: ");

                String input = scanner.nextLine().trim();
                if ("1".equals(input)) {
                    addOrStackItem(items, new Potion());
                    break;
                }
                if ("2".equals(input)) {
                    addOrStackItem(items, new SmokeBomb());
                    break;
                }
                if ("3".equals(input)) {
                    addOrStackItem(items, new PowerStone());
                    break;
                }
                System.out.println("Invalid choice. Try again.");
            }
        }

        return items;
    }

    public Level chooseLevel() {
        while (true) {
            System.out.println();
            System.out.println("Choose level:");
            System.out.println("1. Level 1");
            System.out.println("2. Level 2");
            System.out.println("3. Level 3");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();
            if ("1".equals(input)) {
                return createLevelFromChoice(1);
            }
            if ("2".equals(input)) {
                return createLevelFromChoice(2);
            }
            if ("3".equals(input)) {
                return createLevelFromChoice(3);
            }
            System.out.println("Invalid choice. Try again.");
        }
    }

    public void printSetup(Player player, List<Item> items, Level level,
                           List<Enemy> initialEnemies, List<Enemy> backupEnemies) {
        System.out.printf(
                "Player: %s, %s Stats: HP: %d, ATK: %d, DEF: %d, SPD: %d%n",
                player.getClass().getSimpleName(),
                player.getClass().getSimpleName(),
                player.getHp(),
                player.getAttack(),
                player.getDefense(),
                player.getSpeed()
        );

        System.out.print("Items: ");
        System.out.println(items.stream()
                .map(item -> item.getQuantity() > 1 ? formatItemName(item) + " x" + item.getQuantity() : formatItemName(item))
                .collect(Collectors.joining(" + ")));

        String levelLine = "Level: " + level.getDifficulty() + " (" + formatEnemySummary(initialEnemies) + ")";
        String names = initialEnemies.stream()
                .map(Enemy::getDisplayName)
                .map(n -> {
                    String[] p = n.split(" ");
                    return p.length > 1 ? p[1] : p[0];
                })
                .collect(Collectors.joining(", "));
        if (!names.isEmpty()) {
            levelLine += " - " + names;
        }

        if (initialEnemies != null && !initialEnemies.isEmpty()) {
            Enemy e = initialEnemies.get(0);
            levelLine += String.format(", %s Stats: HP: %d, ATK: %d, DEF: %d, SPD: %d",
                    e.getClass().getSimpleName(), e.getHp(), e.getAttack(), e.getDefense(), e.getSpeed());
        }

        System.out.println(levelLine);
        System.out.println(formatGroupedTurnOrder(player, initialEnemies));
        System.out.println();
    }

    public void showVictory(Player player, int rounds) {
        System.out.println();
        System.out.println("Victory");
        System.out.printf(
                "Result: Player Victory Remaining HP: %d / %d | Total Rounds: %d | %s%n",
                player.getHp(),
                player.getMaxHp(),
                rounds,
                formatRemainingItems(player)
        );
    }

    public void showVictory(int hp, int rounds) {
        System.out.println("Victory!");
        System.out.println("HP left: " + hp);
        System.out.println("Rounds taken: " + rounds);
    }

    public void showDefeat(int enemiesLeft, int rounds) {
        System.out.println();
        System.out.println("Defeat");
        System.out.printf(
                "Result: Player Defeat Enemies remaining: %d | Total Rounds Survived: %d%n",
                enemiesLeft,
                rounds
        );
    }

    public String choosePostBattleOption() {
        System.out.println();
        System.out.println("1. Replay same settings");
        System.out.println("2. New game");
        System.out.println("3. Exit");
        System.out.print("Enter choice: ");
        return scanner.nextLine().trim();
    }

    public List<Enemy> createInitialEnemies(Level level) {
        EnemyActionStrategy strategy = new BasicAttackStrategy();
        if (level instanceof Level1) {
            List<Enemy> enemies = createLevel1Enemies(strategy);
            assignEnemyNames(enemies);
            return enemies;
        }
        if (level instanceof Level2) {
            List<Enemy> enemies = createLevel2Enemies(strategy);
            assignEnemyNames(enemies);
            return enemies;
        }
        List<Enemy> enemies = createLevel3Enemies(strategy);
        assignEnemyNames(enemies);
        return enemies;
    }

    public List<Enemy> createBackupEnemies(Level level) {
        EnemyActionStrategy strategy = new BasicAttackStrategy();
        if (level instanceof Level1) {
            List<Enemy> enemies = createLevel1Backup(strategy);
            assignEnemyNames(enemies);
            return enemies;
        }
        if (level instanceof Level2) {
            List<Enemy> enemies = createLevel2Backup(strategy);
            assignEnemyNames(enemies);
            return enemies;
        }
        List<Enemy> enemies = createLevel3Backup(strategy);
        assignEnemyNames(enemies);
        return enemies;
    }

    public Action chooseAction(List<Action> available) {
        if (available == null || available.isEmpty()) {
            return null;
        }

        System.out.println("Choose action:");
        for (int i = 0; i < available.size(); i++) {
            System.out.println((i + 1) + ". " + available.get(i).info());
        }

        System.out.print("Enter choice: ");
        String input = scanner.nextLine().trim();
        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= available.size()) {
                return available.get(choice - 1);
            }
        } catch (NumberFormatException ignored) {
        }
        return available.get(0);
    }

    private Level createLevelFromChoice(int choice) {
        EnemyActionStrategy enemyStrategy = new BasicAttackStrategy();
        if (choice == 1) {
            List<Enemy> initial = createLevel1Enemies(enemyStrategy);
            List<Enemy> backup = createLevel1Backup(enemyStrategy);
            assignEnemyNames(initial);
            assignEnemyNames(backup);
            return new Level1(initial, backup);
        }
        if (choice == 2) {
            List<Enemy> initial = createLevel2Enemies(enemyStrategy);
            List<Enemy> backup = createLevel2Backup(enemyStrategy);
            assignEnemyNames(initial);
            assignEnemyNames(backup);
            return new Level2(initial, backup);
        }
        List<Enemy> initial = createLevel3Enemies(enemyStrategy);
        List<Enemy> backup = createLevel3Backup(enemyStrategy);
        assignEnemyNames(initial);
        assignEnemyNames(backup);
        return new Level3(initial, backup);
    }

    public Enemy chooseTarget(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return null;
        }

        System.out.println("Choose target:");
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            System.out.println((i + 1) + ". " + getDisplayName(enemy) + " - HP: " + enemy.getHp());
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

        return enemies.get(0);
    }

    public String promptPlayerAction(Player player) {
        System.out.println();
        System.out.println("Choose your action:");
        System.out.println("1. Basic Attack");
        System.out.println("2. Defend");
        System.out.println("3. Use Item");
        System.out.println("4. Special Skill" + (player.isSkillAvailable() ? "" : " (cooldown " + player.getSpecialSkillCd() + ")"));
        System.out.print("Enter choice: ");
        return scanner.nextLine().trim();
    }

    public Item chooseItem(Player player) {
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

    public void printRoundHeader(int roundNumber, List<Character> turnOrderList) {
        System.out.println();
        System.out.println("Round " + roundNumber);

        String currentTurnOrderLine = formatTurnOrderLine(turnOrderList);
        if (roundNumber == 1 || !currentTurnOrderLine.equals(lastPrintedTurnOrderLine)) {
            System.out.println(currentTurnOrderLine);
            lastPrintedTurnOrderLine = currentTurnOrderLine;
        }
    }

    public void printEndOfRound(Player player, List<Enemy> enemies, int roundNumber) {
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
                System.out.print(" <- consumed");
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

    public void printBackupWaveAnnouncement(List<Enemy> backupWave) {
        String joined = backupWave.stream()
                .map(e -> getDisplayName(e) + " (HP: " + e.getHp() + ")")
                .collect(Collectors.joining(" + "));
        System.out.println("All initial enemies eliminated -> Backup Spawn triggered! " + joined + " enter simultaneously");
    }

    public void printDamageLine(String actorName, String actionName, String targetName,
                                int beforeHp, int afterHp, int attackerAtk, int targetDef, String suffix) {
        int damage = Math.max(0, beforeHp - afterHp);
        System.out.println(actorName + " -> " + actionName + " -> " + targetName + ": HP: " +
                beforeHp + " -> " + afterHp + " (dmg: " + attackerAtk + "-" + targetDef + "=" + damage + ")" +
                (suffix == null ? "" : suffix));
    }

    public void printSpecialSkillLine(String actorName, String skillName, String targetName,
                                      int beforeHp, int afterHp, int attackerAtk, int targetDef,
                                      ActionResult result, int cooldown, boolean defeated) {
        int damage = Math.max(0, beforeHp - afterHp);
        StringBuilder line = new StringBuilder();
        line.append(actorName)
                .append(" -> ")
                .append(skillName)
                .append(" -> ")
                .append(targetName)
                .append(": HP: ")
                .append(beforeHp)
                .append(" -> ")
                .append(afterHp)
                .append(" (dmg: ")
                .append(attackerAtk)
                .append("-")
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

        line.append(" | Cooldown: ").append(cooldown);
        System.out.println(line);
    }

    public void printPotionLine(String actorName, int beforeHp, int afterHp, int healAmount) {
        System.out.println(actorName + " -> Item -> Potion used: HP: " + beforeHp + " -> " + afterHp + " (+" + healAmount + ")");
    }

    public void printSmokeBombLine(String actorName) {
        System.out.println(actorName + " -> Item -> Smoke Bomb used: Enemy attacks deal 0 damage this turn + next");
    }

    public void printPowerStoneLine(String actorName, String skillName, String targetName,
                                    int beforeHp, int afterHp, int attackerAtk, int targetDef,
                                    ActionResult result, int cooldown) {
        int damage = Math.max(0, beforeHp - afterHp);
        StringBuilder line = new StringBuilder();
        line.append(actorName)
                .append(" -> Item -> Power Stone used -> ")
                .append(skillName)
                .append(" triggered -> ")
                .append(targetName)
                .append(": HP: ")
                .append(beforeHp)
                .append(" -> ")
                .append(afterHp)
                .append(" (dmg: ")
                .append(attackerAtk)
                .append("-")
                .append(targetDef)
                .append("=")
                .append(damage)
                .append(")");

        if (hasStunEffect(result)) {
            line.append(" | ").append(targetName).append(" STUNNED (2 turns)");
        }

        line.append(" | Cooldown unchanged -> ")
                .append(cooldown)
                .append(" (Power Stone does not affect cooldown)")
                .append(" | Power Stone consumed");
        System.out.println(line);
    }

    public void printDefendLine(String actorName, int beforeDef, int afterDef) {
        System.out.println(actorName + " -> Defend: DEF: " + beforeDef + " -> " + afterDef + " (+10 for 2 turns)");
    }

    public void printStunnedSkip(Character character, boolean expires) {
        if (expires) {
            System.out.println(getDisplayName(character) + " -> STUNNED: Turn skipped | Stun expires");
        } else {
            System.out.println(getDisplayName(character) + " -> STUNNED: Turn skipped");
        }
    }

    public void printEliminatedSkip(Character current, boolean stunnedAtStart) {
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
            System.out.println(name + " -> ELIMINATED: Skipped | Stun expires");
        } else {
            System.out.println(name + " -> ELIMINATED: Skipped");
        }
    }

    public void setLastConsumedItemName(String lastConsumedItemName) {
        this.lastConsumedItemName = lastConsumedItemName;
    }

    public void printMessage(String text) {
        System.out.println(text);
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

        return "Turn Order: " + String.join(" -> ", parts);
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

    private void addOrStackItem(List<Item> items, Item newItem) {
        for (Item item : items) {
            if (item.getClass().equals(newItem.getClass())) {
                item.addQuantity(1);
                return;
            }
        }
        items.add(newItem);
    }

    private String formatRemainingItems(Player player) {
        if (player.getInventory() == null || player.getInventory().isEmpty()) {
            return "Remaining Items: None";
        }

        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Item item : player.getInventory()) {
            if (item == null) {
                continue;
            }
            String name = formatItemName(item);
            totals.put(name, totals.getOrDefault(name, 0) + item.getQuantity());
        }

        return totals.entrySet().stream()
                .map(entry -> {
                    String text = "Remaining " + entry.getKey() + ": " + entry.getValue();
                    if (entry.getValue() > 0) {
                        text += " <- unused";
                    }
                    return text;
                })
                .collect(Collectors.joining(" | "));
    }

    private String formatEnemySummary(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return "None";
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Enemy enemy : enemies) {
            String type = enemy.getClass().getSimpleName();
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }

        return counts.entrySet().stream()
                .map(entry -> entry.getValue() + " " + pluralize(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(" + "));
    }

    private String pluralize(String word, int count) {
        if (count == 1) {
            return word;
        }
        if ("Wolf".equals(word)) {
            return "Wolves";
        }
        return word + "s";
    }

    private String formatGroupedTurnOrder(Player player, List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return "Turn Order: " + player.getClass().getSimpleName() + " (SPD " + player.getSpeed() + ")";
        }

        int enemySpeed = enemies.get(0).getSpeed();
        String enemyType = enemies.get(0).getClass().getSimpleName() + "s";

        return String.format(
                "Turn Order: %s (SPD %d) -> %s (SPD %d)",
                player.getClass().getSimpleName(),
                player.getSpeed(),
                enemyType,
                enemySpeed
        );
    }

    private void assignEnemyNames(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return;
        }

        Map<String, Integer> totalCount = new LinkedHashMap<>();
        for (Enemy enemy : enemies) {
            String type = enemy.getClass().getSimpleName();
            totalCount.put(type, totalCount.getOrDefault(type, 0) + 1);
        }

        Map<String, Integer> seenCount = new LinkedHashMap<>();
        for (Enemy enemy : enemies) {
            String type = enemy.getClass().getSimpleName();
            int seen = seenCount.getOrDefault(type, 0) + 1;
            seenCount.put(type, seen);

            if (totalCount.get(type) == 1) {
                enemy.setName(type);
            } else {
                enemy.setName(type + " " + (char) ('A' + seen - 1));
            }
        }
    }

    private List<Enemy> createLevel1Enemies(EnemyActionStrategy strategy) {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Goblin(strategy));
        enemies.add(new Goblin(strategy));
        enemies.add(new Goblin(strategy));
        return enemies;
    }

    private List<Enemy> createLevel1Backup(EnemyActionStrategy strategy) {
        return new ArrayList<>();
    }

    private List<Enemy> createLevel2Enemies(EnemyActionStrategy strategy) {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Goblin(strategy));
        enemies.add(new Wolf(strategy));
        return enemies;
    }

    private List<Enemy> createLevel2Backup(EnemyActionStrategy strategy) {
        List<Enemy> backup = new ArrayList<>();
        backup.add(new Wolf(strategy));
        backup.add(new Wolf(strategy));
        return backup;
    }

    private List<Enemy> createLevel3Enemies(EnemyActionStrategy strategy) {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Goblin(strategy));
        enemies.add(new Goblin(strategy));
        return enemies;
    }

    private List<Enemy> createLevel3Backup(EnemyActionStrategy strategy) {
        List<Enemy> backup = new ArrayList<>();
        backup.add(new Goblin(strategy));
        backup.add(new Wolf(strategy));
        backup.add(new Wolf(strategy));
        return backup;
    }
}
