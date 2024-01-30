package sep.view.clientcontroller;

import sep.view.scenecontrollers.   GameJFXController;
import sep.view.json.               RDefaultServerRequestParser;
import sep.view.lib.                RRegisterCard;
import sep.view.lib.                EGamePhase;
import sep.view.lib.                EFigure;
import sep.view.lib.                RPopUpMask;
import sep.view.lib.                EPopUp;
import sep.view.lib.                RCheckpointMask;
import sep.view.viewcontroller.     ViewSupervisor;

import java.util.                   ArrayList;
import java.util.                   Arrays;
import java.util.                   Objects;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import org.json.                    JSONArray;
import org.json.                    JSONObject;
import java.util.concurrent.atomic. AtomicBoolean;

/**
 * Holds the state of the game. Like player positions, player names, cards in hand, cards on table, etc.
 * Does not contain actual game logic. If the view or agent needs to know something about the game, it will be
 * stored here. This object is shared across all threads and is automatically updated by the server listener.
 */
public enum EGameState
{
    INSTANCE;

    private static final Logger l = LogManager.getLogger(EGameState.class);

    public static final int         MAX_CHAT_MESSAGE_LENGTH     = 64;

    public static final String[]    PHASE_NAMES                 = new String[] {"Registration Phase", "Upgrade Phase", "Programming Phase", "Activation Phase"};
    private EGamePhase              currentPhase;
    private int                     currentRegister;

    /**
     * Stores information that is shared for all players. The player cards for one client are unique to them and must
     * be stored here in the Game State. Information that is not unique for one player like their selected robot or
     * their name is stored in the {@link RemotePlayer} object.
     */
    private final ArrayList<RemotePlayer>   remotePlayers;
    private RemotePlayer                    currentPlayer;

    private String[]                            serverCourses;
    private String                              currentServerCourse;
    private JSONArray                           currentServerCourseJSON;
    private final ArrayList<RCheckpointMask>    currentCheckpointLocations;

    private final String[]                  registers;
    private final ArrayList<String>         gotRegisters;
    /** The first three slots represent the temporary upgrade cards, the last slots the permanent ones. */
    private final String[]                  boughtUpgradeCards;
    private final ArrayList<String>         upgradeShopCards;

    private int                             damageCardsCountToDraw;
    private ArrayList<String>               selectedDamageCards;

    private boolean                         shopActive;
    private RemotePlayer                    winner;

    private final AtomicBoolean             bProgrammingTimerRunning;
    private final AtomicBoolean             bMemorySwapPlayed;
    private final AtomicBoolean             bSpamBlockerPlayed;

    private EGameState()
    {
        this.currentPhase               = EGamePhase.INVALID;
        this.currentRegister            = 0;

        this.remotePlayers              = new ArrayList<RemotePlayer>();
        this.currentPlayer              = null;

        this.serverCourses              = new String[0];
        this.currentServerCourse        = "";
        this.currentServerCourseJSON    = null;
        this.currentCheckpointLocations = new ArrayList<RCheckpointMask>();

        this.registers                  = new String[5];
        this.gotRegisters               = new ArrayList<String>();
        this.boughtUpgradeCards         = new String[6];
        this.upgradeShopCards           = new ArrayList<String>();

        this.damageCardsCountToDraw     = 0;
        this.selectedDamageCards        = new ArrayList<String>();

        this.shopActive                 = false;
        this.winner                     = null;

        this.bProgrammingTimerRunning   = new AtomicBoolean(false);
        this.bMemorySwapPlayed          = new AtomicBoolean(false);
        this.bSpamBlockerPlayed         = new AtomicBoolean(false);

        return;
    }

    public static void reset()
    {
        EGameState.INSTANCE.currentPhase                = EGamePhase.INVALID;
        EGameState.INSTANCE.currentRegister             = 0;

        EGameState.INSTANCE.remotePlayers                .clear();
        EGameState.INSTANCE.currentPlayer               = null;

        EGameState.INSTANCE.serverCourses               = new String[0];
        EGameState.INSTANCE.currentServerCourse         = "";
        EGameState.INSTANCE.currentServerCourseJSON     = null;
        EGameState.INSTANCE.currentCheckpointLocations   .clear();

        EGameState.INSTANCE.registers[0]                = null;
        EGameState.INSTANCE.registers[1]                = null;
        EGameState.INSTANCE.registers[2]                = null;
        EGameState.INSTANCE.registers[3]                = null;
        EGameState.INSTANCE.registers[4]                = null;
        EGameState.INSTANCE.gotRegisters                 .clear();

        EGameState.INSTANCE.boughtUpgradeCards[0]       = null;
        EGameState.INSTANCE.boughtUpgradeCards[1]       = null;
        EGameState.INSTANCE.boughtUpgradeCards[2]       = null;
        EGameState.INSTANCE.boughtUpgradeCards[3]       = null;
        EGameState.INSTANCE.boughtUpgradeCards[4]       = null;
        EGameState.INSTANCE.boughtUpgradeCards[5]       = null;
        EGameState.INSTANCE.upgradeShopCards             .clear();

        EGameState.INSTANCE.damageCardsCountToDraw      = 0;
        EGameState.INSTANCE.selectedDamageCards          .clear();

        EGameState.INSTANCE.shopActive                  = false;
        EGameState.INSTANCE.winner                      = null;

        EGameState.INSTANCE.bProgrammingTimerRunning    .set(false);
        EGameState.INSTANCE.bMemorySwapPlayed           .set(false);
        EGameState.INSTANCE.bSpamBlockerPlayed          .set(false);

        return;
    }

    // region Getters and Setters

    private RemotePlayer getRemotePlayer(final int id)
    {
        for (final RemotePlayer rp : EGameState.INSTANCE.remotePlayers)
        {
            if (rp.getPlayerID() == id)
            {
                return rp;
            }

            continue;
        }

        return null;
    }

    private boolean isRemotePlayerAlreadyAdded(final int playerID)
    {
        for (final RemotePlayer rp : EGameState.INSTANCE.remotePlayers)
        {
            if (rp.getPlayerID() == playerID)
            {
                return true;
            }

            continue;
        }

        return false;
    }

    public static void addRemotePlayer(final RDefaultServerRequestParser dsrp)
    {
        /* TODO This is not safe at all. More type checking needed. */

        if (EGameState.INSTANCE.isRemotePlayerAlreadyAdded(dsrp.getPlayerID()))
        {
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayer(dsrp.getPlayerID())).setPlayerName(dsrp.getPlayerName());
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayer(dsrp.getPlayerID())).setFigure(dsrp.getFigure());

            l.debug("Remote player {} already added. Updating his name and figures.", dsrp.getPlayerID());
            return;
        }

        EGameState.INSTANCE.remotePlayers.add(EClientInformation.INSTANCE.isAgent() ? new AgentRemotePlayerData(dsrp.getPlayerID(), dsrp.getPlayerName(), dsrp.getFigure(), false) : new RemotePlayer(dsrp.getPlayerID(), dsrp.getPlayerName(), dsrp.getFigure(), false));

        return;
    }

    public void removeRemotePlayer(final int id)
    {
        this.remotePlayers.removeIf(rp -> rp.getPlayerID() == id);

        if (EClientInformation.INSTANCE.isAgent())
        {
            return;
        }

        ViewSupervisor.onPlayerRemoved();

        return;
    }

    public boolean isPlayerRobotUnavailable(final EFigure figure)
    {
        for (final RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getFigure() == figure)
            {
                return true;
            }

            continue;
        }

        return false;
    }

    public boolean hasClientSelectedARobot()
    {
        for (final RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID() && rp.getFigure() != EFigure.INVALID)
            {
                return true;
            }

            continue;
        }

        return false;
    }

    public RemotePlayer getRemotePlayerByFigureID(final EFigure figure)
    {
        for (final RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getFigure() == figure)
            {
                return rp;
            }

            continue;
        }

        return null;
    }

    public RemotePlayer getRemotePlayerByPlayerID(final int id)
    {
        for (final RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == id)
            {
                return rp;
            }

            continue;
        }

        return null;
    }

    public RemotePlayer getRemotePlayerByPlayerName(final String name)
    {
        for (final RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerName().equals(name))
            {
                return rp;
            }

            continue;
        }

        return null;
    }

    public EFigure getClientSelectedFigure()
    {
        for (final RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
            {
                return rp.getFigure();
            }

            continue;
        }

        return EFigure.INVALID;
    }

    public RemotePlayer[] getRemotePlayers()
    {
        return this.remotePlayers.toArray(new RemotePlayer[0]);
    }

    public RemotePlayer getClientRemotePlayer()
    {
        for (final RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
            {
                return rp;
            }

            continue;
        }

        if (EClientInformation.INSTANCE.hasPlayerID())
        {
            l.warn("Could not find the client remote player. If this was during initialization, this is can be ignored. Searched for {}, but found {}.", EClientInformation.INSTANCE.getPlayerID(), Arrays.toString(this.remotePlayers.toArray(new RemotePlayer[0])));
            return null;
        }

        l.warn("Could not find the client remote player because the client player id has not been yet set.");

        return null;
    }

    public String[] getServerCourses()
    {
        return this.serverCourses;
    }

    public void setServerCourses(final String[] courses)
    {
        this.serverCourses = courses;
        return;
    }

    public String getCurrentServerCourse()
    {
        return this.currentServerCourse;
    }

    public void setCurrentServerCourse(final String course)
    {
        this.currentServerCourse = course;
        return;
    }

    /** Only use for development. */
    public JSONObject getAssumedServerCourseRawJSON()
    {
        return new JSONObject(String.format("{\"messageType\":\"GameStarted\",\"messageBody\":{\"gameMap\":%s}}", this.getCurrentServerCourseJSON().toString()));
    }

    public JSONArray getCurrentServerCourseJSON()
    {
        return this.currentServerCourseJSON;
    }

    public void setCurrentServerCourseJSON(JSONArray currentServerCourseJSON)
    {
        this.currentServerCourseJSON = currentServerCourseJSON;
        return;
    }

    public EGamePhase getCurrentPhase()
    {
        return this.currentPhase;
    }

    public void setCurrentPhase(final EGamePhase phase)
    {
        if (this.currentPhase == phase)
        {
            l.warn("Tried to set the current phase to the same phase as before. Ignoring.");
            return;
        }

        this.clearRCardsFromRemotes();

        if (EClientInformation.INSTANCE.isAgent())
        {
            this.currentPhase = phase;

            if (this.currentPhase != EGamePhase.PROGRAMMING && this.currentPhase != EGamePhase.ACTIVATION)
            {
                this.clearAllRegisters();
            }

            if (this.currentPhase == EGamePhase.ACTIVATION)
            {
                this.resetPlayersForActivation();
                this.currentRegister = 1;
            }

            return;
        }

        // TODO
        //      We exited the registration phase, and there will be no more clickable actions on the course view,
        //      therefore, we re-render the course view to remove the hover effect. Note, this is not efficient, we
        //      must implement a faster way, where we just remove the hover effect and not re-render the whole
        //      course view.
        if (this.currentPhase == EGamePhase.REGISTRATION)
        {
            ViewSupervisor.updateCourseView();
        }

        this.currentPhase = phase;
        this.currentPlayer = null;
        if (this.currentPhase != EGamePhase.PROGRAMMING && this.currentPhase != EGamePhase.ACTIVATION)
        {
            this.clearAllRegisters();
            ViewSupervisor.updateFooter();
        }
        if (this.currentPhase == EGamePhase.ACTIVATION)
        {
            this.resetPlayersForActivation();
            this.currentRegister = 1;
        }

        ViewSupervisor.updatePhase();
        ViewSupervisor.updateFooterState(this.currentPhase != EGamePhase.PROGRAMMING);

        return;
    }

    private void resetPlayersForActivation()
    {
        for (final RemotePlayer rp : this.remotePlayers)
        {
            rp.setSelectionFinished(false);
            continue;
        }

        return;
    }

    public void setCurrentPlayer(final int playerID, final boolean bDiscardViewUpdate)
    {
        this.currentPlayer = this.getRemotePlayer(playerID);

        if (bDiscardViewUpdate)
        {
            return;
        }

        ViewSupervisor.updatePlayerView();

        return;
    }

    public RemotePlayer getCurrentPlayer()
    {
        return this.currentPlayer;
    }

    public String getRegister(final int idx)
    {
        if (idx < 0 || idx >= this.registers.length)
        {
            l.warn("Tried getting a register card that is out of bounds [{} / {}].", idx, this.registers.length);
            return null;
        }

        return this.registers[idx];
    }

    public String getGotRegister(final int idx)
    {
        if (idx < 0 || idx >= this.gotRegisters.size())
        {
            /* This is normal, therefore, we may trace this. */
            l.trace("Tried getting a got register card that is out of bounds [{} / {}].", idx, this.gotRegisters.size());
            return null;
        }

        return this.gotRegisters.get(idx);
    }

    public String getBoughtUpgradeCard(final int idx)
    {
        if (idx < 0 || idx >= this.boughtUpgradeCards.length)
        {
            l.warn("Tried getting a upgrade card that is out of bounds [{} / {}].", idx, this.boughtUpgradeCards.length);
            return null;
        }

        return this.boughtUpgradeCards[idx];
    }

    public String[] getRegisters()
    {
        return this.registers;
    }

    public ArrayList<String> getGotRegisters()
    {
        return this.gotRegisters;
    }

    public void clearAllRegisters()
    {
        this.registers[0] = null;
        this.registers[1] = null;
        this.registers[2] = null;
        this.registers[3] = null;
        this.registers[4] = null;

        this.gotRegisters.clear();

        return;
    }

    public void clearGotRegisters()
    {
        this.gotRegisters.clear();
        return;
    }

    public void addRegister(final int idx, final String register)
    {
        if (idx < 0 || idx >= this.registers.length)
        {
            return;
        }

        this.registers[idx] = register;

        return;
    }

    public void addGotRegister(final String register)
    {
        this.gotRegisters.add(register);
        return;
    }

    /**
     * Sets a register slot from a given got register slot.
     *
     * @param tIdx Target index
     * @param oIdx Origin index
     */
    public void setRegister(final int tIdx, final int oIdx)
    {
        if (tIdx < 0 || tIdx >= this.registers.length)
        {
            return;
        }

        if (oIdx < 0 || oIdx >= this.gotRegisters.size())
        {
            return;
        }

        if (this.registers[tIdx] != null)
        {
            return;
        }

        this.registers[tIdx] = this.gotRegisters.get(oIdx);
        this.gotRegisters.set(oIdx, null);

        return;
    }

    /**
     * Undoes a set register slot and add it back to the got registers.
     *
     * @param oIdx     Origin index from register
     */
    public void undoRegister(int oIdx)
    {
        if (oIdx < 0 || oIdx >= this.registers.length)
        {
            return;
        }

        if (this.registers[oIdx] == null)
        {
            return;
        }

        if (!this.gotRegisters.contains(null))
        {
            l.error("Could not undo register. Got registers are full.");
            return;
        }

        this.gotRegisters.set(this.gotRegisters.indexOf(null), this.registers[oIdx]);
        this.registers[oIdx] = null;

        return;

    }

    public boolean areRegistersFull()
    {
        for (final String s : this.registers)
        {
            if (s == null)
            {
                return false;
            }

            continue;
        }

        return true;
    }

    public void setSelectionFinished(final int id)
    {
        Objects.requireNonNull(this.getRemotePlayer(id)).setSelectionFinished(true);
        // TODO Also not efficient here. We must not update the whole HUD, but only the player view and if not already
        //      existing, the new timer view.
        ViewSupervisor.updatePlayerView();
        return;
    }

    public RemotePlayer getWinner()
    {
        return winner;
    }

    public void setWinner(final int id)
    {
        this.winner = this.getRemotePlayerByPlayerID(id);
        return;
    }

    public int getCurrentRegister()
    {
        return this.currentRegister;
    }

    public void setCurrentRegister(final int register)
    {
        this.currentRegister = register;
        return;
    }

    public int getDamageCardsCountToDraw()
    {
        return this.damageCardsCountToDraw;
    }

    public void setDamageCardsCountToDraw(final int count)
    {
        this.damageCardsCountToDraw = count;
        return;
    }

    public void subtractDamageCardsCountsToDrawByOne()
    {
        this.damageCardsCountToDraw = this.damageCardsCountToDraw - 1;
        return;
    }

    public ArrayList<String> getSelectedDamageCards()
    {
        return this.selectedDamageCards;
    }

    public void addSelectedDamageCards(final int idx, final String damageCard)
    {
        this.selectedDamageCards.add(damageCard);
        l.debug("Added {} to selected damageCards", damageCard);
        return;
    }

    public void clearSelectedDamageCards()
    {
        this.selectedDamageCards.clear();
        return;
    }

    private void clearRCardsFromRemotes()
    {
        for (final RemotePlayer rp : this.remotePlayers)
        {
            rp.clearPlayedRCards();
            continue;
        }

        return;
    }

    public void addRCardsToRemotes(final RRegisterCard[] currentRegisterCards)
    {
        for (final RRegisterCard rrc : currentRegisterCards)
        {
            Objects.requireNonNull(this.getRemotePlayer(rrc.ctrlID())).addPlayedRCards(rrc.card());
            continue;
        }

        return;
    }

    public ArrayList<RCheckpointMask> getCurrentCheckpointLocations()
    {
        return this.currentCheckpointLocations;
    }

    public void refillShop(final ArrayList<String> cards)
    {
        for (int i = 0; i < this.upgradeShopCards.size(); ++i)
        {
            if (this.upgradeShopCards.get(i) == null)
            {
                this.upgradeShopCards.set(i, cards.get(0));
                cards.remove(0);
                continue;
            }

            continue;
        }

        /* If the ctrl count was decreased (e.g., through a disconnect), the shop size will also shrink. */
        this.upgradeShopCards.removeIf(Objects::isNull);

        /* The same if the ctrl count increases. */
        if (!cards.isEmpty())
        {
            this.upgradeShopCards.addAll(cards);
        }

        return;
    }

    public void exchangeShop(final ArrayList<String> cards)
    {
        this.upgradeShopCards.clear();
        this.refillShop(cards);
        return;
    }

    public ArrayList<String> getUpgradeShop()
    {
        return this.upgradeShopCards;
    }

    public String getUpgradeShop(final int idx)
    {
        return this.upgradeShopCards.get(idx);
    }

    public String[] getBoughtUpgradeCard()
    {
        return this.boughtUpgradeCards;
    }

    private void addBoughtUpgradeCard(final String card)
    {
        /* A little bit cheeky. */
        final boolean isTemporary = card.contains("MemorySwap") || card.contains("SpamBlocker");

        for (int i = isTemporary ? 0 : 3; i < (isTemporary ? 3 : this.boughtUpgradeCards.length); ++i)
        {
            if (this.boughtUpgradeCards[i] == null)
            {
                this.boughtUpgradeCards[i] = card;
                l.info("Added the {} upgrade card \"{}\" to bought upgrade cards. Total upgrades bought: {}.", isTemporary ? "temporary" : "permanent", card, Arrays.toString(this.boughtUpgradeCards));
                return;
            }

            continue;
        }

        l.fatal("Could not add the {} upgrade card \"{}\" to bought upgrade cards. Bought upgrade cards are full.", isTemporary ? "temporary" : "permanent", card);
        l.fatal(Arrays.toString(this.boughtUpgradeCards));
        GameInstance.kill(GameInstance.EXIT_FATAL);

        return;
    }

    private void removeUpgradeCardFromShop(final String card)
    {
        this.upgradeShopCards.set(this.upgradeShopCards.indexOf(card), null);
        return;
    }

    public void onUpgradeCardBought(final int id, final String card)
    {
        final boolean bLocalOwner = id == EClientInformation.INSTANCE.getPlayerID();

        this.removeUpgradeCardFromShop(card);

        if (bLocalOwner)
        {
            this.addBoughtUpgradeCard(card);
        }

        Objects.requireNonNull(this.getRemotePlayerByPlayerID(id)).getBoughtUpgradeCards().add(card);

        if (EClientInformation.INSTANCE.isAgent())
        {
            return;
        }

        ViewSupervisor.updatePlayerView();

        return;
    }

    public void setProgrammingTimerRunning(final boolean bRunning)
    {
        this.bProgrammingTimerRunning.set(bRunning);

        if (!bRunning)
        {
            try
            {
                ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).resetProgrammingTimeline();
                return;
            }
            catch (final ClassCastException e)
            {
                l.error("Could not cast the current scene controller to GameJFXController during programming timer reset. Ignoring.");
                l.error(e.getMessage());
                return;
            }
        }

        return;
    }

    public boolean isProgrammingTimerRunning()
    {
        return this.bProgrammingTimerRunning.get();
    }

    /** For developing purposes only!
     * @deprecated
     */
    public String[] getBoughtUpgradeCards()
    {
        return this.boughtUpgradeCards;
    }

    public boolean isMemorySwapPlayed()
    {
        return this.bMemorySwapPlayed.get();
    }

    public void setMemorySwapPlayed(final boolean bPlayed)
    {
        this.bMemorySwapPlayed.set(bPlayed);
        return;
    }

    public void overrideGotRegister(final int idx, final String newCard)
    {
        this.gotRegisters.set(idx, newCard);
        return;
    }

    public boolean isSpamBlockerPlayed()
    {
        return this.bSpamBlockerPlayed.get();
    }

    public void setSpamBlockerPlayed(final boolean bPlayed)
    {
        this.bSpamBlockerPlayed.set(bPlayed);
        return;
    }

    public void executePostCardPlayedBehaviour(final int playerID, final String card)
    {
        final RemotePlayer rp = this.getRemotePlayerByPlayerID(playerID);

        assert rp != null;

        if (Objects.equals(card, "MemorySwap") || Objects.equals(card, "SpamBlocker"))
        {
            rp.getBoughtUpgradeCards().remove(card);
            return;
        }

        l.error("Could not execute post card played behaviour for card \"{}\". Ignoring.", card);

        return;
    }

    // endregion Getters and Setters

}
