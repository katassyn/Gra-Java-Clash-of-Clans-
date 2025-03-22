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
public enum Race {
    HUMAN("Human", "Balanced units and buildings", 1.0, 1.0, 1.0),
    ELF("Elf", "Advanced archers and magic, weaker defenses", 0.8, 1.5, 0.9),
    ORC("Orc", "Strong warriors, but slower development", 1.3, 0.7, 1.2);

    private final String name;
    private final String description;
    private final double strengthMultiplier;
    private final double magicMultiplier;
    private final double defenseMultiplier;

    Race(String name, String description, double strengthMultiplier,
         double magicMultiplier, double defenseMultiplier) {
        this.name = name;
        this.description = description;
        this.strengthMultiplier = strengthMultiplier;
        this.magicMultiplier = magicMultiplier;
        this.defenseMultiplier = defenseMultiplier;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getStrengthMultiplier() { return strengthMultiplier; }
    public double getMagicMultiplier() { return magicMultiplier; }
    public double getDefenseMultiplier() { return defenseMultiplier; }
}