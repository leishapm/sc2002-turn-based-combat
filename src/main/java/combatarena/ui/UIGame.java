package combatarena.ui;

import combatarena.actions.Action;
import combatarena.actions.items.Item;
import combatarena.engine.BattleManagement;
import combatarena.entities.Enemy;
import combatarena.entities.Player;
import combatarena.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UIGame {

    private BattleManagement gameSnapshot;
    private final Scanner scanner;

    public UIGame(BattleManagement gameSnapshot) {
        this.gameSnapshot = gameSnapshot;
        this.scanner = new Scanner(System.in);
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
            System.out.println((i + 1) + ". " + enemy.getStatusSummary());
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
}
