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
public class Resources {
    private int stone;
    private int wood;
    private int gold;
    private int maxStone;
    private int maxWood;
    private int maxGold;

    public Resources(int stone, int wood, int gold, int maxStone, int maxWood, int maxGold) {
        this.stone = stone;
        this.wood = wood;
        this.gold = gold;
        this.maxStone = maxStone;
        this.maxWood = maxWood;
        this.maxGold = maxGold;
    }

    // Get starting resources based on difficulty
    public static Resources getStartingResources(DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY:
                return new Resources(300, 300, 200, 1000, 1000, 1000);
            case NORMAL:
                return new Resources(200, 200, 150, 500, 500, 500);
            case HARD:
                return new Resources(150, 150, 100, 300, 300, 300);
            default:
                return new Resources(200, 200, 150, 500, 500, 500);
        }
    }

    // Add resources (for collection or conquest)
    public void add(Resources resources) {
        this.stone += resources.getStone();
        this.wood += resources.getWood();
        this.gold += resources.getGold();

        // When adding from conquest, we can exceed max capacity
    }

    // Add resources with limit check
    public void addWithLimit(Resources resources) {
        this.stone = Math.min(this.stone + resources.getStone(), maxStone);
        this.wood = Math.min(this.wood + resources.getWood(), maxWood);
        this.gold = Math.min(this.gold + resources.getGold(), maxGold);
    }

    // Check if we have enough resources
    public boolean hasEnough(Resources cost) {
        return this.stone >= cost.getStone() &&
                this.wood >= cost.getWood() &&
                this.gold >= cost.getGold();
    }

    // Subtract resources (for building, training, etc.)
    public void subtract(Resources cost) {
        this.stone -= cost.getStone();
        this.wood -= cost.getWood();
        this.gold -= cost.getGold();
    }

    // Increase max capacity
    public void increaseMaxCapacity(int stone, int wood, int gold) {
        this.maxStone += stone;
        this.maxWood += wood;
        this.maxGold += gold;
    }

    // Getters and setters
    public int getStone() { return stone; }
    public int getWood() { return wood; }
    public int getGold() { return gold; }
    public int getMaxStone() { return maxStone; }
    public int getMaxWood() { return maxWood; }
    public int getMaxGold() { return maxGold; }

    @Override
    public String toString() {
        return "Stone: " + stone + "/" + maxStone +
                ", Wood: " + wood + "/" + maxWood +
                ", Gold: " + gold + "/" + maxGold;
    }
}