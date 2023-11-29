package sep.server.model.game;

import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.json.game.MockGameStartedModel;
import sep.server.model.game.cards.Card;
import sep.server.model.game.tiles.Coordinate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

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
        for(int i = 0; i < 5; i++) {
            determinePriorities();
            sortPlayersByPriority(i);
            determineCurrentCards(i);
            for(int j = 0; j < players.size(); i++) {
                players.get(j).registers[i].playCard();
            }
            activateBlueConveyorBelts();
            activateGreenConveyorBelts();
            activatePushPanels();
            activateGears();
            shootBoardLasers();
            shootRobotLasers();
            checkEnergySpaces();
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

    public void activateBlueConveyorBelts() {}
    public void activateGreenConveyorBelts() {}
    public void activatePushPanels() {}
    public void activateGears() {}
    public void shootBoardLasers() {}
    public void shootRobotLasers() {}
    public void checkEnergySpaces() {}
    public void checkCheckpoints() {}

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
                }
                IPlayableCard card = player.getPlayerDeck().remove(0);
                player.getPlayerHand().add(card);
            }
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
