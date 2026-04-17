package combatarena;

import combatarena.actions.items.Item;
import combatarena.engine.BattleManagement;
import combatarena.engine.SpeedOrderStrategy;
import combatarena.engine.TurnOrderStrategy;
import combatarena.entities.Enemy;
import combatarena.entities.Player;
import combatarena.level.Level;
import combatarena.ui.UIGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UIGame uiGame = new UIGame(scanner);

        boolean running = true;
        while (running) {
            System.out.println("SC2002 Turn-Based Combat");
            System.out.println("-------------------------");

            Player player = uiGame.choosePlayer();
            List<Item> selectedItems = uiGame.chooseItems();
            List<Item> originalItems = copyItems(selectedItems);
            player.setInventory(selectedItems);

            Level level = uiGame.chooseLevel();
            boolean replaySame = true;

            while (replaySame) {
                List<Enemy> initialEnemies = uiGame.createInitialEnemies(level);
                List<Enemy> backupEnemies = uiGame.createBackupEnemies(level);
                level.getBackupWave().clear();
                level.getBackupWave().addAll(backupEnemies);

                uiGame.printSetup(player, selectedItems, level, initialEnemies, backupEnemies);

                TurnOrderStrategy turnOrderStrategy = new SpeedOrderStrategy();
                BattleManagement battle = new BattleManagement(player, initialEnemies, turnOrderStrategy, level);
                battle.startBattle(uiGame);

                if (player.isAlive()) {
                    uiGame.showVictory(player, battle.getRoundNumber());
                } else {
                    uiGame.showDefeat(battle.getAliveEnemyCount(), battle.getRoundNumber());
                }

                String choice = uiGame.choosePostBattleOption();
                if ("1".equals(choice)) {
                    player.reset();
                    selectedItems = copyItems(originalItems);
                    player.setInventory(selectedItems);
                } else if ("2".equals(choice)) {
                    replaySame = false;
                } else {
                    replaySame = false;
                    running = false;
                }
            }
        }

        scanner.close();
    }

    private static List<Item> copyItems(List<Item> items) {
        List<Item> copied = new ArrayList<>();
        for (Item item : items) {
            copied.add(item.copy());
        }
        return copied;
    }
}
