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
public enum UnitType {
    // Human units
    HUMAN_SOLDIER("Soldier", Race.HUMAN, 10, 2, 5,
            new Resources(0, 0, 50, 0, 0, 0)),
    HUMAN_ARCHER("Archer", Race.HUMAN, 7, 7, 3,
            new Resources(0, 30, 40, 0, 0, 0)),
    HUMAN_KNIGHT("Knight", Race.HUMAN, 20, 5, 15,
            new Resources(50, 0, 100, 0, 0, 0)),

    // Elf units
    ELF_SCOUT("Scout", Race.ELF, 5, 5, 7,
            new Resources(0, 20, 40, 0, 0, 0)),
    ELF_ARCHER("Archer", Race.ELF, 5, 15, 3,
            new Resources(0, 40, 50, 0, 0, 0)),
    ELF_MAGE("Mage", Race.ELF, 3, 25, 5,
            new Resources(20, 0, 150, 0, 0, 0)),

    // Orc units
    ORC_GRUNT("Grunt", Race.ORC, 15, 0, 10,
            new Resources(0, 20, 40, 0, 0, 0)),
    ORC_RAIDER("Raider", Race.ORC, 20, 0, 5,
            new Resources(20, 30, 60, 0, 0, 0)),
    ORC_SHAMAN("Shaman", Race.ORC, 10, 15, 5,
            new Resources(20, 20, 100, 0, 0, 0));

    private final String name;
    private final Race race;
    private final int attackPower;
    private final int magicPower;
    private final int defense;
    private final Resources cost;

    UnitType(String name, Race race, int attackPower, int magicPower,
             int defense, Resources cost) {
        this.name = name;
        this.race = race;
        this.attackPower = attackPower;
        this.magicPower = magicPower;
        this.defense = defense;
        this.cost = cost;
    }

    public String getName() { return name; }
    public Race getRace() { return race; }
    public int getAttackPower() { return attackPower; }
    public int getMagicPower() { return magicPower; }
    public int getDefense() { return defense; }
    public Resources getCost() { return cost; }

    // Get units available for a specific race
    public static UnitType[] getUnitsForRace(Race race) {
        switch (race) {
            case HUMAN:
                return new UnitType[] {HUMAN_SOLDIER, HUMAN_ARCHER, HUMAN_KNIGHT};
            case ELF:
                return new UnitType[] {ELF_SCOUT, ELF_ARCHER, ELF_MAGE};
            case ORC:
                return new UnitType[] {ORC_GRUNT, ORC_RAIDER, ORC_SHAMAN};
            default:
                return new UnitType[0];
        }
    }
}