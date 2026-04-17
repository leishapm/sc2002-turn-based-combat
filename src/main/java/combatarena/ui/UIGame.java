package combatarena.ui;

import combatarena.actions.Action;
import combatarena.actions.items.Item;
import combatarena.engine.BattleManagement;
import combatarena.entities.Character;
import combatarena.entities.Enemy;
import combatarena.entities.Player;
import combatarena.effects.SmokeBombInvulnerability;
import combatarena.effects.StatusEffect;
import combatarena.effects.Stun;
import combatarena.level.Level;
import combatarena.util.ActionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class UIGame {

    private BattleManagement gameSnapshot;
    private final Scanner scanner;
    private String lastConsumedItemName;
    private String lastPrintedTurnOrderLine;

    public UIGame(BattleManagement gameSnapshot) {
        this.gameSnapshot = gameSnapshot;
        this.scanner = new Scanner(System.in);
        this.lastConsumedItemName = null;
        this.lastPrintedTurnOrderLine = null;
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

    public void showVictory(int hp, int rounds) {
        System.out.println("Victory!");
        System.out.println("HP left: " + hp);
        System.out.println("Rounds taken: " + rounds);
    }

    public void showDefeat(int enemiesLeft, int rounds) {
        System.out.println("Defeat.");
        System.out.println("Enemies left: " + enemiesLeft);
        System.out.println("Rounds survived: " + rounds);
    }

    public Player choosePlayer() {
        System.out.println("Choose player:");
        System.out.println("1. Warrior");
        System.out.println("2. Wizard");
        System.out.print("Enter choice: ");

        String input = scanner.nextLine().trim();

        switch (input) {
            case "1":
                System.out.println("Warrior selected.");
                return null;
            case "2":
                System.out.println("Wizard selected.");
                return null;
            default:
                System.out.println("Invalid choice.");
                return null;
        }
    }

    public List<Item> chooseItems() {
        System.out.println("Choose items feature is not wired yet.");
        return new ArrayList<>();
    }

    public Level chooseLevel() {
        System.out.print("Choose level (1, 2, 3): ");
        String input = scanner.nextLine().trim();

        switch (input) {
            case "1":
                System.out.println("Level 1 selected.");
                return null;
            case "2":
                System.out.println("Level 2 selected.");
                return null;
            case "3":
                System.out.println("Level 3 selected.");
                return null;
            default:
                System.out.println("Invalid level.");
                return null;
        }
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
}
