package sep.view.clientcontroller;

import sep.view.json.RDefaultServerRequestParser;
import sep.view.lib.EShopState;
import sep.view.lib.RRegisterCard;
import sep.view.viewcontroller.ViewSupervisor;
import sep.view.lib.EGamePhase;
import sep.view.lib.EFigure;

import java.util.ArrayList;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

/**
 * Holds the state of the game. Like player positions, player names, cards in hand, cards on table, etc.
 * Does not contain actual game logic. If the view needs to know something about the game, it will be stored here. This
 * object is shared across all threads and is automatically updated by the server listener.
 */
public enum EGameState
{
    INSTANCE;

    private static final Logger l = LogManager.getLogger(EGameState.class);

    public static final int MAX_CHAT_MESSAGE_LENGTH = 64;

    public static final String[] PHASE_NAMES = new String[] {"Registration Phase", "Upgrade Phase", "Programming Phase", "Activation Phase"};
    private EGamePhase currentPhase;

    private int currentRegister;

    /**
     * Stores information that is shared for all players. The player cards for one client are unique to them and must
     * be stored here in the Game State. Information that is not unique for one player like their selected robot or
     * their name is stored in the {@link RemotePlayer} object.
     */
    private final ArrayList<RemotePlayer> remotePlayers;
    private RemotePlayer currentPlayer;

    private String[] serverCourses;
    private String currentServerCourse;
    private JSONArray currentServerCourseJSON;

    private final String[] registers;
    private final ArrayList<String> gotRegisters;

    public static final String[] SHOP_STATES = new String[] {"Upgrade", "Damage", "Reboot"};
    private EShopState shopState;
    private ArrayList<String> temporayUpgradeCards;
    private ArrayList<String> permanentUpgradeCards;
    private String[] shopSlots;

    private int damageCardsCountToDraw;
    private ArrayList<String> selectedDamageCards;

    private boolean shopActive;
    private RemotePlayer winningPlayer;

    private EGameState()
    {
        this.currentPhase = EGamePhase.INVALID;
        this.shopState = EShopState.DEACTIVATED;

        this.remotePlayers = new ArrayList<RemotePlayer>();
        this.currentPlayer = null;

        this.serverCourses = new String[0];
        this.currentServerCourse = "";
        this.currentServerCourseJSON = null;

        this.registers = new String[5];
        this.gotRegisters = new ArrayList<String>();
        this.winningPlayer = null;

        this.selectedDamageCards = new ArrayList<>();
        this.permanentUpgradeCards = new ArrayList<>();
        this.temporayUpgradeCards = new ArrayList<>();
        this.shopSlots = new String[5];
        this. shopActive = false;

        return;
    }

    public static void reset()
    {
        EGameState.INSTANCE.currentPhase = EGamePhase.INVALID;

        EGameState.INSTANCE.remotePlayers.clear();
        EGameState.INSTANCE.currentPlayer = null;

        EGameState.INSTANCE.serverCourses = new String[0];
        EGameState.INSTANCE.currentServerCourse = "";
        EGameState.INSTANCE.currentServerCourseJSON = null;

        EGameState.INSTANCE.registers[0] = null;
        EGameState.INSTANCE.registers[1] = null;
        EGameState.INSTANCE.registers[2] = null;
        EGameState.INSTANCE.registers[3] = null;
        EGameState.INSTANCE.registers[4] = null;
        EGameState.INSTANCE.gotRegisters.clear();
        EGameState.INSTANCE.winningPlayer = null;

        EGameState.INSTANCE.permanentUpgradeCards.clear();
        EGameState.INSTANCE.temporayUpgradeCards.clear();
        EGameState.INSTANCE.shopSlots[0] = null;
        EGameState.INSTANCE.shopSlots[1] = null;
        EGameState.INSTANCE.shopSlots[2] = null;
        EGameState.INSTANCE.shopSlots[3] = null;
        EGameState.INSTANCE.shopSlots[4] = null;

        return;
    }

    // region Getters and Setters

    private RemotePlayer getRemotePlayer(int playerID)
    {
        for (RemotePlayer rp : EGameState.INSTANCE.remotePlayers)
        {
            if (rp.getPlayerID() == playerID)
            {
                return rp;
            }
        }

        return null;
    }

    private boolean isRemotePlayerAlreadyAdded(int playerID)
    {
        for (RemotePlayer rp : EGameState.INSTANCE.remotePlayers)
        {
            if (rp.getPlayerID() == playerID)
            {
                return true;
            }
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

        EGameState.INSTANCE.remotePlayers.add(new RemotePlayer(dsrp.getPlayerID(), dsrp.getPlayerName(), dsrp.getFigure(), false));

        return;
    }

    public void removeRemotePlayer(int playerID)
    {
        this.remotePlayers.removeIf(rp -> rp.getPlayerID() == playerID);
        ViewSupervisor.onPlayerRemoved();
        return;
    }

    /** If the robot at a specific index is already selected by a player. */
    public boolean isPlayerRobotUnavailable(final EFigure f)
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getFigure() == f)
            {
                return true;
            }
        }

        return false;
    }

    /** If this client has already selected a robot. */
    public boolean hasClientSelectedARobot()
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID() && rp.getFigure() != EFigure.INVALID)
            {
                return true;
            }
        }

        return false;
    }

    public RemotePlayer getRemotePlayerByFigureID(final EFigure f)
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getFigure() == f)
            {
                return rp;
            }
        }

        return null;
    }

    public RemotePlayer getRemotePlayerByPlayerID(int caller)
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == caller)
            {
                return rp;
            }
        }

        return null;
    }

    public RemotePlayer getRemotePlayerByPlayerName(String targetPlayer)
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerName().equals(targetPlayer))
            {
                return rp;
            }
        }

        return null;
    }

    public EFigure getClientSelectedFigure()
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
            {
                return rp.getFigure();
            }
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
        }

        l.warn("Could not find the client remote player. If this was during initialization, this is can be ignored.");

        return null;
    }

    public String[] getServerCourses()
    {
        return this.serverCourses;
    }

    public void setServerCourses(String[] serverCourses)
    {
        this.serverCourses = serverCourses;
        return;
    }

    public String getCurrentServerCourse()
    {
        return this.currentServerCourse;
    }

    public void setCurrentServerCourse(final String currentServerCourse)
    {
        this.currentServerCourse = currentServerCourse;
        return;
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
        for (RemotePlayer rp : this.remotePlayers)
        {
            rp.setSelectionFinished(false);
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

    public String getRegister(int idx)
    {
        if (idx < 0 || idx >= this.registers.length)
        {
            l.error("Tried getting a register card that is out of bounds [{}].", idx);
            return null;
        }

        return this.registers[idx];
    }

    public String getGotRegister(int idx)
    {
        if (idx < 0 || idx >= this.gotRegisters.size())
        {
            return null;
        }

        return this.gotRegisters.get(idx);
    }

    public String[] getRegisters()
    {
        return this.registers;
    }

    public ArrayList<String> getGotRegisters()
    {
        return this.gotRegisters;
    }

    /** Will not re-render the player head up display. */
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

    /** Will not re-render the player head up display. */
    public void clearGotRegisters()
    {
        this.gotRegisters.clear();
        return;
    }

    /** Will not re-render the player head up display. */
    public void clearShopSlots(){
        for(int i = 0; i < shopSlots.length; i++){
            shopSlots[i] = null;
        }
    }
    public void addRegister(int idx, String register)
    {
        if (idx < 0 || idx >= this.registers.length)
        {
            return;
        }

        this.registers[idx] = register;

        return;
    }

    public void addGotRegister(String register)
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
    public void setRegister(int tIdx, int oIdx)
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
        for (String s : this.registers)
        {
            if (s == null)
            {
                return false;
            }
        }

        return true;
    }

    public void setSelectionFinished(final int playerID)
    {
        Objects.requireNonNull(this.getRemotePlayer(playerID)).setSelectionFinished(true);
        // TODO Also not efficient here. We must not update the whole HUD, but only the player view and if not already
        //      existing, the new timer view.
        ViewSupervisor.updatePlayerView();
        return;
    }

    public RemotePlayer getWinningPlayer() {
        return winningPlayer;
    }

    public void determineWinningPlayer(int playerID) {
        this.winningPlayer = getRemotePlayerByPlayerID(playerID);
    }

    public int getCurrentRegister() {
        return currentRegister;
    }

    public void setCurrentRegister(int currentRegister) {
        this.currentRegister = currentRegister;
    }

    public String getTemporaryUpgradeCard(int idx)
    {
        if (idx < 0 || idx >= this.temporayUpgradeCards.size())
        {
            //l.debug("Tried getting temporaryUpgradeCard from an emptySlot");
            return null;
        }

        return this.temporayUpgradeCards.get(idx);
    }

    public String getPermanentUpgradeCard(int idx)
    {
        if (idx < 0 || idx >= this.permanentUpgradeCards.size())
        {
            //l.debug("Tried getting permanentUpgradeCard from an emptySlot");
            return null;
        }

        return this.permanentUpgradeCards.get(idx);
    }

    public String getShopSlot(int idx){
        if(idx < 0 || idx > shopSlots.length){
            l.debug("Tried getting content of shopSlot outside of range of Slots");
            return null;
        }
        return this.shopSlots[idx];
    }

    public void addTemporaryUpgradeCards(String temporaryUpgradeCard)
    {
        if (this.temporayUpgradeCards.size() >= 3)
        {
            l.warn(String.format("Tried adding temporaryUpgradeCard in Slot whilst filled"));
            return;
        }

        this.temporayUpgradeCards.add(temporaryUpgradeCard);

        return;
    }

    public void addPermanentUpgradeCard(String permanentUpgradeCard)
    {
        if (this.permanentUpgradeCards.size() <= 3)
        {
            l.warn(String.format("Tried adding permanentUpgradeCard in Slot whilst filled"));
            return;
        }

        this.permanentUpgradeCards.add(permanentUpgradeCard);

        return;
    }

    public void addShopSlot(int idx, String elementName){
        if (idx < 0 || idx >= this.shopSlots.length)
        {
            l.debug(String.format("Tried adding %s outside of shopSlotRange on Position %s"), elementName, idx);
            return;
        }

        if(shopSlots[idx] != null) {
            l.debug(String.format("Tried adding %s on filled shopSlot %s", elementName, idx));
            return;
        }
        shopSlots[idx] = elementName;
        return;
    }

    public boolean isShopFull(){
        for(String s : shopSlots){
            if(s == null){
                return false;
            }
        }
        return true;
    }

    public boolean isShopActive() {
        return shopActive;
    }

    public void setShopActive(boolean shopActive) {
        this.shopActive = shopActive;
    }

    public void setShopState(EShopState state){
        this.shopState = state;
    }

    public EShopState getShopState(){
        return this.shopState;
    }
    // endregion Getters and Setters


    public int getDamageCardsCountToDraw() {
        return damageCardsCountToDraw;
    }

    public void setDamageCardsCountToDraw(int damageCardsCountToDraw) {
        this.damageCardsCountToDraw = damageCardsCountToDraw;
    }

    public void subtractDamageCardsCountsToDrawByOne(){
        this.damageCardsCountToDraw = this.damageCardsCountToDraw - 1;
    }

    public ArrayList<String> getSelectedDamageCards() {
        return selectedDamageCards;
    }

    public void addSelectedDamageCards(int idx, String damageCard) {
        this.selectedDamageCards.add(damageCard);
        l.debug("Added " + damageCard + "to selected damageCards");
    }

    public void clearSelectedDamageCards(){
        this.selectedDamageCards.clear();
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

}
