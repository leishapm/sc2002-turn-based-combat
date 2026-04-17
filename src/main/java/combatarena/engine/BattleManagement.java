package combatarena.engine;

import combatarena.actions.Action;
import combatarena.actions.BasicAttack;
import combatarena.actions.Defend;
import combatarena.actions.items.Item;
import combatarena.actions.items.Potion;
import combatarena.actions.items.PowerStone;
import combatarena.actions.items.SmokeBomb;
import combatarena.effects.ArcaneBuff;
import combatarena.entities.Character;
import combatarena.entities.Enemy;
import combatarena.entities.Player;
import combatarena.effects.StatusEffect;
import combatarena.effects.Stun;
import combatarena.level.Level;
import combatarena.ui.UIGame;
import combatarena.util.ActionResult;

import java.util.ArrayList;
import java.util.List;

public class BattleManagement {

    private final Player player;
    private final List<Enemy> enemies;
    private List<Character> turnOrderList;
    private int currentTurnIndex;
    private int roundNumber;
    private final TurnOrderStrategy turnOrderStrategy;
    private final Level selectedLevel;
    private UIGame battleUi;

    public BattleManagement(Player player, List<Enemy> enemies,
                            TurnOrderStrategy strategy, Level level) {
        this.player = player;
        this.enemies = enemies;
        this.turnOrderStrategy = strategy;
        this.selectedLevel = level;
        this.turnOrderList = new ArrayList<>();
        this.currentTurnIndex = 0;
        this.roundNumber = 1;
        this.battleUi = null;
    }

    public void startBattle() {
        if (battleUi == null) {
            battleUi = new UIGame(this);
        }
        startBattle(battleUi);
    }

    public void startBattle(UIGame ui) {
        this.battleUi = ui;
        if (ui != null) {
            ui.setGameSnapshot(this);
        }
        turnOrderList = determineTurnOrder();
        ui.printRoundHeader(roundNumber, turnOrderList);

        while (!isBattleOver()) {
            Character current = nextTurn(ui);
            if (current == null) {
                continue;
            }

            if (!current.isAlive()) {
                ui.printEliminatedSkip(current, false);
                continue;
            }

            boolean stunnedBeforeUpdate = current.isStunned();
            current.updateEffects();

            if (!current.isAlive()) {
                ui.printEliminatedSkip(current, stunnedBeforeUpdate);
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
                    ui.printStunnedSkip(current, true);
                } else {
                    ui.printStunnedSkip(current, false);
                }
                continue;
            }

            if (current instanceof Player playerEntity) {
                executePlayerTurn(playerEntity, ui);
            } else if (current instanceof Enemy enemy) {
                executeEnemyTurn(enemy, ui);
            }

            if (isBattleOver()) {
                break;
            }
        }

        if (player != null) {
            player.clearArcaneBuffs();
        }
    }

    public void executeTurn(ActionContext context) {
        if (context == null || context.getUser() == null || context.getTargets() == null || context.getTargets().isEmpty()) {
            return;
        }

        ActionResult result;
        if (context.getSelectedItem() != null) {
            result = context.getSelectedItem().use(context);
        } else {
            result = new BasicAttack().execute(context);
        }

        for (Character target : context.getTargets()) {
            applyResult(target, result);
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

    private void executeEnemyTurn(Enemy enemy, UIGame ui) {
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

        ui.printDamageLine(
                getDisplayName(enemy),
                action.getClass().getSimpleName(),
                getDisplayName(player),
                beforeHp,
                afterHp,
                enemy.getAttack(),
                player.getDefense(),
                !player.isAlive() ? " | " + getDisplayName(player) + " was defeated." : ""
        );
    }

    private void executePlayerTurn(Player playerEntity, UIGame ui) {
        List<Enemy> aliveEnemies = getAliveEnemies();
        if (aliveEnemies.isEmpty()) {
            return;
        }

        // Cooldown decrements once when the player actually starts their turn.
        playerEntity.decrementCooldown();

        while (true) {
            String input = ui.promptPlayerAction(playerEntity);

            if ("1".equals(input)) {
                Enemy target = ui.chooseTarget(aliveEnemies);
                if (target == null) {
                    return;
                }

                int beforeHp = target.getHp();
                Action action = new BasicAttack();
                ActionResult result = action.execute(new ActionContext(playerEntity, List.of(target), null));
                applyResult(target, result);
                int afterHp = target.getHp();

                ui.printDamageLine(
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
                int beforeDef = playerEntity.getDefense();
                Action defend = new Defend();
                ActionResult result = defend.execute(new ActionContext(playerEntity, List.of(playerEntity), null));
                for (combatarena.effects.StatusEffect effect : result.getEffectsApplied()) {
                    playerEntity.addEffect(effect);
                }
                int afterDef = playerEntity.getDefense();
                ui.printDefendLine(getDisplayName(playerEntity), beforeDef, afterDef);
                return;
            }

            if ("3".equals(input)) {
                Item item = ui.chooseItem(playerEntity);
                if (item == null) {
                    continue;
                }

                if (item instanceof Potion) {
                    Character target = playerEntity;
                    int beforeHp = target.getHp();
                    ActionResult result = item.use(new ActionContext(playerEntity, List.of(target), item));
                    applyResult(target, result);
                    int afterHp = target.getHp();

                    ui.printPotionLine(getDisplayName(playerEntity), beforeHp, afterHp, result.getHealAmount());
                    ui.setLastConsumedItemName("Potion");
                    return;
                }

                if (item instanceof SmokeBomb) {
                    Character target = playerEntity;
                    ActionResult result = item.use(new ActionContext(playerEntity, List.of(target), item));
                    applyResult(target, result);
                    ui.printSmokeBombLine(getDisplayName(playerEntity));
                    ui.setLastConsumedItemName("Smoke Bomb");
                    return;
                }

                if (item instanceof PowerStone) {
                    if ("Arcane Blast".equals(getSkillDisplayName(playerEntity))) {
                        item.use(new ActionContext(playerEntity, new ArrayList<>(aliveEnemies), item));
                        executeArcaneBlast(playerEntity, new ArrayList<>(aliveEnemies), ui, true);
                    } else {
                        Enemy target = ui.chooseTarget(aliveEnemies);
                        if (target == null) {
                            return;
                        }

                        int beforeHp = target.getHp();
                        ActionResult result = item.use(new ActionContext(playerEntity, List.of(target), item));
                        applyResult(target, result);
                        int afterHp = target.getHp();

                        ui.printPowerStoneLine(
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
                    }
                    ui.setLastConsumedItemName("Power Stone");
                    return;
                }

                return;
            }

            if ("4".equals(input)) {
                if (!playerEntity.isSkillAvailable() || playerEntity.getSpecialSkill() == null) {
                    ui.printMessage("Special Skill not available.");
                    continue;
                }

                String skillName = getSkillDisplayName(playerEntity);

                if ("Arcane Blast".equals(skillName)) {
                    executeArcaneBlast(playerEntity, new ArrayList<>(aliveEnemies), ui, false);
                } else {
                    Enemy target = ui.chooseTarget(aliveEnemies);
                    if (target == null) {
                        return;
                    }

                    int beforeHp = target.getHp();
                    ActionResult result = playerEntity.getSpecialSkill().execute(
                            new ActionContext(playerEntity, List.of(target), null));
                    applyResult(target, result);
                    int afterHp = target.getHp();

                    playerEntity.setSpecialSkillCd(3);

                    ui.printSpecialSkillLine(
                            getDisplayName(playerEntity),
                            skillName,
                            getDisplayName(target),
                            beforeHp,
                            afterHp,
                            playerEntity.getAttack(),
                            target.getDefense(),
                            result,
                            playerEntity.getSpecialSkillCd(),
                            !target.isAlive()
                    );
                }
                return;
            }

            ui.printMessage("Invalid choice. Try again.");
        }
    }

    private void executeArcaneBlast(Player playerEntity, List<Enemy> targets, UIGame ui, boolean triggeredByPowerStone) {
        int killCount = 0;
        List<String> hitLines = new ArrayList<>();
        for (Enemy target : targets) {
            if (target == null || !target.isAlive()) {
                continue;
            }

            int beforeHp = target.getHp();
            ActionResult targetResult = playerEntity.getSpecialSkill().execute(
                    new ActionContext(playerEntity, List.of(target), null));
            int appliedDamage = target.takeDamage(targetResult.getDamageGiven());
            int afterHp = target.getHp();
            if (!target.isAlive()) {
                killCount++;
            }

            for (StatusEffect effect : targetResult.getEffectsApplied()) {
                if (effect instanceof ArcaneBuff) {
                    playerEntity.addEffect(new ArcaneBuff());
                }
            }

            hitLines.add(getDisplayName(target) + ": HP: " + beforeHp + " -> " + afterHp
                    + " (dmg: " + playerEntity.getAttack() + "-" + target.getDefense()
                    + "=" + appliedDamage + ")"
                    + (!target.isAlive() ? " | " + getDisplayName(target) + " was defeated." : ""));
        }

        if (!triggeredByPowerStone) {
            playerEntity.setSpecialSkillCd(3);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getDisplayName(playerEntity));
        if (triggeredByPowerStone) {
            sb.append(" -> Item -> Power Stone used -> Arcane Blast triggered -> ");
        } else {
            sb.append(" -> Arcane Blast -> ");
        }
        sb.append(String.join(" + ", hitLines));
        if (killCount > 0) {
            sb.append(" | +").append(killCount * 10).append(" ATK gained (")
                    .append(killCount).append(" kill").append(killCount > 1 ? "s" : "").append(")");
        }
        if (triggeredByPowerStone) {
            sb.append(" | Cooldown unchanged -> ")
                    .append(playerEntity.getSpecialSkillCd())
                    .append(" (Power Stone does not affect cooldown) | Power Stone consumed");
        } else {
            sb.append(" | Cooldown: ").append(playerEntity.getSpecialSkillCd());
        }
        ui.printMessage(sb.toString());
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

    public void applyResult(ActionResult result) {
        if (result == null || player == null) {
            return;
        }
        applyResult(player, result);
    }

    public Character nextTurn(UIGame ui) {
        if (turnOrderList == null || turnOrderList.isEmpty()) {
            return null;
        }

        if (currentTurnIndex >= turnOrderList.size()) {
            checkAndSpawnBackup(ui);

            ui.printEndOfRound(player, enemies, roundNumber);

            currentTurnIndex = 0;
            roundNumber++;
            turnOrderList = determineTurnOrder();

            if (!isBattleOver()) {
                ui.printRoundHeader(roundNumber, turnOrderList);
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

    public void checkAndSpawnBackup() {
        if (selectedLevel == null || enemies == null || !hasPendingBackup()) {
            return;
        }

        List<Enemy> backupWave = selectedLevel.getBackupWave();
        enemies.addAll(backupWave);
        selectedLevel.setBackupSpawnTriggered(true);
    }

    private void checkAndSpawnBackup(UIGame ui) {
        if (!isWaveCleared() || !hasPendingBackup()) {
            return;
        }

        if (selectedLevel == null || enemies == null || !hasPendingBackup()) {
            return;
        }

        List<Enemy> backupWave = selectedLevel.getBackupWave();
        if (ui != null) {
            ui.printBackupWaveAnnouncement(backupWave);
        }

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
