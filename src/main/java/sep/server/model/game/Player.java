package sep.server.model.game;

import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.builder.DeckBuilder;
import sep.server.model.IOwnershipable;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.tiles.Coordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Player {
    private static final Logger l = LogManager.getLogger(Player.class);

    private int priority;

    private final IOwnershipable ctrl;
    private final Robot playerRobot;

    private final ArrayList<IPlayableCard> playerDeck;
    private final ArrayList<IPlayableCard> discardPile;
    private final ArrayList<AUpgradeCard> upgradeCards;
    private final IPlayableCard[] registers;
    private final ArrayList<IPlayableCard> playerHand;
    private int energyCollected;
    private int checkpointsCollected;
    private Boolean hasAdminPrivilegeUpgrade;

    private Integer chosenRegisterAdminPrivilegeUpgrade;

    private String[] memorySwapCards;




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
        //TODO: Make this work: this.getAuthGameMode().setEnergyBank(this.getAuthGameMode().getEnergyBank() - GameMode.STARTING_ENERGY);

        this.checkpointsCollected = 0;
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
            return;
        }

        this.registers[pos] = playableCard;
        if (this.ctrl instanceof PlayerController pc)
        {
            pc.getSession().sendCardSelected(getController().getPlayerID(), pos, true);
        }


        if (this.hasPlayerFinishedProgramming())
        {
            l.debug("Player {} has finished programming. Notifying session.", this.ctrl.getPlayerID());
            this.ctrl.getAuthGameMode().getSession().broadcastProgrammingSelectionFinished(this.ctrl.getPlayerID());

            if (this.ctrl instanceof final PlayerController pc)
            {
                if (pc.getSession().haveAllPlayersFinishedProgramming())
                {
                    l.debug("All players have finished programming in time. Interrupting timer.");
                    // TODO Interrupt timer
                    pc.getSession().getGameState().getAuthGameMode().handleNewPhase(EGamePhase.ACTIVATION);
                }

                // TODO We ignore this for now.
                // this.session.getGameState().getAuthGameMode().startProgrammingTimer();
            }

            l.debug("Player {} is a local player. Therefore, the timer will not be started nor interrupted.", this.ctrl.getPlayerID());

            return;
        }

    }

    public void handleIncompleteProgramming() {
        if (this.ctrl instanceof PlayerController pc)
        {
            discardPile.addAll(playerHand);
            playerHand.clear();
            shuffleAndRefillDeck();

            for (int i = 0; i < registers.length; i++) {
                if (registers[i] == null) {
                    registers[i] = playerDeck.remove(0);
                }
            }
            pc.getSession().sendCardsYouGotNow(pc, getRegistersAsStringArray());
            return;
        }

        l.error("A local player has not finished programming in time. This must never happen.");
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
            setCardInRegister(currentRoundNumber, newCardFromDeck);

            if (newCardFromDeck != null) {
                newCardFromDeck.playCard(this, currentRoundNumber);
            }

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

    /*public String[] getPlayerHandAsStringArray() {
        final String[] handArray = new String[this.playerHand.size()];
        for (int i = 0; i < this.playerHand.size(); i++) {
            handArray[i] = this.playerHand.get(i).getCardType();
        }
        return handArray;
    }*/

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

    public Integer getChosenRegisterAdminPrivilegeUpgrade() {
        return chosenRegisterAdminPrivilegeUpgrade;
    }

    public void setChosenRegisterAdminPrivilegeUpgrade(Integer chosenRegisterAdminPrivilegeUpgrade) {
        this.chosenRegisterAdminPrivilegeUpgrade = chosenRegisterAdminPrivilegeUpgrade;
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

}
