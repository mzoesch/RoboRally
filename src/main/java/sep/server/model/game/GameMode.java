package sep.server.model.game;

import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.cards.IPlayableCard;

import java.util.ArrayList;
import java.util.Collections;
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
    public void programmingPhase() {}
    public void activationPhase() {
        //sort players by priority
        players.sort(Comparator.comparingInt(Player::getPriority));

        //for loop iterates over registers
        for(int i=0; i<5; i++) {
            for(Player player : players) {
                IPlayableCard[] curPlayerRegister = player.getRegisters();
                //card of the current register is played
                curPlayerRegister[i].playCard();
            }
            //after every player has played the card of the current register, board elements are activated
            activateBoardElements();
            shootRobotLasers();
        }

        endRound();
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
