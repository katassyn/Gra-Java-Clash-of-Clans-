package org.example.combat;

import java.util.*;
import org.example.combat.CombatManager;
import org.example.combat.CombatResult;
import org.example.map.GameMap;
import org.example.map.Territory;
import org.example.model.DifficultyLevel;
import org.example.model.Race;
import org.example.model.Resources;
import org.example.model.UnitType;
import org.example.player.Player;
import org.example.utils.Event;
import org.example.utils.EventManager;

public class CombatManager {
    private static final int DEBUG_MODE = 1;

    public CombatManager() {
    }

    // Resolve combat between player and territory
    public CombatResult resolveCombat(Player player, Territory target) {
        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Resolving combat: Player vs Territory " + target.getId());
        }

        // Get attacking units
        Map<UnitType, Integer> attackingUnits = player.getAttackingUnits();

        // Get defending units
        Map<UnitType, Integer> defendingUnits = target.getDefendingUnits();

        // Calculate attack strength
        int attackStrength = calculateAttackStrength(attackingUnits, player.getRace());
        int defenseStrength = calculateDefenseStrength(defendingUnits, target.getDefenseStrength());

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Combat stats - Attack: " + attackStrength + ", Defense: " + defenseStrength);
        }

        // Calculate casualties
        double winProbability = attackStrength / (double)(attackStrength + defenseStrength);
        boolean playerVictory = Math.random() < winProbability;

        int playerLosses;
        int enemyLosses;
        Map<UnitType, Integer> survivingUnits = new HashMap<>();

        if (playerVictory) {
            // Player wins - calculate losses
            double lossFactor = 0.3 + (0.4 * defenseStrength / (double)(attackStrength + defenseStrength));
            playerLosses = (int)(calculateTotalUnits(attackingUnits) * lossFactor);
            enemyLosses = calculateTotalUnits(defendingUnits);  // All defenders are lost

            // Calculate surviving units
            survivingUnits = calculateSurvivingUnits(attackingUnits, lossFactor);

            if (DEBUG_MODE == 1) {
                System.out.println("[DEBUG] Player victory - Losses: " + playerLosses + ", Enemy Losses: " + enemyLosses);
            }

        } else {
            // Player loses - more casualties
            double lossFactor = 0.6 + (0.2 * defenseStrength / (double)(attackStrength + defenseStrength));
            playerLosses = (int)(calculateTotalUnits(attackingUnits) * lossFactor);
            enemyLosses = (int)(calculateTotalUnits(defendingUnits) * 0.3);  // Some defenders survive

            // Calculate surviving units
            survivingUnits = calculateSurvivingUnits(attackingUnits, lossFactor);

            if (DEBUG_MODE == 1) {
                System.out.println("[DEBUG] Player defeat - Losses: " + playerLosses + ", Enemy Losses: " + enemyLosses);
            }
        }

        // Return combat result with captured resources if player wins
        Resources capturedResources = playerVictory ? target.getStoredResources() : new Resources(0, 0, 0, 0, 0, 0);

        // Update player's units
        player.returnAttackingUnits(survivingUnits);

        // Update territory's defending units if player loses
        if (!playerVictory) {
            // Reduce defending units by enemy losses
            updateDefendingUnits(target, enemyLosses);
        }

        return new CombatResult(playerVictory, playerLosses, enemyLosses, capturedResources, survivingUnits);
    }

    // Resolve enemy attack against player
    public CombatResult resolveEnemyAttack(Territory attackingTerritory, Player player) {
        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Resolving enemy attack: Territory " + attackingTerritory.getId() + " vs Player");
        }

        // Get attacking enemy units
        Map<UnitType, Integer> attackingUnits = attackingTerritory.getDefendingUnits();

        // Get player's defending units (all available units)
        Map<UnitType, Integer> defendingUnits = player.getUnits();

        // Calculate strengths
        int attackStrength = calculateAttackStrength(attackingUnits, null);  // Null race for enemy
        int defenseStrength = calculateDefenseStrength(defendingUnits, player.getCurrentTerritory().getDefenseStrength());

        // Apply race defensive bonus
        defenseStrength = (int)(defenseStrength * player.getRace().getDefenseMultiplier());

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Enemy attack stats - Attack: " + attackStrength +
                    ", Defense: " + defenseStrength);
        }

        // Calculate result
        double defenseProbability = defenseStrength / (double)(attackStrength + defenseStrength);
        boolean playerVictory = Math.random() < defenseProbability;

        int playerLosses;
        int enemyLosses;
        Map<UnitType, Integer> survivingPlayerUnits = new HashMap<>();

        if (playerVictory) {
            // Player successfully defends
            double playerLossFactor = 0.2 + (0.3 * attackStrength / (double)(attackStrength + defenseStrength));
            double enemyLossFactor = 0.7 + (0.2 * defenseStrength / (double)(attackStrength + defenseStrength));

            playerLosses = (int)(calculateTotalUnits(defendingUnits) * playerLossFactor);
            enemyLosses = (int)(calculateTotalUnits(attackingUnits) * enemyLossFactor);

            // Calculate surviving units
            survivingPlayerUnits = calculateSurvivingUnits(defendingUnits, playerLossFactor);

            if (DEBUG_MODE == 1) {
                System.out.println("[DEBUG] Defense successful - Player Losses: " + playerLosses +
                        ", Enemy Losses: " + enemyLosses);
            }

        } else {
            // Player fails to defend
            double playerLossFactor = 0.5 + (0.3 * attackStrength / (double)(attackStrength + defenseStrength));
            double enemyLossFactor = 0.4;

            playerLosses = (int)(calculateTotalUnits(defendingUnits) * playerLossFactor);
            enemyLosses = (int)(calculateTotalUnits(attackingUnits) * enemyLossFactor);

            // Calculate surviving units
            survivingPlayerUnits = calculateSurvivingUnits(defendingUnits, playerLossFactor);

            if (DEBUG_MODE == 1) {
                System.out.println("[DEBUG] Defense failed - Player Losses: " + playerLosses +
                        ", Enemy Losses: " + enemyLosses);
            }
        }

        // Update player's units
        for (Map.Entry<UnitType, Integer> entry : survivingPlayerUnits.entrySet()) {
            UnitType type = entry.getKey();
            int survivors = entry.getValue();
            player.getUnits().put(type, survivors);
        }

        // Update attacking territory's units if player wins
        if (playerVictory) {
            updateDefendingUnits(attackingTerritory, enemyLosses);
        }

        // No resources captured in defense
        Resources noCapturedResources = new Resources(0, 0, 0, 0, 0, 0);

        return new CombatResult(playerVictory, playerLosses, enemyLosses, noCapturedResources, survivingPlayerUnits);
    }

    // Calculate attack strength based on units
    private int calculateAttackStrength(Map<UnitType, Integer> units, Race race) {
        int strength = 0;

        for (Map.Entry<UnitType, Integer> entry : units.entrySet()) {
            UnitType type = entry.getKey();
            int count = entry.getValue();

            // Calculate base attack value
            int unitStrength = (type.getAttackPower() + type.getMagicPower()) * count;

            // Apply race modifier if available
            if (race != null) {
                unitStrength = (int)(unitStrength *
                        (type.getAttackPower() > type.getMagicPower() ?
                                race.getStrengthMultiplier() : race.getMagicMultiplier()));
            }

            strength += unitStrength;
        }

        return strength;
    }

    // Calculate defense strength based on units and territory
    private int calculateDefenseStrength(Map<UnitType, Integer> units, int baseDefense) {
        int strength = baseDefense;  // Start with territory base defense

        for (Map.Entry<UnitType, Integer> entry : units.entrySet()) {
            UnitType type = entry.getKey();
            int count = entry.getValue();

            strength += type.getDefense() * count;
        }

        return strength;
    }

    // Calculate total number of units
    private int calculateTotalUnits(Map<UnitType, Integer> units) {
        int total = 0;
        for (int count : units.values()) {
            total += count;
        }
        return total;
    }

    // Calculate surviving units after battle
    private Map<UnitType, Integer> calculateSurvivingUnits(Map<UnitType, Integer> units, double lossFactor) {
        Map<UnitType, Integer> survivors = new HashMap<>();

        for (Map.Entry<UnitType, Integer> entry : units.entrySet()) {
            UnitType type = entry.getKey();
            int count = entry.getValue();

            // Calculate survivors
            int lost = (int)(count * lossFactor);
            int survived = count - lost;

            if (survived > 0) {
                survivors.put(type, survived);
            }
        }

        return survivors;
    }

    // Update defending units after a battle
    private void updateDefendingUnits(Territory territory, int losses) {
        Map<UnitType, Integer> defendingUnits = territory.getDefendingUnits();
        int totalUnits = calculateTotalUnits(defendingUnits);

        if (totalUnits <= 0) {
            return;
        }

        // Calculate loss factor
        double lossFactor = losses / (double)totalUnits;

        // Update each unit type
        for (Map.Entry<UnitType, Integer> entry : new HashMap<>(defendingUnits).entrySet()) {
            UnitType type = entry.getKey();
            int count = entry.getValue();

            // Calculate losses for this type
            int typeLosses = (int)(count * lossFactor);
            int survivors = count - typeLosses;

            if (survivors <= 0) {
                defendingUnits.remove(type);
            } else {
                defendingUnits.put(type, survivors);
            }
        }
    }
}