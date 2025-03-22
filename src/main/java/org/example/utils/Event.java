package org.example.utils;

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
public class Event {
    private String name;
    private String description;
    private EventEffect effect;
    private String effectDescription;

    public Event(String name, String description, EventEffect effect, String effectDescription) {
        this.name = name;
        this.description = description;
        this.effect = effect;
        this.effectDescription = effectDescription;
    }

    // Apply event effect to player
    public void applyEffect(Player player) {
        effect.apply(player);
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getEffectDescription() { return effectDescription; }
}