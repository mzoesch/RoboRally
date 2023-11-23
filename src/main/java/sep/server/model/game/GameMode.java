package sep.server.model.game;

import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.viewmodel.PlayerController;

/**
 * The rules of the game are implemented here. It is a high-level manager object for one game and controls the
 * overall flow of it. It is spawned at the start of a game and not destroyed until the game has ended.
 */
public class GameMode
{
    int playerNum;
    Player[] players;
    int energyBank;
    AUpgradeCard[] upgradeShop;
    int currentRoundNumber; //Represents the round number during the activation phase (indicate which register is currently being revealed)

    public GameMode(String course, PlayerController[] playerControllers)
    {
    }


    public void setupGame() {}
    public void startGame() {}
    public void runRound() {}
    public void upgradePhase() {}
    public void programmingPhase() {}
    public void activationPhase() {}
    public void endRound() {}
}
