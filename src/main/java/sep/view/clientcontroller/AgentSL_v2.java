package sep.view.clientcontroller;

import sep.view.json.lobby.         PlayerValuesModel;
import sep.view.json.lobby.         SetStatusModel;
import sep.view.json.game.          SetStartingPointModel;
import sep.view.lib.                EGamePhase;
import sep.view.lib.                RCoordinate;
import sep.view.json.game.          SelectedCardModel;

import org.json.                    JSONException;
import org.json.                    JSONObject;
import org.json.                    JSONArray;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     BufferedReader;
import java.util.                   Objects;
import java.util.                   Arrays;

final class TileModifier
{
    private final JSONObject modifier;

    public TileModifier(final JSONObject modifier)
    {
        this.modifier = modifier;
        return;
    }

    public String getType()
    {
        return this.modifier.getString("type");
    }

}

final class Tile
{
    private final JSONArray     tile;
    private final RCoordinate   location;

    public Tile (final JSONArray tile, final RCoordinate location)
    {
        this.tile       = tile;
        this.location   = location;

        return;
    }

    public RCoordinate getLocation()
    {
        return location;
    }

    public TileModifier getModifier(final int idx)
    {
        return new TileModifier(this.tile.getJSONObject(idx));
    }

    public boolean isStartPoint()
    {
        for (int i = 0; i < this.tile.length(); ++i)
        {
            if (Objects.equals(this.getModifier(i).getType(), "StartPoint"))
            {
                return true;
            }

            continue;
        }

        return false;
    }

    public JSONArray getJSON()
    {
        return this.tile;
    }

}

interface ICourse
{
    public abstract     Tile[][]        getTiles();
    public abstract     int             getFiles();
    public abstract     int             getRanks();

    public abstract     RCoordinate     getNextFreeStartPoint();
}

enum EEnvironment implements ICourse
{
    INSTANCE;

    private enum ECourseImpl implements ICourse
    {
        INSTANCE;

        private static final Logger l = LogManager.getLogger(ECourseImpl.class);

        private JSONObject course;

        private ECourseImpl()
        {
            this.course = null;
            return;
        }

        public void setCourse(final JSONObject course)
        {
            this.course = course;
            return;
        }

        private JSONArray getTilesAsJSON() throws JSONException
        {
            return this.course.getJSONObject("messageBody").getJSONArray("gameMap");
        }

        @Override
        public Tile[][] getTiles() throws JSONException
        {
            final Tile[][] course = new Tile[this.getFiles()][this.getRanks()];

            for (int file = 0; file < this.getFiles(); ++file)
            {
                for (int rank = 0; rank < this.getRanks(); ++rank)
                {
                    course[file][rank] = new Tile(this.getTilesAsJSON().getJSONArray(file).getJSONArray(rank), new RCoordinate(file, rank));
                    continue;
                }

                continue;
            }

            return course;
        }

        @Override
        public int getFiles() throws JSONException
        {
            return this.getTilesAsJSON().toList().size();
        }

        @Override
        public int getRanks() throws JSONException
        {
            return this.getTilesAsJSON().getJSONArray(0).toList().size();
        }

        @Override
        public RCoordinate getNextFreeStartPoint() throws JSONException
        {
            final Tile[][] tiles = this.getTiles();

            for (int file = 0; file < this.getFiles(); ++file)
            {
                for (int rank = 0; rank < this.getRanks(); ++rank)
                {
                    if (tiles[file][rank].isStartPoint())
                    {
                        boolean bFree = true;

                        for (int i = 0; i < EGameState.INSTANCE.getRemotePlayers().length; ++i)
                        {
                            if (EGameState.INSTANCE.getRemotePlayers()[i].getStartingPosition() == null)
                            {
                                continue;
                            }

                            if (Objects.equals(EGameState.INSTANCE.getRemotePlayers()[i].getStartingPosition(), tiles[file][rank].getLocation()))
                            {
                                bFree = false;
                                break;
                            }

                            continue;
                        }

                        if (bFree)
                        {
                            return tiles[file][rank].getLocation();
                        }

                        continue;
                    }

                    continue;
                }

                continue;
            }

            l.fatal("Failed to find a free starting points.");
            GameInstance.kill();

            return null;
        }

        public JSONObject getCourse()
        {
            return course;
        }

    }

    private static final Logger l = LogManager.getLogger(EEnvironment.class);

    private EEnvironment()
    {
        return;
    }

    public void setCourse(final JSONObject course)
    {
        ECourseImpl.INSTANCE.setCourse(course);
        return;
    }

    private boolean isCourseMissing()
    {
        if (ECourseImpl.INSTANCE.getCourse() == null)
        {
            l.fatal("Course is null. Cannot get tiles.");
            GameInstance.kill();
            return true;
        }

        return false;
    }

    @Override
    public Tile[][] getTiles() throws JSONException
    {
        if (this.isCourseMissing())
        {
            return null;
        }

        return ECourseImpl.INSTANCE.getTiles();
    }

    @Override
    public int getFiles() throws JSONException
    {
        if (this.isCourseMissing())
        {
            return -1;
        }

        return ECourseImpl.INSTANCE.getFiles();
    }

    @Override
    public int getRanks() throws JSONException
    {
        if (this.isCourseMissing())
        {
            return -1;
        }

        return ECourseImpl.INSTANCE.getRanks();
    }

    @Override
    public RCoordinate getNextFreeStartPoint() throws JSONException
    {
        if (this.isCourseMissing())
        {
            return null;
        }

        return ECourseImpl.INSTANCE.getNextFreeStartPoint();
    }

}


/**
 * The master class of an agent. An object of this class must always run on the main thread.
 * {@inheritDoc}
 */
public final class AgentSL_v2 extends ServerListener
{
    private static final Logger l = LogManager.getLogger(AgentSL_v2.class);

    public AgentSL_v2(final BufferedReader br)
    {
        super(br);
        return;
    }

    private void evaluateProgrammingPhase()
    {
        l.warn(Arrays.toString(EGameState.INSTANCE.getRegisters()));
        l.warn(EGameState.INSTANCE.getGotRegisters().toString());

        /* Very, very primitive. Just a framework for now. Open for later construction. */
        int j = 0;
        for (int i = 0; i < 5; ++i)
        {
            while (true)
            {
                if (i == 0 && Objects.equals(EGameState.INSTANCE.getGotRegister(j), "Again"))
                {
                    ++j;
                    continue;
                }

                break;
            }

            l.warn("Setting register {}, from {}", i, j);
            EGameState.INSTANCE.setRegister(i, j);

            ++j;
            continue;
        }

        for (int i = 0; i < 5; ++i)
        {
            new SelectedCardModel(i, EGameState.INSTANCE.getRegister(i)).send();
            continue;
        }

        l.warn(Arrays.toString(EGameState.INSTANCE.getRegisters()));
        l.warn(EGameState.INSTANCE.getGotRegisters().toString());

        l.debug("Agent {} evaluated for the current programming phase. The determined cards are: {}.", EClientInformation.INSTANCE.getPlayerID(), Arrays.toString(EGameState.INSTANCE.getRegisters()));

        return;
    }

    // region Server request handlers

    @Override
    protected boolean onCorePlayerAttributesChanged() throws JSONException
    {
        l.debug("Player {}'s core attributes have changed. Updating.", this.dsrp.getPlayerID());
        EGameState.addRemotePlayer(this.dsrp);
        return true;
    }

    @Override
    protected boolean onChatMsg()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onLobbyPlayerStatus()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onSelectMapRequest() throws IllegalStateException
    {
        throw new IllegalStateException("AgentSL_v2.onSelectMapRequest() was called. The server should never ask an agent to select a map.");
    }

    @Override
    protected boolean onMapSelected() throws RuntimeException, JSONException
    {
        if (this.dsrp.getCourseName() == null || this.dsrp.getCourseName().isEmpty())
        {
            return true;
        }

        l.info("Current session course updated to {}. Sending name and robot to server.", this.dsrp.getCourseName());
        EGameState.INSTANCE.setCurrentServerCourse(this.dsrp.getCourseName());
        new PlayerValuesModel(EClientInformation.INSTANCE.getPrefAgentName(), EClientInformation.INSTANCE.getPrefAgentRobot()).send();

        if (Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady())
        {
            return true;
        }

        l.info("Sending Agent ready request to server.");
        new SetStatusModel(true).send();

        return true;
    }

    @Override
    protected boolean onGameStart() throws JSONException
    {
        l.debug("Game start received.");
        EEnvironment.INSTANCE.setCourse(this.dsrp.request());
        return true;
    }

    @Override
    protected boolean onPhaseChange() throws JSONException
    {
        l.debug("Game phase has changed to: {}", EGamePhase.fromInt(this.dsrp.getPhase()));
        EGameState.INSTANCE.setCurrentPhase(EGamePhase.fromInt(this.dsrp.getPhase()));
        return true;
    }

    @Override
    protected boolean onPlayerTurnChange() throws JSONException
    {
        EGameState.INSTANCE.setCurrentPlayer(this.dsrp.getPlayerID(), true);

        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.REGISTRATION)
        {
            if (this.dsrp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
            {
                final RCoordinate location = EEnvironment.INSTANCE.getNextFreeStartPoint();
                if (location == null)
                {
                    l.fatal("Failed to get free starting point.");
                    GameInstance.kill();
                    return false;
                }

                l.debug("Sending starting point: {}.", location.toString());
                new SetStartingPointModel(location.x(), location.y()).send();

                return true;
            }

            return true;
        }

        return true;
    }

    @Override
    protected boolean onErrorMsg() throws JSONException
    {
        l.fatal("Server sent an error message. Message: {}", this.dsrp.getErrorMessage());
        GameInstance.kill();
        return false;
    }

    @Override
    protected boolean onCardPlayed() throws JSONException
    {
        /* We still need to figure out what this does. */
        l.fatal("Server triggered onCardPlayedEvent. {}.", this.dsrp.request().toString(0));
        GameInstance.kill(GameInstance.EXIT_FATAL);
        return false;
    }

    @Override
    protected boolean onStartingPointTaken() throws JSONException
    {
        l.debug("Player {} took starting point {}.", this.dsrp.getPlayerID(), this.dsrp.getCoordinate().toString());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setStartingPosition(this.dsrp.getCoordinate());
        return true;
    }

    @Override
    protected boolean onRobotRotationUpdate() throws JSONException
    {
        l.debug("Player {} has rotated {}.", this.dsrp.getPlayerID(), this.dsrp.getRotation());
        ( (AgentRemotePlayerData) Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID()))).addRotation(this.dsrp.getRotation());
        return true;
    }

    @Override
    protected boolean onRegisterSlotUpdate()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onPlayerFinishedProgramming()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onForcedFinishProgramming() throws JSONException
    {
        l.fatal("Server forced agent {} to finish programming.", this.dsrp.getPlayerID());
        GameInstance.kill(GameInstance.EXIT_FATAL);
        return false;
    }

    @Override
    protected boolean onPlayerProgrammingCardsReceived()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onProgrammingDeckShuffled()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onProgrammingTimerStart()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onProgrammingTimerEnd()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onProgrammingCardsReceived() throws JSONException
    {
        l.debug("Received nine new programming cards from server: {}", String.join(", ", Arrays.asList(this.dsrp.getCardsInHand())));

        EGameState.INSTANCE.clearAllRegisters();
        for (final String c : this.dsrp.getCardsInHand())
        {
            EGameState.INSTANCE.addGotRegister(c);
            continue;
        }

        this.evaluateProgrammingPhase();

        return true;
    }

    @Override
    protected boolean onCurrentRegisterCards()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onCurrentRegisterCardReplacement()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onAnimationPlay()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onCheckpointReached() throws JSONException
    {
        l.debug("Player {} reached checkpoint {}.", this.dsrp.getPlayerID(), this.dsrp.getCheckpointNumber());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setCheckPointsReached(this.dsrp.getCheckpointNumber());
        return true;
    }

    @Override
    protected boolean onEnergyTokenChanged() throws JSONException
    {
        l.debug("Player {}'s energy amount has been updated to {}.", this.dsrp.getPlayerID(), this.dsrp.getEnergyCount());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setEnergy(this.dsrp.getEnergyCount());
        return true;
    }

    @Override
    protected boolean onGameEnd() throws JSONException
    {
        l.info("Game ended. Killing the game instance. Winner: {}.", this.dsrp.getWinningPlayer());
        GameInstance.kill();
        return true;
    }

    @Override
    protected boolean onPlayerPositionUpdate() throws JSONException
    {
        l.debug("Player {} has moved to {}.", this.dsrp.getPlayerID(), this.dsrp.getCoordinate().toString());
        ( (AgentRemotePlayerData) Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID()))).setLocation(this.dsrp.getCoordinate());
        return true;
    }

    @Override
    protected boolean onPlayerReboot() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onClientConnectionUpdate() throws JSONException
    {
        l.debug("Client {}'s net connection status was updated. Client is connected: {}; Taking action: {}.", this.dsrp.getPlayerID(), this.dsrp.getIsConnected(), this.dsrp.getNetAction().toString());
        if (Objects.requireNonNull(this.dsrp.getNetAction()) == EConnectionLoss.REMOVE)
        {
            EGameState.INSTANCE.removeRemotePlayer(this.dsrp.getPlayerID());
            return true;
        }

        l.error("Received net action {}, but the agent could not understand it. Ignoring.", this.dsrp.getNetAction().toString());
        return false;
    }

    @Override
    protected boolean onPickDamageType() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onDrawDamage() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onExchangeShop() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onRefillShop() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onUpgradeBought() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onCheckpointMoved() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onRegisterChosen() throws JSONException
    {
        return false;
    }

    // endregion Server request handlers

}
