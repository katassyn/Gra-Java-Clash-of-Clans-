package org.example.map;

import java.util.*;
import org.example.combat.CombatManager;
import org.example.combat.CombatResult;
import org.example.map.GameMap;
import org.example.map.Territory;
import org.example.model.DifficultyLevel;
import org.example.model.Race;
import org.example.model.UnitType;
import org.example.player.Player;
import org.example.utils.Event;
import org.example.utils.EventManager;

public class GameMap {
    private static final int DEBUG_MODE = 1;

    private Map<Integer, Territory> territories;
    private int width;
    private int height;
    private int playerStartingTerritory;
    private DifficultyLevel difficulty;

    public GameMap(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
        initializeMap();
    }

    // Initialize the map with territories
    private void initializeMap() {
        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Initializing game map");
        }

        territories = new HashMap<>();

        // Map dimensions based on difficulty
        switch (difficulty) {
            case EASY:
                width = 5;
                height = 5;
                break;
            case NORMAL:
                width = 6;
                height = 6;
                break;
            case HARD:
                width = 7;
                height = 7;
                break;
            default:
                width = 5;
                height = 5;
        }

        // Create territories
        int id = 1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String name = "Territory " + id;
                Territory territory = new Territory(id, name);
                territories.put(id, territory);
                id++;
            }
        }

        // Set adjacent territories (grid-based)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentId = y * width + x + 1;
                Territory current = territories.get(currentId);

                // Check 4 directions (up, right, down, left)
                if (y > 0) {  // Up
                    int upId = (y - 1) * width + x + 1;
                    current.addAdjacentTerritory(upId);
                }
                if (x < width - 1) {  // Right
                    int rightId = y * width + (x + 1) + 1;
                    current.addAdjacentTerritory(rightId);
                }
                if (y < height - 1) {  // Down
                    int downId = (y + 1) * width + x + 1;
                    current.addAdjacentTerritory(downId);
                }
                if (x > 0) {  // Left
                    int leftId = y * width + (x - 1) + 1;
                    current.addAdjacentTerritory(leftId);
                }
            }
        }

        // Set player starting territory (top-left corner)
        playerStartingTerritory = 1;

        // Setup defending units based on distance from start
        for (Territory territory : territories.values()) {
            if (territory.getId() != playerStartingTerritory) {
                int distance = calculateDistance(territory.getId(), playerStartingTerritory);
                territory.setupDefendingUnits(difficulty, distance);
            }
        }

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Map initialized with " + territories.size() + " territories");
            System.out.println("[DEBUG] Player starting at territory " + playerStartingTerritory);
        }
    }

    // Calculate Manhattan distance between two territory IDs
    private int calculateDistance(int id1, int id2) {
        int x1 = (id1 - 1) % width;
        int y1 = (id1 - 1) / width;

        int x2 = (id2 - 1) % width;
        int y2 = (id2 - 1) / width;

        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    // Place player on starting territory
    public void placePlayerStart(Player player) {
        Territory startTerritory = territories.get(playerStartingTerritory);
        startTerritory.captureBy(player);
        player.setCurrentTerritory(startTerritory);

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Player placed on starting territory ID=" + playerStartingTerritory);
        }
    }

    // Display the entire map
    public void displayMap() {
        System.out.println("\n----- MAP -----");

        // Display column headers
        System.out.print("   ");
        for (int x = 0; x < width; x++) {
            System.out.print(" " + (x + 1) + "  ");
        }
        System.out.println();

        // Display grid
        for (int y = 0; y < height; y++) {
            // Row header
            System.out.print(" " + (char)('A' + y) + " ");

            // Territories in this row
            for (int x = 0; x < width; x++) {
                int id = y * width + x + 1;
                Territory territory = territories.get(id);

                if (territory.getOwner() != null) {
                    // Player-owned territory
                    System.out.print("[P] ");
                } else {
                    // Enemy/neutral territory
                    System.out.print("[" + id + "] ");
                }
            }
            System.out.println();
        }
        System.out.println("Legend: [P] = Player-owned, [#] = Enemy territory ID");
    }

    // Display attackable territories
    public boolean displayAttackableTerritories() {
        boolean hasAttackable = false;
        System.out.println("Territories you can attack:");

        for (Territory territory : territories.values()) {
            if (territory.getOwner() == null && isAdjacentToPlayerTerritory(territory)) {
                hasAttackable = true;
                System.out.println(territory.getId() + ". " + territory.getName() +
                        " (Estimated Defense: " + estimateDefenseStrength(territory) + ")");
            }
        }

        return hasAttackable;
    }

    // Check if a territory is adjacent to any player-owned territory
    private boolean isAdjacentToPlayerTerritory(Territory territory) {
        for (int adjId : territory.getAdjacentTerritories()) {
            Territory adjacent = territories.get(adjId);
            if (adjacent != null && adjacent.getOwner() != null) {
                return true;
            }
        }
        return false;
    }

    // Estimate defense strength (for display purposes)
    private String estimateDefenseStrength(Territory territory) {
        int totalUnits = 0;
        for (int count : territory.getDefendingUnits().values()) {
            totalUnits += count;
        }

        if (totalUnits < 10) {
            return "Weak";
        } else if (totalUnits < 20) {
            return "Moderate";
        } else if (totalUnits < 40) {
            return "Strong";
        } else {
            return "Very Strong";
        }
    }

    // Get territory by ID
    public Territory getTerritoryById(int id) {
        return territories.get(id);
    }

    // Capture a territory
    public void captureTerritory(int id, Player player) {
        Territory territory = territories.get(id);
        if (territory != null) {
            territory.captureBy(player);

            if (DEBUG_MODE == 1) {
                System.out.println("[DEBUG] Territory " + id + " captured by player");
            }
        }
    }

    // Lose a territory
    public void loseTerritory(int id) {
        Territory territory = territories.get(id);
        if (territory != null) {
            territory.captureBy(null);  // Set owner to null

            if (DEBUG_MODE == 1) {
                System.out.println("[DEBUG] Territory " + id + " lost by player");
            }
        }
    }

    // Get count of player-owned territories
    public int getPlayerTerritoryCount(Player player) {
        int count = 0;
        for (Territory territory : territories.values()) {
            if (territory.getOwner() == player) {
                count++;
            }
        }
        return count;
    }

    // Get total number of territories
    public int getTotalTerritories() {
        return territories.size();
    }

    // Check if the entire map is conquered
    public boolean isMapConquered(Player player) {
        return getPlayerTerritoryCount(player) == territories.size();
    }

    // Get first player territory (used when current territory is lost)
    public Territory getFirstPlayerTerritory(Player player) {
        for (Territory territory : territories.values()) {
            if (territory.getOwner() == player) {
                return territory;
            }
        }
        return null;
    }

    // Display player territories
    public void displayPlayerTerritories(Player player) {
        for (Territory territory : territories.values()) {
            if (territory.getOwner() == player) {
                System.out.println(territory.getId() + ". " + territory.getName());
            }
        }
    }

    // Get number of adjacent enemy territories
    public int getAdjacentEnemyTerritoryCount(Player player) {
        Set<Integer> adjacentEnemies = new HashSet<>();

        // Find all player territories
        for (Territory territory : territories.values()) {
            if (territory.getOwner() == player) {
                // Check all adjacent territories
                for (int adjId : territory.getAdjacentTerritories()) {
                    Territory adjacent = territories.get(adjId);
                    if (adjacent != null && adjacent.getOwner() == null) {
                        adjacentEnemies.add(adjId);
                    }
                }
            }
        }

        return adjacentEnemies.size();
    }

    // Get a random adjacent enemy territory
    public Territory getRandomAdjacentEnemyTerritory(Player player) {
        List<Territory> adjacentEnemies = new ArrayList<>();

        // Find all player territories
        for (Territory territory : territories.values()) {
            if (territory.getOwner() == player) {
                // Check all adjacent territories
                for (int adjId : territory.getAdjacentTerritories()) {
                    Territory adjacent = territories.get(adjId);
                    if (adjacent != null && adjacent.getOwner() == null) {
                        adjacentEnemies.add(adjacent);
                    }
                }
            }
        }

        if (adjacentEnemies.isEmpty()) {
            return null;
        }

        // Return a random enemy territory
        int randomIndex = (int)(Math.random() * adjacentEnemies.size());
        return adjacentEnemies.get(randomIndex);
    }
}