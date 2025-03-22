package org.example.model;
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
public enum BuildingType {
    // Basic buildings available to all races
    TOWN_HALL("Town Hall", "Controls village development",
            new Resources(100, 100, 100, 0, 0, 0), 5),
    STONE_MINE("Stone Mine", "Produces stone daily",
            new Resources(50, 100, 20, 0, 0, 0), 3),
    LUMBER_MILL("Lumber Mill", "Produces wood daily",
            new Resources(100, 20, 20, 0, 0, 0), 3),
    GOLD_MINE("Gold Mine", "Produces gold daily",
            new Resources(100, 50, 50, 0, 0, 0), 3),
    SILO("Silo", "Increases resource storage capacity",
            new Resources(150, 150, 50, 0, 0, 0), 5),
    HOUSE("House", "Increases population capacity",
            new Resources(50, 100, 30, 0, 0, 0), 5),
    BARRACKS("Barracks", "Enables unit training",
            new Resources(150, 200, 100, 0, 0, 0), 3),

    // Race-specific buildings
    HUMAN_CHURCH("Church", "Provides bonuses to Human units",
            new Resources(200, 150, 200, 0, 0, 0), 3),
    ELF_MAGIC_TOWER("Magic Tower", "Enhances Elf magic abilities",
            new Resources(150, 250, 200, 0, 0, 0), 3),
    ORC_TOTEM("War Totem", "Strengthens Orc warriors",
            new Resources(250, 150, 150, 0, 0, 0), 3);

    private final String name;
    private final String description;
    private final Resources baseCost;
    private final int maxLevel;

    BuildingType(String name, String description, Resources baseCost, int maxLevel) {
        this.name = name;
        this.description = description;
        this.baseCost = baseCost;
        this.maxLevel = maxLevel;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Resources getBaseCost() { return baseCost; }
    public int getMaxLevel() { return maxLevel; }

    // Calculate cost based on level
    public Resources getCostForLevel(int currentLevel) {
        double multiplier = 1 + (currentLevel * 0.5);
        return new Resources(
                (int) (baseCost.getStone() * multiplier),
                (int) (baseCost.getWood() * multiplier),
                (int) (baseCost.getGold() * multiplier),
                0, 0, 0
        );
    }

    // Get buildings available for a specific race and town hall level
    public static BuildingType[] getAvailableBuildings(Race race, int townHallLevel) {
        // Basic buildings available to all races based on town hall level
        if (townHallLevel == 1) {
            return new BuildingType[] {TOWN_HALL, STONE_MINE, LUMBER_MILL, GOLD_MINE, HOUSE};
        } else if (townHallLevel == 2) {
            return new BuildingType[] {TOWN_HALL, STONE_MINE, LUMBER_MILL, GOLD_MINE, HOUSE, SILO};
        } else if (townHallLevel >= 3) {
            BuildingType[] basicBuildings = {TOWN_HALL, STONE_MINE, LUMBER_MILL, GOLD_MINE, HOUSE, SILO, BARRACKS};

            // Add race-specific buildings
            if (race == Race.HUMAN && townHallLevel >= 4) {
                return addBuildingType(basicBuildings, HUMAN_CHURCH);
            } else if (race == Race.ELF && townHallLevel >= 4) {
                return addBuildingType(basicBuildings, ELF_MAGIC_TOWER);
            } else if (race == Race.ORC && townHallLevel >= 4) {
                return addBuildingType(basicBuildings, ORC_TOTEM);
            }

            return basicBuildings;
        }

        // Default fallback
        return new BuildingType[] {TOWN_HALL, STONE_MINE, LUMBER_MILL, GOLD_MINE, HOUSE};
    }

    // Helper method to add a building type to an array
    private static BuildingType[] addBuildingType(BuildingType[] array, BuildingType newItem) {
        BuildingType[] result = new BuildingType[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = newItem;
        return result;
    }

    // Get daily resource production for a building
    public Resources getDailyProduction(int level, DifficultyLevel difficulty) {
        double multiplier = level * difficulty.getResourceMultiplier();

        switch (this) {
            case STONE_MINE:
                return new Resources((int) (50 * multiplier), 0, 0, 0, 0, 0);
            case LUMBER_MILL:
                return new Resources(0, (int) (50 * multiplier), 0, 0, 0, 0);
            case GOLD_MINE:
                return new Resources(0, 0, (int) (30 * multiplier), 0, 0, 0);
            default:
                return new Resources(0, 0, 0, 0, 0, 0);
        }
    }

    // Get storage capacity increase for a building
    public Resources getStorageIncrease(int level) {
        int baseIncrease = 200 * level;

        switch (this) {
            case SILO:
                return new Resources(baseIncrease, baseIncrease, 0, 0, 0, 0);
            case TOWN_HALL:
                return new Resources(100 * level, 100 * level, 100 * level, 0, 0, 0);
            default:
                return new Resources(0, 0, 0, 0, 0, 0);
        }
    }

    // Get population increase for a building
    public int getPopulationIncrease(int level) {
        switch (this) {
            case HOUSE:
                return 5 * level;
            case TOWN_HALL:
                return 10 * level;
            default:
                return 0;
        }
    }
}