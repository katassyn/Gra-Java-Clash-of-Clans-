package org.example.main;

import java.util.Scanner;

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

public class Game {
    private static final int DEBUG_MODE = 1; // Set to 0 for production

    private Player player;
    private GameMap gameMap;
    private int currentDay;
    private boolean isGameOver;
    private Race playerRace;
    private DifficultyLevel difficulty;
    private Scanner scanner;

    public Game() {
        scanner = new Scanner(System.in);
        currentDay = 1;
        isGameOver = false;
    }

    public void start() {
        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Game initialization started");
        }

        showIntro();
        setupGame();
        gameLoop();
        endGame();

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Game ended");
        }
    }

    private void showIntro() {
        System.out.println("========================================");
        System.out.println("         VILLAGE CONQUEST              ");
        System.out.println("========================================");
        System.out.println("Welcome to Village Conquest, a strategy game");
        System.out.println("where you build your base, train armies,");
        System.out.println("and conquer territories to win!");
        System.out.println("========================================");
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }

    private void setupGame() {
        // Select race
        playerRace = selectRace();

        // Select difficulty
        difficulty = selectDifficulty();

        // Initialize player with starting resources
        player = new Player(playerRace, difficulty);

        // Generate map
        gameMap = new GameMap(difficulty);

        // Place player on the map
        gameMap.placePlayerStart(player);

        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Game setup completed. Race: " + playerRace +
                    ", Difficulty: " + difficulty);
        }
    }

    private Race selectRace() {
        int choice = 0;
        boolean validChoice = false;

        while (!validChoice) {
            System.out.println("\nSelect your race:");
            System.out.println("1. Humans - Balanced units and buildings");
            System.out.println("2. Elves - Advanced archers and magic, weaker defenses");
            System.out.println("3. Orcs - Strong warriors, but slower development");
            System.out.print("\nYour choice (1-3): ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 1 && choice <= 3) {
                    validChoice = true;
                } else {
                    System.out.println("Invalid choice. Please select 1, 2, or 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }

        switch (choice) {
            case 1: return Race.HUMAN;
            case 2: return Race.ELF;
            case 3: return Race.ORC;
            default: return Race.HUMAN; // Default fallback
        }
    }

    private DifficultyLevel selectDifficulty() {
        int choice = 0;
        boolean validChoice = false;

        while (!validChoice) {
            System.out.println("\nSelect difficulty level:");
            System.out.println("1. Bambik (Easy) - More resources, weaker enemies");
            System.out.println("2. Normal - Balanced gameplay");
            System.out.println("3. Realism (Hard) - Limited resources, stronger enemies");
            System.out.print("\nYour choice (1-3): ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 1 && choice <= 3) {
                    validChoice = true;
                } else {
                    System.out.println("Invalid choice. Please select 1, 2, or 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }

        switch (choice) {
            case 1: return DifficultyLevel.EASY;
            case 2: return DifficultyLevel.NORMAL;
            case 3: return DifficultyLevel.HARD;
            default: return DifficultyLevel.NORMAL; // Default fallback
        }
    }

    private void gameLoop() {
        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Starting game loop");
        }

        while (!isGameOver) {
            // Show day information
            System.out.println("\n========== DAY " + currentDay + " ==========");

            // Collect daily resources
            collectResources();

            // Show player status
            showPlayerStatus();

            // Show map
            gameMap.displayMap();

            // Player actions
            processPlayerActions();

            // Enemy actions
            processEnemyActions();

            // Random events
            processRandomEvents();

            // Check victory conditions
            checkVictoryConditions();

            // End day
            currentDay++;

            if (DEBUG_MODE == 1) {
                System.out.println("[DEBUG] Day " + currentDay + " completed");
            }

            // Small pause between days
            System.out.println("\nPress ENTER to continue to the next day...");
            scanner.nextLine();
        }
    }

    private void collectResources() {
        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Collecting daily resources");
        }

        player.collectDailyResources();
        System.out.println("\nYou collected daily resources from your buildings!");
    }

    private void showPlayerStatus() {
        System.out.println("\n----- YOUR VILLAGE STATUS -----");
        player.displayStatus();
    }

    private void processPlayerActions() {
        boolean endTurn = false;

        while (!endTurn) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Build or upgrade structures");
            System.out.println("2. Train units");
            System.out.println("3. Attack enemy territory");
            System.out.println("4. View detailed village information");
            System.out.println("5. End turn");
            System.out.print("\nYour choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        buildMenu();
                        break;
                    case 2:
                        trainUnitsMenu();
                        break;
                    case 3:
                        attackMenu();
                        break;
                    case 4:
                        viewDetailedInfo();
                        break;
                    case 5:
                        endTurn = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private void buildMenu() {
        System.out.println("\n----- BUILD MENU -----");
        System.out.println("Available resources:");
        player.displayResources();

        System.out.println("\nWhat would you like to build or upgrade?");
        player.displayAvailableBuildings();

        System.out.println("0. Back to main menu");
        System.out.print("\nYour choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 0) {
                return;
            }

            player.buildOrUpgrade(choice);

        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
        }
    }

    private void trainUnitsMenu() {
        System.out.println("\n----- TRAINING MENU -----");
        System.out.println("Available resources:");
        player.displayResources();

        System.out.println("\nWhat units would you like to train?");
        player.displayAvailableUnits();

        System.out.println("0. Back to main menu");
        System.out.print("\nYour choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 0) {
                return;
            }

            System.out.print("How many units? ");
            int count = Integer.parseInt(scanner.nextLine());
            player.trainUnits(choice, count);

        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
        }
    }

    private void attackMenu() {
        System.out.println("\n----- ATTACK MENU -----");

        // Check if player has units
        if (!player.hasUnits()) {
            System.out.println("You don't have any units to attack with!");
            return;
        }

        // Show attackable territories
        System.out.println("\nTerritories you can attack:");
        boolean hasAttackableTerritory = gameMap.displayAttackableTerritories();

        if (!hasAttackableTerritory) {
            System.out.println("There are no territories you can attack right now.");
            return;
        }

        System.out.println("0. Back to main menu");
        System.out.print("\nSelect territory to attack (0 to cancel): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice == 0) {
                return;
            }

            Territory target = gameMap.getTerritoryById(choice);
            if (target == null) {
                System.out.println("Invalid territory selection.");
                return;
            }

            // Select units to send
            player.displayUnits();
            System.out.println("\nHow many units to send to battle?");

            for (UnitType unitType : player.getAvailableUnitTypes()) {
                System.out.print(unitType.getName() + " (max " + player.getUnitCount(unitType) + "): ");
                int count = Integer.parseInt(scanner.nextLine());
                if (count > 0) {
                    player.assignUnitsToAttack(unitType, count);
                }
            }

            // Execute attack
            CombatManager combatManager = new CombatManager();
            CombatResult result = combatManager.resolveCombat(player, target);

            if (result.isPlayerVictory()) {
                gameMap.captureTerritory(target.getId(), player);
                player.addResources(result.getCapturedResources());
                System.out.println("\nVictory! You captured territory " + target.getId() +
                        " and gained resources!");
            } else {
                System.out.println("\nDefeat! Your attack on territory " + target.getId() +
                        " was repelled. You lost " + result.getPlayerLosses() + " units.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
        }
    }

    private void viewDetailedInfo() {
        System.out.println("\n----- DETAILED VILLAGE INFORMATION -----");

        System.out.println("\nBuildings:");
        player.displayDetailedBuildings();

        System.out.println("\nUnits:");
        player.displayDetailedUnits();

        System.out.println("\nTerritories:");
        gameMap.displayPlayerTerritories(player);

        System.out.println("\nPress ENTER to return to the main menu...");
        scanner.nextLine();
    }

    private void processEnemyActions() {
        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Processing enemy actions");
        }

        // Calculate chance of enemy attack based on difficulty and adjacent enemy territories
        double attackChance = difficulty.getEnemyAttackChance() *
                gameMap.getAdjacentEnemyTerritoryCount(player);

        if (Math.random() < attackChance) {
            Territory attackingTerritory = gameMap.getRandomAdjacentEnemyTerritory(player);

            if (attackingTerritory != null) {
                System.out.println("\n! ! ! ENEMY ATTACK ! ! !");
                System.out.println("Territory " + attackingTerritory.getId() +
                        " is attacking your territory!");

                CombatManager combatManager = new CombatManager();
                CombatResult result = combatManager.resolveEnemyAttack(attackingTerritory, player);

                if (result.isPlayerVictory()) {
                    System.out.println("You successfully defended your territory!");
                    System.out.println("Enemy casualties: " + result.getEnemyLosses() + " units");
                    System.out.println("Your casualties: " + result.getPlayerLosses() + " units");
                } else {
                    System.out.println("Your defenses have failed!");
                    System.out.println("You lost " + result.getPlayerLosses() + " units");

                    // Check if player lost their last territory
                    if (gameMap.getPlayerTerritoryCount(player) == 1) {
                        gameMap.loseTerritory(player.getCurrentTerritory().getId());
                        isGameOver = true;
                        System.out.println("\nYou lost your last territory! GAME OVER!");
                    } else {
                        gameMap.loseTerritory(player.getCurrentTerritory().getId());
                        System.out.println("You lost territory " + player.getCurrentTerritory().getId());

                        // Assign a new current territory
                        player.setCurrentTerritory(gameMap.getFirstPlayerTerritory(player));
                    }
                }
            }
        } else {
            System.out.println("\nNo enemy attacks today.");
        }
    }

    private void processRandomEvents() {
        if (DEBUG_MODE == 1) {
            System.out.println("[DEBUG] Processing random events");
        }

        // 20% chance of random event each day
        if (Math.random() < 0.2) {
            EventManager eventManager = new EventManager();
            Event randomEvent = eventManager.generateRandomEvent(difficulty);

            System.out.println("\n! ! ! RANDOM EVENT ! ! !");
            System.out.println(randomEvent.getDescription());

            randomEvent.applyEffect(player);

            System.out.println("Effect: " + randomEvent.getEffectDescription());
        }
    }

    private void checkVictoryConditions() {
        // Check if player has conquered the entire map
        if (gameMap.isMapConquered(player)) {
            isGameOver = true;
            System.out.println("\n*********************************");
            System.out.println("*          VICTORY!            *");
            System.out.println("* You have conquered the map!  *");
            System.out.println("*********************************");
        }

        // Check if player has no territories left (handled in enemy attack)
    }

    private void endGame() {
        System.out.println("\n========== GAME SUMMARY ==========");
        System.out.println("Days survived: " + currentDay);
        System.out.println("Territories conquered: " + gameMap.getPlayerTerritoryCount(player) +
                " out of " + gameMap.getTotalTerritories());
        System.out.println("Final resources: ");
        player.displayResources();

        System.out.println("\nThank you for playing Village Conquest!");
        System.out.println("========================================");
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}