package org.example.player;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.example.combat.CombatManager;
import org.example.combat.CombatResult;
import org.example.map.GameMap;
import org.example.map.Territory;
import org.example.model.*;
import org.example.player.Player;
import org.example.utils.Event;
import org.example.utils.EventManager;

public class Player {
    private static final int DEBUG_MODE = 1; // Set to 0 for production

    private String name;
    private Race race;
    private DifficultyLevel difficulty;
    private Resources resources;
    private Map<BuildingType, Integer> buildings;  // Building type -> level
    private Map<UnitType, Integer> units;  // Unit type -> count
    private Map<UnitType, Integer> unitsInAttack; // Units currently attacking
    private int population;
    private int maxPopulation;
    private Territory currentTerritory;

    public Player(Race race, DifficultyLevel difficulty) {
        this.race = race;
        this.difficulty = difficulty;
        this.name = "Player";

        // Initialize resources based on difficulty
        this.resources = Resources.getStartingResources(difficulty);

        // Initialize buildings with level 1 Town Hall
        this.buildings = new HashMap<>();
        this.buildings.put(BuildingType.TOWN_HALL, 1);

        // Initialize empty units
        this.units = new HashMap<>();
        this.unitsInAttack = new HashMap<>();

        // Initialize population
        this.population = 0;
        this.maxPopulation = BuildingType.TOWN_HALL.getPopulationIncrease(1);

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Player initialized: Race=" + race.getName() +
                    ", Difficulty=" + difficulty.getName());
        }
    }

    // Collect daily resources from all buildings
    public void collectDailyResources() {
        Resources dailyResources = new Resources(0, 0, 0, 0, 0, 0);

        for (Map.Entry<BuildingType, Integer> entry : buildings.entrySet()) {
            BuildingType buildingType = entry.getKey();
            int level = entry.getValue();

            Resources production = buildingType.getDailyProduction(level, difficulty);
            dailyResources.add(production);

            if (DEBUG_MODE == 1 && (production.getStone() > 0 || production.getWood() > 0 || production.getGold() > 0)) {
                System.out.println("[DEBUG] Collected from " + buildingType.getName() +
                        ": Stone=" + production.getStone() +
                        ", Wood=" + production.getWood() +
                        ", Gold=" + production.getGold());
            }
        }

        // Add daily resources with limit check
        resources.addWithLimit(dailyResources);

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Total daily collection: Stone=" + dailyResources.getStone() +
                    ", Wood=" + dailyResources.getWood() +
                    ", Gold=" + dailyResources.getGold());
        }
    }

    // Display player resources
    public void displayResources() {
        System.out.println(resources.toString());
    }

    // Display player status
    public void displayStatus() {
        System.out.println("Resources: " + resources.toString());
        System.out.println("Population: " + population + "/" + maxPopulation);

        // Display number of buildings
        System.out.println("Buildings: " + buildings.size() + " (Town Hall level: " +
                buildings.get(BuildingType.TOWN_HALL) + ")");

        // Display total units
        int totalUnits = 0;
        for (int count : units.values()) {
            totalUnits += count;
        }
        System.out.println("Units: " + totalUnits);
    }

    // Display detailed building information
    public void displayDetailedBuildings() {
        for (Map.Entry<BuildingType, Integer> entry : buildings.entrySet()) {
            BuildingType type = entry.getKey();
            int level = entry.getValue();
            System.out.println(type.getName() + " (Level " + level + "/" + type.getMaxLevel() + ")");
            System.out.println("  " + type.getDescription());

            // Show production for resource buildings
            Resources production = type.getDailyProduction(level, difficulty);
            if (production.getStone() > 0) {
                System.out.println("  Daily stone production: " + production.getStone());
            }
            if (production.getWood() > 0) {
                System.out.println("  Daily wood production: " + production.getWood());
            }
            if (production.getGold() > 0) {
                System.out.println("  Daily gold production: " + production.getGold());
            }

            // Show capacity increase for storage buildings
            Resources storage = type.getStorageIncrease(level);
            if (storage.getMaxStone() > 0) {
                System.out.println("  Stone storage: +" + storage.getMaxStone());
            }
            if (storage.getMaxWood() > 0) {
                System.out.println("  Wood storage: +" + storage.getMaxWood());
            }
            if (storage.getMaxGold() > 0) {
                System.out.println("  Gold storage: +" + storage.getMaxGold());
            }

            // Show population increase
            int pop = type.getPopulationIncrease(level);
            if (pop > 0) {
                System.out.println("  Population capacity: +" + pop);
            }
        }
    }

    // Display available buildings for construction/upgrade
    public void displayAvailableBuildings() {
        int townHallLevel = buildings.getOrDefault(BuildingType.TOWN_HALL, 1);
        BuildingType[] availableTypes = BuildingType.getAvailableBuildings(race, townHallLevel);

        int index = 1;
        for (BuildingType type : availableTypes) {
            int currentLevel = buildings.getOrDefault(type, 0);

            // If at max level, skip
            if (currentLevel >= type.getMaxLevel()) {
                continue;
            }

            // If building exists, show upgrade option
            if (currentLevel > 0) {
                Resources upgradeCost = type.getCostForLevel(currentLevel + 1);
                System.out.println(index + ". Upgrade " + type.getName() + " to level " + (currentLevel + 1) +
                        " (Cost: Stone=" + upgradeCost.getStone() +
                        ", Wood=" + upgradeCost.getWood() +
                        ", Gold=" + upgradeCost.getGold() + ")");
            } else {
                // Otherwise show new building option
                Resources buildCost = type.getBaseCost();
                System.out.println(index + ". Build new " + type.getName() +
                        " (Cost: Stone=" + buildCost.getStone() +
                        ", Wood=" + buildCost.getWood() +
                        ", Gold=" + buildCost.getGold() + ")");
            }

            index++;
        }
    }

    // Build or upgrade a building
    public void buildOrUpgrade(int choice) {
        int townHallLevel = buildings.getOrDefault(BuildingType.TOWN_HALL, 1);
        BuildingType[] availableTypes = BuildingType.getAvailableBuildings(race, townHallLevel);

        // Validate choice
        if (choice < 1 || choice > availableTypes.length) {
            System.out.println("Invalid choice.");
            return;
        }

        BuildingType selectedType = availableTypes[choice - 1];
        int currentLevel = buildings.getOrDefault(selectedType, 0);

        // Check if at max level
        if (currentLevel >= selectedType.getMaxLevel()) {
            System.out.println(selectedType.getName() + " is already at maximum level.");
            return;
        }

        // Calculate cost
        Resources cost;
        if (currentLevel > 0) {
            // Upgrade
            cost = selectedType.getCostForLevel(currentLevel + 1);
        } else {
            // New building
            cost = selectedType.getBaseCost();
        }

        // Check resources
        if (!resources.hasEnough(cost)) {
            System.out.println("Not enough resources to build/upgrade " + selectedType.getName());
            return;
        }

        // Deduct resources
        resources.subtract(cost);

        // Update building
        buildings.put(selectedType, currentLevel + 1);

        // Update storage capacity
        Resources storageIncrease = selectedType.getStorageIncrease(currentLevel + 1);
        resources.increaseMaxCapacity(
                storageIncrease.getMaxStone(),
                storageIncrease.getMaxWood(),
                storageIncrease.getMaxGold()
        );

        // Update population capacity
        maxPopulation += selectedType.getPopulationIncrease(currentLevel + 1);

        if (currentLevel > 0) {
            System.out.println(selectedType.getName() + " upgraded to level " + (currentLevel + 1));
        } else {
            System.out.println("New " + selectedType.getName() + " built");
        }

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Building update: " + selectedType.getName() +
                    " now at level " + buildings.get(selectedType));
        }
    }

    // Display available units for training
    public void displayAvailableUnits() {
        // Check if player has barracks
        if (!buildings.containsKey(BuildingType.BARRACKS)) {
            System.out.println("You need to build Barracks first to train units.");
            return;
        }

        UnitType[] raceUnits = UnitType.getUnitsForRace(race);

        int index = 1;
        for (UnitType unitType : raceUnits) {
            Resources cost = unitType.getCost();
            System.out.println(index + ". " + unitType.getName() +
                    " (Cost: Stone=" + cost.getStone() +
                    ", Wood=" + cost.getWood() +
                    ", Gold=" + cost.getGold() + ")");
            System.out.println("   Attack: " + unitType.getAttackPower() +
                    ", Magic: " + unitType.getMagicPower() +
                    ", Defense: " + unitType.getDefense());
            index++;
        }
    }

    // Train units
    public void trainUnits(int choice, int count) {
        // Check if player has barracks
        if (!buildings.containsKey(BuildingType.BARRACKS)) {
            System.out.println("You need to build Barracks first to train units.");
            return;
        }

        // Validate choice
        UnitType[] raceUnits = UnitType.getUnitsForRace(race);
        if (choice < 1 || choice > raceUnits.length) {
            System.out.println("Invalid unit choice.");
            return;
        }

        // Validate count
        if (count <= 0) {
            System.out.println("Invalid unit count.");
            return;
        }

        UnitType selectedType = raceUnits[choice - 1];

        // Check population capacity
        if (population + count > maxPopulation) {
            System.out.println("Not enough population capacity. Build more houses.");
            return;
        }

        // Calculate total cost
        Resources unitCost = selectedType.getCost();
        Resources totalCost = new Resources(
                unitCost.getStone() * count,
                unitCost.getWood() * count,
                unitCost.getGold() * count,
                0, 0, 0
        );

        // Check resources
        if (!resources.hasEnough(totalCost)) {
            System.out.println("Not enough resources to train " + count + " " + selectedType.getName() + " units.");
            return;
        }

        // Deduct resources
        resources.subtract(totalCost);

        // Add units
        int currentCount = units.getOrDefault(selectedType, 0);
        units.put(selectedType, currentCount + count);

        // Update population
        population += count;

        System.out.println("Trained " + count + " " + selectedType.getName() + " units.");

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Units trained: " + count + " " + selectedType.getName() +
                    ", Total: " + units.get(selectedType));
        }
    }

    // Display all units
    public void displayUnits() {
        if (units.isEmpty()) {
            System.out.println("You don't have any units.");
            return;
        }

        for (Map.Entry<UnitType, Integer> entry : units.entrySet()) {
            UnitType type = entry.getKey();
            int count = entry.getValue();

            System.out.println(type.getName() + ": " + count + " units");
            System.out.println("  Attack: " + type.getAttackPower() +
                    ", Magic: " + type.getMagicPower() +
                    ", Defense: " + type.getDefense());
        }
    }

    // Display detailed unit information
    public void displayDetailedUnits() {
        if (units.isEmpty()) {
            System.out.println("You don't have any units.");
            return;
        }

        int totalUnits = 0;
        int totalAttackPower = 0;
        int totalMagicPower = 0;
        int totalDefense = 0;

        for (Map.Entry<UnitType, Integer> entry : units.entrySet()) {
            UnitType type = entry.getKey();
            int count = entry.getValue();

            System.out.println(type.getName() + ": " + count + " units");
            System.out.println("  Attack: " + type.getAttackPower() +
                    ", Magic: " + type.getMagicPower() +
                    ", Defense: " + type.getDefense());

            totalUnits += count;
            totalAttackPower += type.getAttackPower() * count;
            totalMagicPower += type.getMagicPower() * count;
            totalDefense += type.getDefense() * count;
        }

        System.out.println("\nTotal army strength:");
        System.out.println("Units: " + totalUnits);
        System.out.println("Attack Power: " + totalAttackPower);
        System.out.println("Magic Power: " + totalMagicPower);
        System.out.println("Defense: " + totalDefense);
    }

    // Check if player has any units
    public boolean hasUnits() {
        if (units.isEmpty()) {
            return false;
        }

        for (int count : units.values()) {
            if (count > 0) {
                return true;
            }
        }

        return false;
    }

    // Get count of a specific unit type
    public int getUnitCount(UnitType unitType) {
        return units.getOrDefault(unitType, 0);
    }

    // Get available unit types
    public List<UnitType> getAvailableUnitTypes() {
        List<UnitType> result = new ArrayList<>();

        for (Map.Entry<UnitType, Integer> entry : units.entrySet()) {
            if (entry.getValue() > 0) {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    // Assign units to attack
    public void assignUnitsToAttack(UnitType unitType, int count) {
        int available = units.getOrDefault(unitType, 0);

        if (count <= 0) {
            return;
        }

        if (count > available) {
            count = available;
            System.out.println("Only " + count + " " + unitType.getName() + " units available.");
        }

        // Remove from available units
        units.put(unitType, available - count);

        // Add to attack units
        int attackingCount = unitsInAttack.getOrDefault(unitType, 0);
        unitsInAttack.put(unitType, attackingCount + count);

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Assigned to attack: " + count + " " + unitType.getName());
        }
    }

    // Return attacking units (survivors) to available units
    public void returnAttackingUnits(Map<UnitType, Integer> survivors) {
        for (Map.Entry<UnitType, Integer> entry : survivors.entrySet()) {
            UnitType type = entry.getKey();
            int count = entry.getValue();

            // Add to available units
            int available = units.getOrDefault(type, 0);
            units.put(type, available + count);

            if (DEBUG_MODE == 1) {
                System.out.println("[DEBUG] Returned from attack: " + count + " " + type.getName());
            }
        }

        // Clear attacking units
        unitsInAttack.clear();
    }

    // Get attacking units
    public Map<UnitType, Integer> getAttackingUnits() {
        return unitsInAttack;
    }

    // Add resources from conquest
    public void addResources(Resources captured) {
        resources.add(captured);

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Resources added from conquest: " +
                    "Stone=" + captured.getStone() +
                    ", Wood=" + captured.getWood() +
                    ", Gold=" + captured.getGold());
        }
    }

    // Getters and setters
    public Race getRace() { return race; }
    public DifficultyLevel getDifficulty() { return difficulty; }
    public int getTownHallLevel() { return buildings.getOrDefault(BuildingType.TOWN_HALL, 1); }
    public Resources getResources() { return resources; }
    public Map<BuildingType, Integer> getBuildings() { return buildings; }
    public Map<UnitType, Integer> getUnits() { return units; }
    public int getPopulation() { return population; }
    public int getMaxPopulation() { return maxPopulation; }

    public Territory getCurrentTerritory() { return currentTerritory; }
    public void setCurrentTerritory(Territory territory) { this.currentTerritory = territory; }
}