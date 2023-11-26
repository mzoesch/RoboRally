package sep.server.model.game;

import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.cards.IPlayableCard;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * The rules of the game are implemented here. It is a high-level manager object for one game and controls the
 * overall flow of it. It is spawned at the start of a game and not destroyed until the game has ended.
 */
public class GameMode
{
    int playerNum;
    ArrayList<Player> players;
    int energyBank;
    AUpgradeCard[] upgradeShop;


    public GameMode(String course, PlayerController[] playerControllers)
    {
    }

    public void setupGame() {
        determinePriority();
    }
    public void determinePriority() {}
    public void runRound() {}
    public void upgradePhase() {}
    public void programmingPhase() {
        distributeCards(players);
    }
    public void activationPhase() {
        players.sort(Comparator.comparingInt(Player::getPriority));

        for(int i=0; i<5; i++) {
            for(Player player : players) {
                player.getRegisters()[i].playCard();
                player.getDiscardPile().add(player.getRegisters()[i]);
                player.getRegisters()[i] = null;
            }
            activateBoardElements();
            shootRobotLasers();
        }

        endRound();
    }
    public void distributeCards(ArrayList<Player> players) {
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

    public void activateBoardElements() {}
    public void shootRobotLasers() {}
    public void endRound() {
        //TODO: check if game is finished

        for(Player player : players) {
            player.shuffleAndRefillDeck();
        }
    }

}
