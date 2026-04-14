package combatarena;

import combatarena.actions.items.Potion;
import combatarena.actions.items.PowerStone;
import combatarena.actions.items.SmokeBomb;
import combatarena.actions.Action;
import combatarena.effects.ArcaneBuff;
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
import combatarena.skills.SpecialSkill;
import combatarena.util.ActionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("SC2002 Turn-Based Combat");
        System.out.println("==========================");

        Player player = choosePlayer(scanner);
        Level level = chooseLevel(scanner);

        TurnOrderStrategy turnOrderStrategy = new SpeedOrderStrategy();
        List<Enemy> enemies = new ArrayList<>(level.getInitialWave());

        BattleManagement battle = new BattleManagement(
                player,
                enemies,
                turnOrderStrategy,
                level
        );

        System.out.println();
        System.out.println("Battle starting...");
        System.out.println("Player: " + player.getClass().getSimpleName());
        System.out.println("Level: " + level.getDifficulty());
        System.out.println();

        battle.startBattle();

        if (player.isAlive()) {
            System.out.println("Battle ended. Player survived with HP: " + player.getHp());
        } else {
            System.out.println("Battle ended. Player was defeated.");
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
                Player warrior = new Warrior(createWarriorSkill());
                warrior.getInventory().add(new Potion());
                warrior.getInventory().add(new SmokeBomb());
                warrior.getInventory().add(new PowerStone());
                return warrior;
            }

            if ("2".equals(input)) {
                Player wizard = new Wizard(createWizardSkill());
                wizard.getInventory().add(new Potion());
                wizard.getInventory().add(new SmokeBomb());
                wizard.getInventory().add(new PowerStone());
                return wizard;
            }

            System.out.println("Invalid choice. Try again.");
        }
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
                return new Level1(createLevel1Enemies(enemyStrategy), createLevel1Backup(enemyStrategy));
            }

            if ("2".equals(input)) {
                return new Level2(createLevel2Enemies(enemyStrategy), createLevel2Backup(enemyStrategy));
            }

            if ("3".equals(input)) {
                return new Level3(createLevel3Enemies(enemyStrategy), createLevel3Backup(enemyStrategy));
            }

            System.out.println("Invalid choice. Try again.");
        }
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
        enemies.add(new Wolf(strategy));
        return enemies;
    }

    private static List<Enemy> createLevel1Backup(EnemyActionStrategy strategy) {
        List<Enemy> backup = new ArrayList<>();
        backup.add(new Goblin(strategy));
        return backup;
    }

    private static List<Enemy> createLevel2Enemies(EnemyActionStrategy strategy) {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Goblin(strategy));
        enemies.add(new Goblin(strategy));
        enemies.add(new Wolf(strategy));
        return enemies;
    }

    private static List<Enemy> createLevel2Backup(EnemyActionStrategy strategy) {
        List<Enemy> backup = new ArrayList<>();
        backup.add(new Goblin(strategy));
        backup.add(new Wolf(strategy));
        return backup;
    }

    private static List<Enemy> createLevel3Enemies(EnemyActionStrategy strategy) {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Wolf(strategy));
        enemies.add(new Wolf(strategy));
        enemies.add(new Goblin(strategy));
        enemies.add(new Goblin(strategy));
        return enemies;
    }

    private static List<Enemy> createLevel3Backup(EnemyActionStrategy strategy) {
        List<Enemy> backup = new ArrayList<>();
        backup.add(new Wolf(strategy));
        backup.add(new Wolf(strategy));
        return backup;
    }
}
