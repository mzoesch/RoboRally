package sep.server.model.game;

import sep.server.model.game.cards.upgrade.UpgradeCard;

public class Game {
    int playerNum;
    Player[] players;
    EnergyCube[] energyBank;
    UpgradeCard[] upgradeShop;

    public void addPlayers() {}
    public void setupGame() {}
    public void startGame() {}
    public void runRound() {}
    public void upgradePhase() {}
    public void programmingPhase() {}
    public void activationPhase() {}
    public void endRound() {}
}
