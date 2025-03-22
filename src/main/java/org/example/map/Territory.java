package org.example.map;

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

public class Territory {
    private int id;
    private String name;
    private Player owner;  // null = neutral/enemy
    private List<Integer> adjacentTerritories;
    private Resources storedResources;
    private int defenseStrength;
    private Map<UnitType, Integer> defendingUnits;

    public Territory(int id, String name) {
        this.id = id;
        this.name = name;
        this.owner = null;
        this.adjacentTerritories = new ArrayList<>();

        // Random starting resources and defense
        this.storedResources = new Resources(
                20 + (int)(Math.random() * 100),
                20 + (int)(Math.random() * 100),
                10 + (int)(Math.random() * 50),
                0, 0, 0
        );

        this.defenseStrength = 10 + (int)(Math.random() * 20);
        this.defendingUnits = new HashMap<>();
    }

    // Add defending units based on difficulty and territory position
    public void setupDefendingUnits(DifficultyLevel difficulty, int distanceFromStart) {
        // Base defense increases with distance and difficulty
        int baseUnits = 5 + (distanceFromStart * 2);
        double multiplier = difficulty.getEnemyStrengthMultiplier();

        // Create a balanced unit composition
        Random random = new Random();
        UnitType[] enemyUnits;

        // Pick a random enemy race that's different from player's
        int raceChoice = random.nextInt(3);
        switch (raceChoice) {
            case 0:
                enemyUnits = UnitType.getUnitsForRace(Race.HUMAN);
                break;
            case 1:
                enemyUnits = UnitType.getUnitsForRace(Race.ELF);
                break;
            default:
                enemyUnits = UnitType.getUnitsForRace(Race.ORC);
        }

        // Distribute units - more advanced units appear further away
        int basicUnits = (int)(baseUnits * multiplier * (0.7 - (0.1 * distanceFromStart)));
        if (basicUnits < 1) basicUnits = 1;

        int mediumUnits = (int)(baseUnits * multiplier * (0.2 + (0.05 * distanceFromStart)));
        if (mediumUnits < 0) mediumUnits = 0;

        int advancedUnits = (int)(baseUnits * multiplier * (0.1 + (0.05 * distanceFromStart)));
        if (advancedUnits < 0) advancedUnits = 0;

        // Assign units
        defendingUnits.put(enemyUnits[0], basicUnits);
        defendingUnits.put(enemyUnits[1], mediumUnits);
        defendingUnits.put(enemyUnits[2], advancedUnits);
    }

    // Add an adjacent territory
    public void addAdjacentTerritory(int territoryId) {
        if (!adjacentTerritories.contains(territoryId)) {
            adjacentTerritories.add(territoryId);
        }
    }

    // Check if this territory is adjacent to the given territory
    public boolean isAdjacentTo(int territoryId) {
        return adjacentTerritories.contains(territoryId);
    }

    // Capture this territory
    public void captureBy(Player player) {
        this.owner = player;
        // Clear defending units when captured
        this.defendingUnits.clear();
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public Player getOwner() { return owner; }
    public List<Integer> getAdjacentTerritories() { return adjacentTerritories; }
    public Resources getStoredResources() { return storedResources; }
    public int getDefenseStrength() { return defenseStrength; }
    public Map<UnitType, Integer> getDefendingUnits() { return defendingUnits; }

    public void setName(String name) { this.name = name; }
    public void setDefenseStrength(int strength) { this.defenseStrength = strength; }
}