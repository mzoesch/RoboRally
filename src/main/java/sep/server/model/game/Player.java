package sep.server.model.game;

import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.tiles.Coordinate;
import sep.server.model.game.builder.DeckBuilder;
import sep.server.viewmodel.PlayerController;
import sep.server.viewmodel.Session;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Player {
    private static final Logger l = LogManager.getLogger(Player.class);

    private int priority;

    private final Session session;
    private final PlayerController playerController;
    private final Robot playerRobot;

    private final ArrayList<IPlayableCard> playerDeck;
    private final ArrayList<IPlayableCard> discardPile;
    private final ArrayList<AUpgradeCard> upgradeCards;
    private final IPlayableCard[] registers;
    private final ArrayList<IPlayableCard> playerHand;
    private int energyCollected;
    private int checkpointsCollected;

    /**
     * @deprecated Make gateway call instead
     */
    private GameMode gameMode;

    public Player(final PlayerController playerController, final Course currentCourse, final Session session) {
        this.session = session;
        this.playerController = playerController;
        this.playerRobot = new Robot(currentCourse);

        this.playerDeck = new DeckBuilder().buildProgrammingDeck();
        Collections.shuffle(this.playerDeck);
        this.discardPile = new ArrayList<>();
        this.upgradeCards = new ArrayList<>();
        this.registers = new IPlayableCard[5];
        this.playerHand = new ArrayList<>();

        this.energyCollected = GameMode.STARTING_ENERGY;
        this.checkpointsCollected = 0;

        this.gameMode = session.getGameState().getAuthGameMode();
    }

    /**
     * The following method shuffles the discard pile and refills the player deck.
     */
    public void shuffleAndRefillDeck() {
        Collections.shuffle(discardPile);
        playerDeck.addAll(playerDeck.size(), discardPile);
        discardPile.clear();
    }

    /**
     * Adds a playable card to the specified register position.
     * If all registers are full after the addition, it notifies the session that the selection is finished. If all
     * players have finished their selection, the next phase will be started.
     * @param card Name of the card to be added to the register
     * @param pos  Position of the register to add the card to (zero-based)
     */
    public void setCardToRegister(final String card, final int pos) {
        if (this.hasPlayerFinishedProgramming()) {
            l.warn("Player {} has already finished programming and, therefore, cannot change their programming registers anymore.", this.playerController.getPlayerName());
            return;
        }

        if (pos < 0 || pos > 4) {
            l.error("Invalid register position: " + pos);
            return;
        }

        final IPlayableCard playableCard = this.getCardByName(card);

        if (playableCard == null) {
            this.registers[pos] = null;
            this.session.sendCardSelected(this.playerController.getPlayerID(), pos, false);
            return;
        }

        this.registers[pos] = playableCard;
        this.session.sendCardSelected(getPlayerController().getPlayerID(), pos, true);

        if (this.hasPlayerFinishedProgramming()) {
            l.debug("Player " + this.playerController.getPlayerName() + " has finished programming.");
            this.session.sendSelectionFinished(this.playerController.getPlayerID());

            if (this.session.haveAllPlayersFinishedProgramming()) {
                l.debug("All players have finished programming in time. Interrupting timer.");
                // TODO Interrupt timer
                this.session.getGameState().getAuthGameMode().handleNewPhase(EGamePhase.ACTIVATION);
            }

            // TODO We ignore this for now.
            // this.session.getGameState().getAuthGameMode().startProgrammingTimer();
        }
    }

    public void handleIncompleteProgramming() {
        discardPile.addAll(playerHand);
        playerHand.clear();
        shuffleAndRefillDeck();

        for (int i = 0; i < registers.length; i++) {
            if (registers[i] == null) {
                registers[i] = playerDeck.remove(0);
            }
        }
        session.sendCardsYouGotNow(getPlayerController(), getRegistersAsStringArray());
    }

    /**
     * @return True if all registers are full, false otherwise
     */
    public boolean hasPlayerFinishedProgramming() {
        for (final IPlayableCard c : this.registers) {
            if (c == null) {
                return false;
            }
        }
        return true;
    }

    public IPlayableCard getCardByName(final String cardName) {
        for (IPlayableCard c : this.playerHand) {
            if (c.getCardType().equals(cardName)) {
                return c;
            }
        }
        return null;
    }

    public PlayerController getPlayerController()
    {
        return this.playerController;
    }

    public Robot getPlayerRobot()
    {
        return this.playerRobot;
    }

    public ArrayList<IPlayableCard> getPlayerDeck()
    {
        return this.playerDeck;
    }

    public ArrayList<IPlayableCard> getDiscardPile()
    {
        return this.discardPile;
    }

    public ArrayList<IPlayableCard> getPlayerHand()
    {
        return this.playerHand;
    }

    public IPlayableCard[] getRegisters()
    {
        return this.registers;
    }

    public IPlayableCard getCardByRegisterIndex(final int idx)
    {
        return this.registers[idx];
    }

    public String[] getRegistersAsStringArray() {
        final String[] registersArray = new String[this.registers.length];
        for (int i = 0; i < this.registers.length; i++) {
            registersArray[i] = this.registers[i].getCardType();
        }
        return registersArray;
    }

    public String[] getPlayerHandAsStringArray() {
        final String[] handArray = new String[this.playerHand.size()];
        for (int i = 0; i < this.playerHand.size(); i++) {
            handArray[i] = this.playerHand.get(i).getCardType();
        }
        return handArray;
    }

    public void setCardInRegister(final int idx, final IPlayableCard newCard) {
        if (this.registers[idx] != null) {
            this.discardPile.add(this.registers[idx]);
        }
        this.registers[idx] = newCard;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    public int getCheckpointsCollected()
    {
        return checkpointsCollected;
    }

    public void setCheckpointsCollected(final int checkpointsCollected) {
        this.checkpointsCollected = checkpointsCollected;
    }
    public GameMode getGameMode() {
        return gameMode;
    }

    public int getEnergyCollected()
    {
        return energyCollected;
    }

    public void setEnergyCollected(int energyCollected)
    {
        this.energyCollected = energyCollected;
    }

    public ArrayList<AUpgradeCard> getUpgradeCards()
    {
        return upgradeCards;
    }
}
