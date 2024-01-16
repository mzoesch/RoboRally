package sep.view.clientcontroller;

import sep.view.json.lobby.         PlayerValuesModel;
import sep.view.json.lobby.         SetStatusModel;
import sep.view.json.game.          SetStartingPointModel;
import sep.view.lib.                EGamePhase;
import sep.view.lib.                RCoordinate;

import org.json.                    JSONException;
import org.json.                    JSONObject;
import org.json.                    JSONArray;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     BufferedReader;
import java.util.                   Objects;

class TileModifier
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

class Tile
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

    public abstract     RCoordinate getNextFreeStartPoint();
}

enum ECourse implements ICourse
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

    private static final Logger l = LogManager.getLogger(ECourse.class);

    private ECourse()
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
    protected boolean onMapSelected() throws RuntimeException
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
        ECourse.INSTANCE.setCourse(this.dsrp.request());
        return true;
    }

    @Override
    protected boolean onPhaseChange() throws JSONException
    {
        l.debug("Game phase has changed. New phase: {}", EGamePhase.fromInt(this.dsrp.getPhase()));
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
                final RCoordinate location = ECourse.INSTANCE.getNextFreeStartPoint();
                if (location == null)
                {
                    l.fatal("Failed to get free starting point.");
                    GameInstance.kill();
                    return false;
                }

                /* We do this because else the server will get into a lot of race conditions. Pretty fishy. But there is no time to fix this.*/
                try
                {
                    Thread.sleep(200);
                }
                catch (final InterruptedException e)
                {
                    throw new RuntimeException(e);
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
        l.error("Server sent an error message. Message: {}", this.dsrp.getErrorMessage());
        return false;
    }

    @Override
    protected boolean onCardPlayed() throws JSONException
    {
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
        return false;
    }

    @Override
    protected boolean onRegisterSlotUpdate() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onPlayerFinishedProgramming() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onForcedFinishProgramming() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onPlayerProgrammingCardsReceived() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onProgrammingDeckShuffled() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onProgrammingTimerStart() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onProgrammingTimerEnd() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onProgrammingCardsReceived() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onCurrentRegisterCards() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onCurrentRegisterCardReplacement() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onAnimationPlay() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onCheckpointReached() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onEnergyTokenChanged() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onGameEnd() throws JSONException
    {
        l.info("Game ended. Killing the game instance.");
        GameInstance.kill();
        return true;
    }

    @Override
    protected boolean onPlayerPositionUpdate() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onPlayerReboot() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onClientConnectionUpdate() throws JSONException
    {
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
    protected boolean onDiscardSome() throws JSONException
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
