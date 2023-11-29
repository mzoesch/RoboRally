package sep.server.model.game;

import sep.server.json.game.effects.EnergyModel;
import sep.server.json.game.effects.MovementModel;
import sep.server.json.game.effects.PlayerTurningModel;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.tiles.*;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.json.game.MockGameStartedModel;
import sep.server.model.game.cards.Card;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import org.json.JSONObject;

/**
 * The rules of the game are implemented here. It is a high-level manager object for one game and controls the
 * overall flow of it. It is spawned at the start of a game and not destroyed until the game has ended.
 */
public class GameMode
{
    private final Course course;
    int playerNum;
    ArrayList<Player> players;
    int energyBank;
    AUpgradeCard[] upgradeShop;
    GameState gameState;


    public GameMode(String course, PlayerController[] playerControllers)
    {
        super();

        this.course = new Course(course);

        //TODO hier Spieler erstellen; Roboter erstellen

        /* Just temporary. This is for helping to develop the front-end. */
        for (PlayerController pc : playerControllers) {
            new MockGameStartedModel(pc.getClientInstance()).send();
            continue;
        }

        /* Announcing Phase Zero. */
        for (PlayerController pc : playerControllers) {
            pc.getClientInstance().sendMockJSON(new JSONObject("{\"messageType\":\"ActivePhase\",\"messageBody\":{\"phase\":0}}"));
            continue;
        }

        /* Selecting starting player. */
        for (PlayerController pc : playerControllers) {
            pc.getClientInstance().sendMockJSON(new JSONObject(String.format("{\"messageType\":\"CurrentPlayer\",\"messageBody\":{\"clientID\":%d}}", playerControllers[0].getPlayerID())));
            continue;
        }

        return;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void programmingPhase() {
        distributeCards(players);
    }

    /**
     * The following method handles the activation phase: it iterates over the different registers and
     * plays the current card for each player (players are sorted by priority). Once each player's
     * card has been played the board elements activate and the robot lasers are shot.
     */
    public void activationPhase() {
        for(int currentRegister = 0; currentRegister < 5; currentRegister++) {
            determinePriorities();
            sortPlayersByPriority(currentRegister);
            determineCurrentCards(currentRegister);
            for(int j = 0; j < players.size(); currentRegister++) {
                players.get(j).registers[currentRegister].playCard();
            }
            activateConveyorBelt(2);
            activateConveyorBelt(1);
            activatePushPanels(currentRegister);
            activateGears();
            shootBoardLasers();
            shootRobotLasers();
            checkEnergySpaces(currentRegister);
            checkCheckpoints();
        }
    }

    /**
     * The following method calculates the priorities for all players: First the distance from each robot to the
     * antenna is calculated. Next the priorities are assigned. The closest player gets the highest priority.
     */
    public void determinePriorities() {
        Coordinate antennaCoordinate = new Coordinate(0,4); //StartA board
        int maxPriority = players.size();
        int currentPriority = maxPriority;
        int[] distances = new int[players.size()];

        for(int i = 0; i < players.size(); i++) {
            Robot playerRobot = players.get(i).getPlayerRobot();
            Coordinate robotCoordinate = playerRobot.getCurrentTile().getCoordinate();
            distances[i] = Math.abs(antennaCoordinate.getXCoordinate() - robotCoordinate.getXCoordinate())
                    + Math.abs(antennaCoordinate.getYCoordinate() - robotCoordinate.getYCoordinate());
        }

        for(int j = 0; j < maxPriority; j++) {
            int minDistance = Integer.MAX_VALUE;
            int minIndex = -1;

            for(int i = 0; i < distances.length; i++) {
                if(distances[i] < minDistance) {
                    minDistance = distances[i];
                    minIndex = i;
                }
            }

            if(minIndex != -1) {
                players.get(minIndex).setPriority(currentPriority);
                currentPriority--;
                distances[minIndex] = Integer.MAX_VALUE;
            }
        }

    }

    /**
     * The following method sorts all players from highest to lowest priority.
     */
    public void sortPlayersByPriority(int currentRegisterIndex) {
        players.sort(Comparator.comparingInt(Player::getPriority).reversed());
    }

    /**
     * The following method determines the current card that each player holds in the currently active register.
     * @return client ID and card type as String
     */
    public HashMap<Integer, String> determineCurrentCards(int currentRegisterIndex) {
        //TODO adjust to JSON wrapper class once created
        HashMap<Integer, String> currentCards = new HashMap<>();

        for(Player player : players) {
            String cardInRegister = ((Card) player.getCardInRegister(currentRegisterIndex)).getCardType();
            currentCards.put(player.getPlayerController().getPlayerID(), cardInRegister);
        }

        return currentCards;
    }

    /**
     * The following method handles the activation of conveyor belts and sends the corresponding JSON messages.
     * The robot is moved in the outcoming flow direction of the conveyor belt.
     * @param speed determines the amount of fields the robot is moved
     */
    private void activateConveyorBelt(int speed) {
        for (Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if (fieldType instanceof ConveyorBelt) {
                    ConveyorBelt conveyorBelt = (ConveyorBelt) fieldType;
                    int beltSpeed = conveyorBelt.getSpeed();

                    if (beltSpeed == speed) {
                        Coordinate oldCoordinate = currentTile.getCoordinate();
                        String outDirection = conveyorBelt.getOutcomingFlowDirection();
                        Coordinate newCoordinate = oldCoordinate;

                        newCoordinate = calculateNewCoordinate(speed, outDirection, oldCoordinate);

                        if (!course.isCoordinateWithinBounds(newCoordinate)) {
                            player.getPlayerRobot().reboot();
                            return;
                        }

                        if (!player.getPlayerRobot().isMovable(course.getTileByCoordinate(newCoordinate))) {
                            return;
                        }

                        course.updateRobotPosition(player.getPlayerRobot(), newCoordinate);

                        for(Player player1 : players) {
                            new MovementModel(player1.getPlayerController().getClientInstance(),
                                    player1.getPlayerController().getPlayerID(),
                                    newCoordinate.getXCoordinate(), newCoordinate.getYCoordinate()).send();
                        }
                    }
                }
            }
        }
    }

    /**
     * The following method handles the activation of push panels and sends the corresponding JSON messages.
     * The robot is moved to the next field in the direction of the panel's pushOrientation.
     * @param currentRegister the register that is currently active
     */
    public void activatePushPanels(int currentRegister) {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof PushPanel) {
                    PushPanel pushPanel = (PushPanel) fieldType;
                    int[] activateAtRegister = pushPanel.getActivateAtRegister();

                    for(int register : activateAtRegister) {
                        if(register == currentRegister) {
                            String pushOrientation = pushPanel.getOrientation();
                            Coordinate oldCoordinate = currentTile.getCoordinate();
                            Coordinate newCoordinate = oldCoordinate;

                            newCoordinate = calculateNewCoordinate(1, pushOrientation, oldCoordinate);

                            if (!course.isCoordinateWithinBounds(newCoordinate)) {
                                player.getPlayerRobot().reboot();
                                return;
                            }

                            if (!player.getPlayerRobot().isMovable(course.getTileByCoordinate(newCoordinate))) {
                                return;
                            }

                            course.updateRobotPosition(player.getPlayerRobot(), newCoordinate);

                            for(Player player1 : players) {
                                new MovementModel(player1.getPlayerController().getClientInstance(),
                                        player1.getPlayerController().getPlayerID(),
                                        newCoordinate.getXCoordinate(), newCoordinate.getYCoordinate()).send();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The following method handles the activation of gears and sends the corresponding JSON messages.
     * The robot is rotated 90 degrees into the gear's rotational direction.
     */
    public void activateGears() {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof Gear) {
                    Gear gear = (Gear) fieldType;
                    String rotationalDirection = gear.getRotationalDirection();
                    String robotDirection = player.getPlayerRobot().getDirection();
                    String newDirection = robotDirection;
                    if(rotationalDirection == "counterclockwise") {
                        switch (robotDirection) {
                            case "NORTH" -> newDirection = "WEST";
                            case "EAST" -> newDirection = "NORTH";
                            case "SOUTH" -> newDirection = "EAST";
                            case "WEST" -> newDirection = "SOUTH";
                        }
                    } else if(rotationalDirection == "clockwise") {
                        switch (robotDirection) {
                            case "NORTH" -> newDirection = "EAST";
                            case "EAST" -> newDirection = "SOUTH";
                            case "SOUTH" -> newDirection = "WEST";
                            case "WEST" -> newDirection = "NORTH";
                        }
                    }

                    player.getPlayerRobot().setDirection(newDirection);

                    for(Player player1 : players) {
                        new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                                player1.getPlayerController().getPlayerID(), rotationalDirection).send();
                    }
                }
            }
        }
    }
    public void shootBoardLasers() {}
    public void shootRobotLasers() {}

    /**
     * The following method checks if any robot ended their register on an energy space, if
     * they receive an energy cube, and sends the corresponding JSON messages.
     * @param currentRegister the register that is currently active
     */
    public void checkEnergySpaces(int currentRegister) {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof EnergySpace) {
                    EnergySpace energySpace = (EnergySpace) fieldType;
                    int availableEnergy = energySpace.getAvailableEnergy();
                    int currentEnergy = player.getEnergyCollected();
                    if(currentRegister == 5) {
                        if(energyBank > 0) {
                            player.setEnergyCollected(currentEnergy + 1);
                            energyBank -= 1;
                        }
                    } else if(availableEnergy > 0) {
                        player.setEnergyCollected(currentEnergy + 1);
                    }

                    for(Player player1 : players) {
                        new EnergyModel(player1.getPlayerController().getClientInstance(),
                                player1.getPlayerController().getPlayerID(),
                                player.getEnergyCollected(),
                                "EnergySpace").send();
                    }
                }
            }
        }
    }
    public void checkCheckpoints() {}

    /**
     * The following method calculates the new coordinates for activating conveyor belts and push panels.
     * @param distance amount of fields the robot is moved
     * @param orientation direction the robot is moved to
     * @param oldCoordinate coordinates of the current push panel pushing a robot
     * @return
     */
    public Coordinate calculateNewCoordinate(int distance, String orientation, Coordinate oldCoordinate) {
        Coordinate newCoordinate = null;
        switch (orientation) {
            case "NORTH" -> newCoordinate = new Coordinate(oldCoordinate.getXCoordinate(),
                    oldCoordinate.getYCoordinate() - distance);
            case "EAST" -> newCoordinate = new Coordinate(oldCoordinate.getXCoordinate() + distance,
                    oldCoordinate.getYCoordinate());
            case "SOUTH" -> newCoordinate = new Coordinate(oldCoordinate.getXCoordinate(),
                    oldCoordinate.getYCoordinate() + distance);
            case "WEST" -> newCoordinate = new Coordinate(oldCoordinate.getXCoordinate() - distance,
                    oldCoordinate.getYCoordinate());
        }
        return newCoordinate;
    }

    /**
     * The following method is used whenever the current player's card in the currently active register needs to be
     * replaced by the top card of the player deck.
     * @return array that holds current register, new card that is now placed in the current register and ID of player
     *  that is replacing cards
     */
    public Object[] replaceCardInRegister(int currentRegisterIndex, int currentPlayerIndex) {
        //TODO adjust to JSON wrapper class once created
        Player player = players.get(currentPlayerIndex);
        IPlayableCard topCardFromDiscardPile = player.getPlayerDeck().get(0);
        String newCard = ((Card) topCardFromDiscardPile).getCardType();
        int clientID = player.getPlayerController().getPlayerID();

        player.setCardInRegister(currentRegisterIndex, topCardFromDiscardPile);

        return new Object[] {currentRegisterIndex, newCard, clientID};
    }

    public void distributeCards(ArrayList<Player> players)
    {
        for (Player player : players) {
            for (int i = 0; i < 9; i++) {
                if (player.getPlayerDeck().isEmpty()) {
                    player.shuffleAndRefillDeck();
                    gameState.sendShuffle(player);
                }
                IPlayableCard card = player.getPlayerDeck().remove(0);
                player.getPlayerHand().add(card);
            }

            gameState.sendHandCards(player);
        }

    }





    public void endRound() {
        for(int i = 0; i<5; i++) {
            for(Player player : players) {
                player.getDiscardPile().add(player.getRegisters()[i]);
                player.getRegisters()[i] = null;
            }
        }

        //TODO: check if game is finished

        for(Player player : players) {
            player.shuffleAndRefillDeck();
        }
    }

}
