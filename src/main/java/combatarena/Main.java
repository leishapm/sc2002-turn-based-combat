package combatarena;

import combatarena.actions.SpecialSkill;
import combatarena.actions.items.Item;
import combatarena.actions.items.Potion;
import combatarena.actions.items.PowerStone;
import combatarena.actions.items.SmokeBomb;
import combatarena.effects.ArcaneBuff;
import combatarena.effects.Stun;
import combatarena.engine.ActionContext;
import combatarena.engine.BasicAttackStrategy;
import combatarena.engine.BattleManagement;
import combatarena.engine.EnemyActionStrategy;
import combatarena.engine.SpeedOrderStrategy;
import combatarena.engine.TurnOrderStrategy;
import combatarena.entities.Enemy;
import combatarena.entities.Goblin;
import combatarena.entities.Player;
import combatarena.entities.Warrior;
import combatarena.entities.Wolf;
import combatarena.entities.Wizard;
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

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {

            System.out.println("SC2002 Turn-Based Combat");
            System.out.println("-------------------------");

            Player player = choosePlayer(scanner);
            List<Item> selectedItems = chooseItems(scanner);
            List<Item> originalItems = new ArrayList<>();
            for (Item item : selectedItems) {
                originalItems.add(item.copy());
            }
            player.setInventory(selectedItems);

            Level level = chooseLevel(scanner);

            boolean replaySame = true;

            while (replaySame) {

                EnemyActionStrategy strategy = new BasicAttackStrategy();

                List<Enemy> initialEnemies;
                List<Enemy> backupEnemies;

                if (level instanceof Level1) {
                    initialEnemies = createLevel1Enemies(strategy);
                    backupEnemies = createLevel1Backup(strategy);
                } else if (level instanceof Level2) {
                    initialEnemies = createLevel2Enemies(strategy);
                    backupEnemies = createLevel2Backup(strategy);
                } else {
                    initialEnemies = createLevel3Enemies(strategy);
                    backupEnemies = createLevel3Backup(strategy);
                }

                assignEnemyNames(initialEnemies);
                assignEnemyNames(backupEnemies);

                printSetup(player, selectedItems, level, initialEnemies, backupEnemies);

                TurnOrderStrategy turnOrderStrategy = new SpeedOrderStrategy();

                BattleManagement battle = new BattleManagement(
                        player,
                        initialEnemies,
                        turnOrderStrategy,
                        level,
                        scanner
                );

                battle.startBattle();

                if (player.isAlive()) {
                    System.out.println();
                    System.out.println("Victory");
                    System.out.printf(
                            "Result: Player Victory Remaining HP: %d / %d | Total Rounds: %d | %s%n",
                            player.getHp(),
                            player.getMaxHp(),
                            battle.getRoundNumber(),
                            formatRemainingItems(player)
                    );
                } else {
                    System.out.println();
                    System.out.println("Defeat");
                    System.out.printf(
                            "Result: Player Defeat Enemies remaining: %d | Total Rounds Survived: %d%n",
                            battle.getAliveEnemyCount(),
                            battle.getRoundNumber()
                    );
                }

                System.out.println();
                System.out.println("1. Replay same settings");
                System.out.println("2. New game");
                System.out.println("3. Exit");
                System.out.print("Enter choice: ");

                String choice = scanner.nextLine();

                if (choice.equals("1")) {
                    player.reset();

                    List<Item> resetItems = new ArrayList<>();
                    for (Item item : originalItems) {
                        resetItems.add(item.copy());
                    }
                    player.setInventory(resetItems);
                } else if (choice.equals("2")) {
                    replaySame = false;
                } else {
                    replaySame = false;
                    running = false;
                }
            }
        }

        scanner.close();
    }

    private static Player choosePlayer(Scanner scanner) {
        while (true) {
            System.out.println("Choose your player:");
            System.out.println("1. Warrior");
            System.out.println("2. Wizard");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();

            if ("1".equals(input)) {
                return new Warrior(createWarriorSkill());
            }

            if ("2".equals(input)) {
                return new Wizard(createWizardSkill());
            }

            System.out.println("Invalid choice. Try again.");
        }
    }

    private static List<Item> chooseItems(Scanner scanner) {
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

    private static void addOrStackItem(List<Item> items, Item newItem) {
        for (Item item : items) {
            if (item.getClass().equals(newItem.getClass())) {
                item.addQuantity(1);
                return;
            }
        }
        items.add(newItem);
    }

    private static Level chooseLevel(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("Choose level:");
            System.out.println("1. Level 1");
            System.out.println("2. Level 2");
            System.out.println("3. Level 3");
            System.out.print("Enter choice: ");

            String input = scanner.nextLine().trim();
            EnemyActionStrategy enemyStrategy = new BasicAttackStrategy();

            if ("1".equals(input)) {
                List<Enemy> initial = createLevel1Enemies(enemyStrategy);
                List<Enemy> backup = createLevel1Backup(enemyStrategy);
                assignEnemyNames(initial);
                assignEnemyNames(backup);
                return new Level1(initial, backup);
            }

            if ("2".equals(input)) {
                List<Enemy> initial = createLevel2Enemies(enemyStrategy);
                List<Enemy> backup = createLevel2Backup(enemyStrategy);
                assignEnemyNames(initial);
                assignEnemyNames(backup);
                return new Level2(initial, backup);
            }

            if ("3".equals(input)) {
                List<Enemy> initial = createLevel3Enemies(enemyStrategy);
                List<Enemy> backup = createLevel3Backup(enemyStrategy);
                assignEnemyNames(initial);
                assignEnemyNames(backup);
                return new Level3(initial, backup);
            }

            System.out.println("Invalid choice. Try again.");
        }
    }

    private static void printSetup(Player player, List<Item> items, Level level,
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
                .map(item -> {
                    String name = formatItemName(item);
                    if (item.getQuantity() > 1) {
                        return name + " x" + item.getQuantity();
                    }
                    return name;
                })
                .collect(Collectors.joining(" + ")));

        // Level line: "Level: Easy (3 Goblins) – A, B, C, Goblin Stats: ..."
        String levelLine = "Level: " + level.getDifficulty() + " (" + formatEnemySummary(initialEnemies) + ")";

        // Extract A, B, C from names like "Goblin A"
        String names = initialEnemies.stream()
                .map(Enemy::getDisplayName)
                .map(n -> {
                    String[] p = n.split(" ");
                    return p.length > 1 ? p[1] : p[0];
                })
                .collect(Collectors.joining(", "));
        if (!names.isEmpty()) {
            levelLine += " – " + names;
        }

        // Inline single enemy stats (first type)
        if (initialEnemies != null && !initialEnemies.isEmpty()) {
            Enemy e = initialEnemies.get(0);
            levelLine += String.format(
                    ", %s Stats: HP: %d, ATK: %d, DEF: %d, SPD: %d",
                    e.getClass().getSimpleName(),
                    e.getHp(),
                    e.getAttack(),
                    e.getDefense(),
                    e.getSpeed()
            );
        }

        System.out.println(levelLine);

        // Grouped turn order: "Player → Goblins"
        System.out.println(formatGroupedTurnOrder(player, initialEnemies));
        System.out.println();
    }

    private static String formatEnemySummary(List<Enemy> enemies) {
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

    private static String formatBackupNames(List<Enemy> backupEnemies) {
        if (backupEnemies == null || backupEnemies.isEmpty()) {
            return "None";
        }

        return backupEnemies.stream()
                .map(Enemy::getDisplayName)
                .collect(Collectors.joining(" + "));
    }

    private static void printEnemyStats(List<Enemy> initialEnemies, List<Enemy> backupEnemies) {
        Map<String, Enemy> orderedUnique = new LinkedHashMap<>();

        if (initialEnemies != null) {
            for (Enemy enemy : initialEnemies) {
                orderedUnique.putIfAbsent(enemy.getClass().getSimpleName(), enemy);
            }
        }

        if (backupEnemies != null) {
            for (Enemy enemy : backupEnemies) {
                orderedUnique.putIfAbsent(enemy.getClass().getSimpleName(), enemy);
            }
        }

        for (Enemy enemy : orderedUnique.values()) {
            System.out.printf(
                    "%s Stats: HP: %d, ATK: %d, DEF: %d, SPD: %d%n",
                    enemy.getClass().getSimpleName(),
                    enemy.getHp(),
                    enemy.getAttack(),
                    enemy.getDefense(),
                    enemy.getSpeed()
            );
        }
    }

    private static String formatTurnOrderLine(Player player, List<Enemy> enemies) {
        List<Object> combatants = new ArrayList<>();
        combatants.add(player);
        combatants.addAll(enemies);

        combatants.sort((a, b) -> Integer.compare(getSpeed(b), getSpeed(a)));

        List<String> parts = new ArrayList<>();
        for (Object obj : combatants) {
            if (obj instanceof Player p) {
                parts.add(p.getClass().getSimpleName() + " (SPD " + p.getSpeed() + ")");
            } else if (obj instanceof Enemy e) {
                parts.add(e.getDisplayName() + " (SPD " + e.getSpeed() + ")");
            }
        }

        return "Turn Order: " + String.join(" → ", parts);
    }

    private static int getSpeed(Object obj) {
        if (obj instanceof Player p) {
            return p.getSpeed();
        }
        if (obj instanceof Enemy e) {
            return e.getSpeed();
        }
        return 0;
    }

    private static void assignEnemyNames(List<Enemy> enemies) {
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

    private static String formatItemName(Item item) {
        if (item == null) {
            return "Unknown";
        }
        return item.getClass().getSimpleName()
                .replace("SmokeBomb", "Smoke Bomb")
                .replace("PowerStone", "Power Stone")
                .replace("ArcaneBlast", "Arcane Blast");
    }

    private static String formatRemainingItems(Player player) {
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
                        text += " ← unused";
                    }
                    return text;
                })
                .collect(Collectors.joining(" | "));
    }

    private static String pluralize(String word, int count) {
        if (count == 1) {
            return word;
        }
        if ("Wolf".equals(word)) {
            return "Wolves";
        }
        return word + "s";
    }

    private static SpecialSkill createWarriorSkill() {
        return new SpecialSkill() {
            @Override
            public ActionResult execute(ActionContext context) {
                return executeEffect(context);
            }

            @Override
            public ActionResult executeEffect(ActionContext context) {
                ActionResult result = new ActionResult();
                result.setDamageGiven(40);
                result.addEffect(new Stun(3));
                return result;
            }
        };
    }

    private static SpecialSkill createWizardSkill() {
        return new SpecialSkill() {
            @Override
            public ActionResult execute(ActionContext context) {
                return executeEffect(context);
            }

            @Override
            public ActionResult executeEffect(ActionContext context) {
                ActionResult result = new ActionResult();
                result.setDamageGiven(30);
                result.addEffect(new ArcaneBuff());
                return result;
            }
        };
    }

    private static List<Enemy> createLevel1Enemies(EnemyActionStrategy strategy) {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Goblin(strategy));
        enemies.add(new Goblin(strategy));
        enemies.add(new Goblin(strategy));
        return enemies;
    }

    private static List<Enemy> createLevel1Backup(EnemyActionStrategy strategy) {
        return new ArrayList<>();
    }

    private static List<Enemy> createLevel2Enemies(EnemyActionStrategy strategy) {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Goblin(strategy));
        enemies.add(new Wolf(strategy));
        return enemies;
    }

    private static List<Enemy> createLevel2Backup(EnemyActionStrategy strategy) {
        List<Enemy> backup = new ArrayList<>();
        backup.add(new Wolf(strategy));
        backup.add(new Wolf(strategy));
        return backup;
    }

    private static List<Enemy> createLevel3Enemies(EnemyActionStrategy strategy) {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Goblin(strategy));
        enemies.add(new Goblin(strategy));
        return enemies;
    }

    private static List<Enemy> createLevel3Backup(EnemyActionStrategy strategy) {
        List<Enemy> backup = new ArrayList<>();
        backup.add(new Goblin(strategy));
        backup.add(new Wolf(strategy));
        backup.add(new Wolf(strategy));
        return backup;
    }
    private static String formatGroupedTurnOrder(Player player, List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return "Turn Order: " + player.getClass().getSimpleName() + " (SPD " + player.getSpeed() + ")";
        }

        int enemySpeed = enemies.get(0).getSpeed();
        String enemyType = enemies.get(0).getClass().getSimpleName() + "s";

        return String.format(
                "Turn Order: %s (SPD %d) → %s (SPD %d)",
                player.getClass().getSimpleName(),
                player.getSpeed(),
                enemyType,
                enemySpeed
        );
    }
}
