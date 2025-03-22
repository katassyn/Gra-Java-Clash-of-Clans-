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

public class CombatResult {
    private boolean playerVictory;
    private int playerLosses;
    private int enemyLosses;
    private Resources capturedResources;
    private Map<UnitType, Integer> survivingUnits;

    public CombatResult(boolean playerVictory, int playerLosses, int enemyLosses,
                        Resources capturedResources, Map<UnitType, Integer> survivingUnits) {
        this.playerVictory = playerVictory;
        this.playerLosses = playerLosses;
        this.enemyLosses = enemyLosses;
        this.capturedResources = capturedResources;
        this.survivingUnits = survivingUnits;
    }

    // Getters
    public boolean isPlayerVictory() { return playerVictory; }
    public int getPlayerLosses() { return playerLosses; }
    public int getEnemyLosses() { return enemyLosses; }
    public Resources getCapturedResources() { return capturedResources; }
    public Map<UnitType, Integer> getSurvivingUnits() { return survivingUnits; }
}