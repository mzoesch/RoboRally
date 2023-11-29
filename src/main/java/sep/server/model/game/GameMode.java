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
    int currentPlayerIndex;
    int currentRegisterIndex;
    int energyBank;
    AUpgradeCard[] upgradeShop;


    public GameMode(String course, int registerIndex, PlayerController[] playerControllers)
    {
        super();

        this.course = new Course(course);
        this.currentRegisterIndex = -1;

        //TODO hier Spieler erstellen; Roboter erstellen

        /* Just temporary. This is for helping to develop the front-end. */
        for (PlayerController pc : playerControllers) {
            new MockGameStartedModel(pc.getClientInstance()).send();
            continue;
        }

        return;
    }

    public void programmingPhase() {
        distributeCards(players);
    }

    /**
     * The following method is called whenever a new register is to be activated: priorities of the players are determined,
     * currentRegister is set to next possible register, currentPlayer is set to -1.
     */
    public void prepareRegister() {
        determinePriorities();
        sortPlayersByPriority();
        if(currentRegisterIndex < 5) {
            this.currentRegisterIndex += 1;
            this.currentPlayerIndex = -1;
        }
        determineCurrentCards();
    }

    /**
     * The following method determines the current card that each player holds in the currently active register.
     * @return client ID and card type as String
     */
    public HashMap<Integer, String> determineCurrentCards() {
        //TODO adjust to JSON wrapper class once created
        HashMap<Integer, String> currentCards = new HashMap<>();

        for(Player player : players) {
            String cardInRegister = ((Card) player.getCardInRegister(currentRegisterIndex)).getCardType();
            currentCards.put(player.getPlayerController().getPlayerID(), cardInRegister);
        }

        return currentCards;
    }

    /**
     * The following method is called whenever a register is activated for a player: the currentPlayer is set to the player
     * with the next lower priority and the card of the currently active register is played.
     */
    public void activateRegister() {
        Player currentPlayer = players.get(currentPlayerIndex + 1);
        currentPlayer.registers[currentRegisterIndex].playCard();
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
    public void sortPlayersByPriority() {
        players.sort(Comparator.comparingInt(Player::getPriority).reversed());
    }

    /**
     * The following method is used whenever the current player's card in the currently active register needs to be
     * replaced by the top card of the player deck.
     * @return array that holds current register, new card that is now placed in the current register and ID of player
     *  that is replacing cards
     */
    public Object[] replaceCardInRegister() {
        //TODO adjust to JSON wrapper class once created
        int register = currentRegisterIndex;
        Player player = players.get(currentPlayerIndex);
        IPlayableCard topCardFromDiscardPile = player.getPlayerDeck().get(0);
        String newCard = ((Card) topCardFromDiscardPile).getCardType();
        int clientID = player.getPlayerController().getPlayerID();

        player.setCardInRegister(register, topCardFromDiscardPile);

        return new Object[] {register, newCard, clientID};
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
