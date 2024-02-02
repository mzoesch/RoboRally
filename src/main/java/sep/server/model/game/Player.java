package sep.server.model.game;

import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.builder.DeckBuilder;
import sep.server.model.IOwnershipable;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.tiles.Coordinate;
import sep.server.viewmodel.EServerInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Player {
    private static final Logger l = LogManager.getLogger(Player.class);

    private int priority;

    private final IOwnershipable ctrl;
    private final Robot playerRobot;

    /** The cards a player can draw from his pillar. */
    private final ArrayList<IPlayableCard> playerDeck;
    /**
     * The cards that are being discarded in one phase.
     * These cards will be shuffled and added to the {@link #playerDeck} in the next programming phase.
     */
    private final ArrayList<IPlayableCard> discardPile;
    private final ArrayList<AUpgradeCard> upgradeCards;
    private final IPlayableCard[] registers;
    /** The nine programming cards a player has drawn. */
    private final ArrayList<IPlayableCard> playerHand;
    private int energyCollected;
    private int checkpointsCollected;
    private Boolean hasAdminPrivilegeUpgrade;

    private RAdminPrivilegeMask chosenRegisterAdminPrivilegeUpgrade;

    private String[] memorySwapCards;

    private final ArrayList<String> boughtUpgradeCards;


    public Player(final IOwnershipable ctrl, final Course currentCourse) {
        this.ctrl = ctrl;
        this.playerRobot = new Robot(false, this, currentCourse);

        this.playerDeck = new DeckBuilder().buildProgrammingDeck();
        Collections.shuffle(this.playerDeck);
        this.discardPile = new ArrayList<>();
        this.upgradeCards = new ArrayList<>();
        this.registers = new IPlayableCard[5];
        this.playerHand = new ArrayList<>();
        this.hasAdminPrivilegeUpgrade = false;
        this.chosenRegisterAdminPrivilegeUpgrade = null;

        this.energyCollected = GameMode.STARTING_ENERGY;

        this.checkpointsCollected = 0;

        this.boughtUpgradeCards = new ArrayList<String>();

        this.chosenRegisterAdminPrivilegeUpgrade = null;

        return;
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
            l.warn("Player {} has already finished programming and, therefore, cannot change their programming registers anymore.", this.ctrl.getName());
            return;
        }

        if (pos < 0 || pos > 4) {
            l.error("Invalid register position: " + pos);
            return;
        }

        final IPlayableCard playableCard = this.getCardByName(card);

        if (playableCard == null) {
            this.registers[pos] = null;
            if (this.ctrl instanceof PlayerController pc)
            {
                pc.getSession().sendCardSelected(this.ctrl.getPlayerID(), pos, false);
            }
            l.debug("Player {} has removed card from register {}.", this.ctrl.getPlayerID(), pos);
            return;
        }

        this.registers[pos] = playableCard;
        if (this.ctrl instanceof PlayerController pc)
        {
            pc.getSession().sendCardSelected(getController().getPlayerID(), pos, true);
        }

        l.debug("Player {} has added card {} to register {}.", this.ctrl.getPlayerID(), this.registers[pos], pos);

        l.debug("Checking if player {} has finished programming. Their current registers are: {}.", this.ctrl.getPlayerID(), this.registers);
        if (this.hasPlayerFinishedProgramming())
        {
            l.debug("Player {} has finished programming. Notifying session.", this.ctrl.getPlayerID());
            this.ctrl.getAuthGameMode().getSession().broadcastProgrammingSelectionFinished(this.ctrl.getPlayerID());

            if (this.ctrl instanceof final PlayerController pc)
            {
                if (this.getAuthGameMode().getSession().haveAllPlayersFinishedProgramming())
                {
                    l.info("All players have finished programming in time. Interrupting Programming Timer Service.");

                    if (this.getAuthGameMode().getProgrammingTimerService() == null)
                    {
                        this.getAuthGameMode().executePostProgrammingPhaseTimerServiceBehavior();
                        return;
                    }

                    this.getAuthGameMode().getProgrammingTimerService().interrupt();

                    return;
                }

                if (pc.isRemoteAgent())
                {
                    l.debug("Player {} is a remote agent. Therefore, the timer will not be started nor interrupted.", this.ctrl.getPlayerID());
                    return;
                }

                if (this.getAuthGameMode().isProgrammingTimerServiceRunning())
                {
                    return;
                }

                l.debug("Player {} has finished programming and is the first player to do so. Starting programming timer.", this.ctrl.getPlayerID());
                this.getAuthGameMode().startProgrammingTimerService();

                return;
            }

            l.debug("Player {} is a local player. Therefore, the timer will not be started nor interrupted.", this.ctrl.getPlayerID());

            return;
        }

    }

    private IPlayableCard drawCardFromHand()
    {
        return this.playerHand.remove(0);
    }

    public void executeIncompleteProgrammingBehavior()
    {
        if (this.ctrl instanceof PlayerController pc)
        {
            l.debug("Player {} has not finished programming in time. Their current hand is: {}.", this.ctrl.getPlayerID(), this.registers);

            final ArrayList<IPlayableCard> addedCards = new ArrayList<IPlayableCard>();

            for (int i = 0; i < this.registers.length; ++i)
            {
                if (this.registers[i] == null)
                {
                    while (true)
                    {
                        final IPlayableCard card = this.drawCardFromHand();
                        if (i == 0 && Objects.equals(card.getCardType(), "Again"))
                        {
                            this.playerHand.add(card);
                            continue;
                        }

                        this.registers[i] = card;
                        break;
                    }

                    addedCards.add(this.registers[i]);

                    continue;
                }

                continue;
            }

            if (addedCards.isEmpty())
            {
                l.warn("Player {}'s registers are full. No cards have been added while post incomplete programming behavior.", this.ctrl.getPlayerID());
                return;
            }

            l.debug("Player {}'s hand cards after incomplete programming task execution: {}. Notifying them.", this.ctrl.getPlayerID(), this.registers);

            pc.getSession().sendIncompleteProgrammingCards(pc, addedCards.stream().map(IPlayableCard::getCardType).toArray(String[]::new));

            return;
        }

        l.fatal("A local player has not finished programming in time. This must never happen.");
        EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);

        return;
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

    /**
     * After a DamageCard has been played, it is removed from the register, and a new card from the deck is placed into the register.
     */
    public void updateRegisterAfterDamageCardWasPlayed(String cardType, int currentRoundNumber) {
        ArrayList deckToUpdate = null;

        if ("VirusDamage".equals(cardType)) {
            deckToUpdate = getAuthGameMode().getVirusDeck();
        } else if ("SpamDamage".equals(cardType)) {
            deckToUpdate = getAuthGameMode().getSpamDeck();
        } else if ("TrojanHorseDamage".equals(cardType)) {
            deckToUpdate = getAuthGameMode().getTrojanDeck();
        }  else if ("WormDamage".equals(cardType)) {
            deckToUpdate = getAuthGameMode().getWormDeck();
        }

        deckToUpdate.add(getCardByRegisterIndex(currentRoundNumber));
        registers[currentRoundNumber] = null;

        if (playerDeck.isEmpty()) {
            playerDeck.add(discardPile.remove(0));
        }

        if(!playerDeck.isEmpty()) {
            IPlayableCard newCardFromDeck = playerDeck.remove(0);
            setCardInRegister(currentRoundNumber, newCardFromDeck, false);

            if (newCardFromDeck != null) {
                newCardFromDeck.playCard(this, currentRoundNumber);
            }

            assert newCardFromDeck != null;
            String newCardString = newCardFromDeck.getCardType();
            getAuthGameMode().getSession().broadcastReplacedCard(getController().getPlayerID(), currentRoundNumber, newCardString);
        }

    }

    public IPlayableCard getCardByName(final String cardName) {
        for (IPlayableCard c : this.playerHand) {
            if (c.getCardType().equals(cardName)) {
                return c;
            }
        }
        l.error("Card " + cardName + " not found.");
        return null;
    }

    public IOwnershipable getController()
    {
        return this.ctrl;
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
            IPlayableCard card = this.playerHand.get(i);
            if (card == null) {
                handArray[i] = "Card is null";
            } else {
                handArray[i] = card.getCardType();
            }
        }

        return handArray;
    }


    public void setCardInRegister(final int idx, final IPlayableCard newCard, boolean bOverrideNullSafe) {
        if (this.registers[idx] != null) {
            this.discardPile.add(this.registers[idx]);
        }
        if(newCard != null) {
            this.registers[idx] = newCard;
        } else {

            if (bOverrideNullSafe)
            {
                l.debug("Card set to register is null. But override is enabled.");
                this.registers[idx] = null;
                return;
            }

            l.error("Card set to register is null");
        }
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
    public GameMode getAuthGameMode() {
        return this.ctrl.getAuthGameMode();
    }

    public int getEnergyCollected()
    {
        return energyCollected;
    }

    public void setEnergyCollected(int energyCollected)
    {
        this.energyCollected = energyCollected;
    }

    public Boolean getHasAdminPrivilegeUpgrade() {
        return hasAdminPrivilegeUpgrade;
    }
    public void setHasAdminPrivilegeUpgrade(Boolean hasAdminPrivilegeUpgrade) {
        this.hasAdminPrivilegeUpgrade = hasAdminPrivilegeUpgrade;
    }

    public synchronized RAdminPrivilegeMask getChosenRegisterAdminPrivilegeUpgrade()
    {
        return this.chosenRegisterAdminPrivilegeUpgrade;
    }

    public synchronized void setChosenRegisterAdminPrivilegeUpgrade(final Integer chosenRegisterAdminPrivilegeUpgrade)
    {
        if (chosenRegisterAdminPrivilegeUpgrade == null)
        {
            this.chosenRegisterAdminPrivilegeUpgrade = null;
            return;
        }

        this.chosenRegisterAdminPrivilegeUpgrade = new RAdminPrivilegeMask(System.currentTimeMillis(), chosenRegisterAdminPrivilegeUpgrade);

        return;
    }

    public void setMemorySwapCards(String[] memorySwapCards) {
        this.memorySwapCards = memorySwapCards;
    }

    public String[] getMemorySwapCards() {
        return memorySwapCards;
    }
    public ArrayList<AUpgradeCard> getUpgradeCards()
    {
        return upgradeCards;
    }

    public void clearOldHand() {
        if (!playerHand.isEmpty()) {
            l.debug("P {} - Clearing old Hand. {} Cards has been moved to discard pile: {}", getController().getName(), playerHand.size(), getPlayerHandAsStringArray());
            discardPile.addAll(playerHand);
            this.playerHand.clear();
        } else {
            l.debug("P {} -  Hand is Empty. No cards has been moved to discard pile.", getController().getName());
        }
    }

    public void clearOldRegister() {
        if (Arrays.asList(registers).isEmpty()) {
            l.debug("P {} - Clearing old Register. {} Cards has been moved to discard pile: {}", getController().getName(), registers.length, getRegistersAsStringArray());
            discardPile.addAll(Arrays.asList(registers));
            Arrays.fill(registers, null);
        } else {
            l.debug("P {} - Register is empty. No cards moved to discard pile.", getController().getName());
        }
    }

    public Coordinate getPosition()
    {
        return this.playerRobot.getCurrentTile().getCoordinate();
    }

    @Override
    public String toString()
    {
        return String.format("Player{%d,%s}",this.ctrl.getPlayerID(), this.playerRobot.getCurrentTile().getCoordinate());
    }

    public ArrayList<String> getBoughtUpgradeCards()
    {
        return this.boughtUpgradeCards;
    }

    public void onMemorySwapCardPlayed(final ArrayList<String> discardedCards)
    {
        l.debug("Player {}'s hand is: {}. Removing {}.", this.ctrl.getPlayerID(), this.playerHand, discardedCards);

        for (final String card : discardedCards)
        {
            if (!this.playerHand.contains(this.getCardByName(card)))
            {
                l.fatal("Player {}'s hand does not contain card {} after MemorySwap card was played.", this.ctrl.getPlayerID(), card);
                EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
                return;
            }

            this.playerHand.remove(this.getCardByName(card));
            continue;
        }

        l.debug("Player {}'s hand after removing {} is: {}.", this.ctrl.getPlayerID(), discardedCards, this.playerHand);

        if (this.playerHand.size() > 9)
        {
            l.fatal("Player {}'s hand is larger than 9 after MemorySwap card was played.", this.ctrl.getPlayerID());
            EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
            return;
        }

        return;
    }

}
