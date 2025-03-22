package org.example.utils;

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
public class EventManager {
    private static final int DEBUG_MODE = 1;

    private List<Event> positiveEvents;
    private List<Event> neutralEvents;
    private List<Event> negativeEvents;

    public EventManager() {
        initializeEvents();
    }

    // Initialize predefined events
    private void initializeEvents() {
        positiveEvents = new ArrayList<>();
        neutralEvents = new ArrayList<>();
        negativeEvents = new ArrayList<>();

        // Positive events
        positiveEvents.add(new Event(
                "Gold Discovery",
                "Your miners have discovered a rich gold vein!",
                (player) -> {
                    Resources bonus = new Resources(0, 0, 100, 0, 0, 0);
                    player.getResources().addWithLimit(bonus);
                    if (DEBUG_MODE == 1) {
                        System.out.println("[DEBUG] Event: Added 100 gold");
                    }
                },
                "You gained 100 gold."
        ));

        positiveEvents.add(new Event(
                "Wandering Merchant",
                "A merchant caravan visits your village offering rare goods at discount prices.",
                (player) -> {
                    // 20% discount on the next building upgrade
                    // (This is just a narrative effect - actual implementation would be complex)
                    if (DEBUG_MODE == 1) {
                        System.out.println("[DEBUG] Event: Merchant discount effect set");
                    }
                },
                "Your next building upgrade will be 20% cheaper."
        ));

        positiveEvents.add(new Event(
                "Skilled Craftsmen",
                "A group of skilled craftsmen joins your village.",
                (player) -> {
                    // Add some resources
                    Resources bonus = new Resources(50, 50, 30, 0, 0, 0);
                    player.getResources().addWithLimit(bonus);
                    if (DEBUG_MODE == 1) {
                        System.out.println("[DEBUG] Event: Added 50 stone, 50 wood, 30 gold");
                    }
                },
                "You gained 50 stone, 50 wood, and 30 gold."
        ));

        // Neutral events
        neutralEvents.add(new Event(
                "Traveling Bard",
                "A bard visits your village, sharing tales of distant lands.",
                (player) -> {
                    // Just flavor, no mechanical effect
                    if (DEBUG_MODE == 1) {
                        System.out.println("[DEBUG] Event: Bard visit (no effect)");
                    }
                },
                "Your villagers' morale improves, but there's no tangible benefit."
        ));

        neutralEvents.add(new Event(
                "Strange Weather",
                "Unusual weather patterns have been observed lately.",
                (player) -> {
                    // Just flavor, no mechanical effect
                    if (DEBUG_MODE == 1) {
                        System.out.println("[DEBUG] Event: Weather event (no effect)");
                    }
                },
                "The weather eventually returns to normal with no lasting consequences."
        ));

        // Negative events
        negativeEvents.add(new Event(
                "Bandit Raid",
                "A small group of bandits has raided your supply stores!",
                (player) -> {
                    // Lose some resources
                    Resources loss = new Resources(
                            Math.min(30, player.getResources().getStone()),
                            Math.min(30, player.getResources().getWood()),
                            Math.min(20, player.getResources().getGold()),
                            0, 0, 0
                    );
                    player.getResources().subtract(loss);
                    if (DEBUG_MODE == 1) {
                        System.out.println("[DEBUG] Event: Lost resources to bandits");
                    }
                },
                "You lost some resources to the bandits."
        ));

        negativeEvents.add(new Event(
                "Disease Outbreak",
                "A disease has spread among your village!",
                (player) -> {
                    // Lose some units
                    Map<UnitType, Integer> units = player.getUnits();
                    for (Map.Entry<UnitType, Integer> entry : new HashMap<>(units).entrySet()) {
                        UnitType type = entry.getKey();
                        int count = entry.getValue();

                        // Lose 10% of units
                        int losses = Math.max(1, count / 10);
                        units.put(type, count - losses);

                        if (DEBUG_MODE == 1) {
                            System.out.println("[DEBUG] Event: Lost " + losses + " " + type.getName() + " units to disease");
                        }
                    }
                },
                "Some of your units have fallen ill and died."
        ));

        negativeEvents.add(new Event(
                "Supply Shortage",
                "A supply chain disruption has affected your resource production!",
                (player) -> {
                    // No immediate effect, but implied reduced production
                    if (DEBUG_MODE == 1) {
                        System.out.println("[DEBUG] Event: Supply shortage (narrative effect)");
                    }
                },
                "Your resource production will be reduced today."
        ));
    }

    // Generate a random event based on difficulty
    public Event generateRandomEvent(DifficultyLevel difficulty) {
        Random random = new Random();

        // Determine event type based on difficulty
        double positiveChance;
        double neutralChance;
        double negativeChance;

        switch (difficulty) {
            case EASY:
                positiveChance = 0.6;
                neutralChance = 0.3;
                negativeChance = 0.1;
                break;
            case NORMAL:
                positiveChance = 0.4;
                neutralChance = 0.3;
                negativeChance = 0.3;
                break;
            case HARD:
                positiveChance = 0.2;
                neutralChance = 0.3;
                negativeChance = 0.5;
                break;
            default:
                positiveChance = 0.4;
                neutralChance = 0.3;
                negativeChance = 0.3;
        }

        // Select event type
        double roll = random.nextDouble();
        List<Event> selectedCategory;

        if (roll < positiveChance) {
            selectedCategory = positiveEvents;
        } else if (roll < positiveChance + neutralChance) {
            selectedCategory = neutralEvents;
        } else {
            selectedCategory = negativeEvents;
        }

        // Select a random event from the category
        int eventIndex = random.nextInt(selectedCategory.size());
        return selectedCategory.get(eventIndex);
    }
}