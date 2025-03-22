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
public enum DifficultyLevel {
    EASY("Bambik", "More resources, weaker enemies", 1.5, 0.7, 0.1),
    NORMAL("Normal", "Balanced gameplay", 1.0, 1.0, 0.15),
    HARD("Realism", "Limited resources, stronger enemies", 0.7, 1.3, 0.2);

    private final String name;
    private final String description;
    private final double resourceMultiplier;
    private final double enemyStrengthMultiplier;
    private final double enemyAttackChance;

    DifficultyLevel(String name, String description, double resourceMultiplier,
                    double enemyStrengthMultiplier, double enemyAttackChance) {
        this.name = name;
        this.description = description;
        this.resourceMultiplier = resourceMultiplier;
        this.enemyStrengthMultiplier = enemyStrengthMultiplier;
        this.enemyAttackChance = enemyAttackChance;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getResourceMultiplier() { return resourceMultiplier; }
    public double getEnemyStrengthMultiplier() { return enemyStrengthMultiplier; }
    public double getEnemyAttackChance() { return enemyAttackChance; }
}