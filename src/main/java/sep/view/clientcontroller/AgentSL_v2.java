package sep.view.clientcontroller;

import sep.view.json.lobby.         PlayerValuesModel;
import sep.view.json.lobby.         SetStatusModel;
import sep.view.lib.                EGamePhase;
import sep.view.lib.                RCoordinate;
import sep.view.lib.                EAgentDifficulty;
import sep.view.json.game.          SetStartingPointModel;
import sep.view.json.game.          SelectedCardModel;

import org.json.                    JSONException;
import org.json.                    JSONObject;
import org.json.                    JSONArray;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     BufferedReader;
import java.util.                   Objects;
import java.util.                   Arrays;
import java.util.                   Random;
import java.util.                   Locale;

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

    public String getFirstWallOrientation()
    {
        return this.modifier.getJSONArray("orientations").getString(0);
    }

    public String getCheckpointCount()
    {
        return this.modifier.getString("count");
    }

}

final class Tile
{
    private final JSONArray     tile;
    private final RCoordinate   coordinate;

    public Tile(final JSONArray tile, final RCoordinate location)
    {
        this.tile           = tile;
        this.coordinate     = location;

        return;
    }

    public RCoordinate getCoordinate()
    {
        return coordinate;
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

    public boolean isAntenna() {
        for (int i = 0; i < this.tile.length(); ++i) {
            if (Objects.equals(this.getModifier(i).getType(), "Antenna")) {
                return true;
            }
        }
        return false;
    }

    public boolean isLaser() {
        for (int i = 0; i < this.tile.length(); ++i) {
            if (Objects.equals(this.getModifier(i).getType(), "Laser")) {
                return true;
            }
        }
        return false;
    }

    public boolean isPit() {
        for (int i = 0; i < this.tile.length(); ++i) {
            if (Objects.equals(this.getModifier(i).getType(), "Pit")) {
                return true;
            }
        }
        return false;
    }

    public boolean isConveyorBelt() {
        for (int i = 0; i < this.tile.length(); ++i) {
            if (Objects.equals(this.getModifier(i).getType(), "ConveyorBelt")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasWall() {
        for (int i = 0; i < this.tile.length(); ++i) {
            if (Objects.equals(this.getModifier(i).getType(), "Wall")) {
                return true;
            }
        }
        return false;
    }

    public String getWallOrientation() {
        if(this.hasWall()) {
            for (int i = 0; i < this.tile.length(); ++i) {
                if(this.getModifier(i).getFirstWallOrientation() != null) {
                    return this.getModifier(i).getFirstWallOrientation();
                }
            }
        }
        return null;
    }

    public boolean isCheckpoint() {
        for (int i = 0; i < this.tile.length(); ++i) {
            if (Objects.equals(this.getModifier(i).getType(), "CheckPoint")) {
                return true;
            }
        }
        return false;
    }

    public String getCheckpointNum() {
        if(this.hasWall()) {
            for (int i = 0; i < this.tile.length(); ++i) {
                if(this.getModifier(i).getCheckpointCount() != null) {
                    return this.getModifier(i).getCheckpointCount();
                }
            }
        }
        return null;
    }

    public JSONArray getJSON()
    {
        return this.tile;
    }

    public boolean isTerminalState()
    {
        /* TODO We have to check if it is the right checkpoint. */
        return this.isAntenna() || this.isPit() || this.isCheckpoint();
    }

    public RCoordinate getLocation()
    {
        return this.coordinate;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (!(obj instanceof final Tile otherTile))
        {
            return false;
        }

        return Objects.equals(this.coordinate, otherTile.coordinate);
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

    private final record RGoalMask(int count, RCoordinate location)
    {
    }

    enum EAction
    {
        NORTH,
        EAST,
        SOUTH,
        WEST,
        NUM;

        public static EAction fromInt(final int i)
        {
            return EAction.values()[i];
        }

    }

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

                            if (Objects.equals(EGameState.INSTANCE.getRemotePlayers()[i].getStartingPosition(), tiles[file][rank].getCoordinate()))
                            {
                                bFree = false;
                                break;
                            }

                            continue;
                        }

                        if (bFree)
                        {
                            return tiles[file][rank].getCoordinate();
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

    /** Learning rate. */
    private static final double     ALPHA   = 0.9;
    /* Discount factor.*/
    private static final double     GAMMA   = 0.9;
    /* The percentage of time when the best action should be used. Eagerness? */
    private static final double     EPSILON = 0.9;

    private static final float      INIT_Q  = 0.0f;
    private float[][][]             quantityMatrix = null;
    private float[][]               rewardMatrix = null;

    public AgentSL_v2(final BufferedReader br)
    {
        super(br);
        return;
    }

    /**
     * Each reward matrix cell represents the transition from one state (tile) to another.
     */
    private void setupRewardMatrix() {
        try {
            Tile[][] tiles = EEnvironment.INSTANCE.getTiles();
            int numTiles = EEnvironment.INSTANCE.getFiles() * EEnvironment.INSTANCE.getRanks();
            rewardMatrix = new float[numTiles][numTiles];

            for (int currentTile = 0; currentTile < numTiles; ++currentTile) {
                for (int targetTile = 0; targetTile < numTiles; ++targetTile) {
                    int currentFile = currentTile / EEnvironment.INSTANCE.getRanks();
                    int currentRank = currentTile % EEnvironment.INSTANCE.getRanks();

                    int targetFile = targetTile / EEnvironment.INSTANCE.getRanks();
                    int targetRank = targetTile % EEnvironment.INSTANCE.getRanks();

                    rewardMatrix[currentTile][targetTile] = calculateReward(tiles[currentFile][currentRank], tiles[targetFile][targetRank]);
                }
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param currentTile tile (state) the agent is sitting on
     * @param targetTile tile (state) the agent wants to transition
     * @return reward value based on different criteria
     */
    private float calculateReward(Tile currentTile, Tile targetTile) {
        int distanceX = Math.abs(currentTile.getCoordinate().x() - targetTile.getCoordinate().x());
        int distanceY = Math.abs(currentTile.getCoordinate().y() - targetTile.getCoordinate().y());
        int distance = distanceX + distanceY;

        if (distance > 1) {
            return -1;
        } else if (currentTile == targetTile) {
            return -1;
        } else if(targetTile.hasWall()) {
            if ((currentTile.getCoordinate().y() < targetTile.getCoordinate().y())
                    && (targetTile.getWallOrientation() == "top")) {
                return -1;
            } else if ((currentTile.getCoordinate().x() > targetTile.getCoordinate().x())
                    && (targetTile.getWallOrientation() == "right")) {
                return -1;
            } else if ((currentTile.getCoordinate().y() > targetTile.getCoordinate().y())
                    && (targetTile.getWallOrientation() == "bottom")) {
                return -1;
            } else if ((currentTile.getCoordinate().x() < targetTile.getCoordinate().x())
                    && (targetTile.getWallOrientation() == "left")) {
                return -1;
            }
        } else if(targetTile.isConveyorBelt()) {
            //TODO: we should change this as sometimes there is no other
            // possibility than crossing a belt (e.g. Dizzy Highway)
            return -10;
        } else if(targetTile.isLaser()) {
            return -10;
        } else if(targetTile.isPit()) {
            return -10;
        } else if(targetTile.isCheckpoint()) {
            //TODO
        }else if (targetTile.isAntenna()) {
            return -1;
        } else {
            return 0;
        }
        return 0;
    }

    private void evaluateProgrammingPhase()
    {
        //Q LEARNING:
        setupRewardMatrix();
        this.quantityMatrix = new float[EEnvironment.INSTANCE.getFiles()][EEnvironment.INSTANCE.getRanks()][4];
        Arrays.stream(this.quantityMatrix).forEach(files -> Arrays.stream(files).forEach(ranks -> Arrays.fill(ranks, AgentSL_v2.INIT_Q)));

        //RANDOM:
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

            EGameState.INSTANCE.setRegister(i, j);

            ++j;
            continue;
        }

        for (int i = 0; i < 5; ++i)
        {
            new SelectedCardModel(i, EGameState.INSTANCE.getRegister(i)).send();
            continue;
        }

        l.debug("Agent {} evaluated for the current programming phase. The determined cards are: {}.", EClientInformation.INSTANCE.getPlayerID(), Arrays.toString(EGameState.INSTANCE.getRegisters()));

        return;
    }

    public void onDevelopmentEvaluation()
    {
        EEnvironment.INSTANCE.setCourse(EGameState.INSTANCE.getAssumedServerCourseRawJSON());
        this.evaluateProgrammingPhase();
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

        new Thread(() ->
        {
            final int t = (int) (new Random(System.currentTimeMillis() + ProcessHandle.current().pid()).nextDouble() * 300) + 200;
            l.warn("Waiting {} ms before confirming agent name and figure.", t);
            try
            {
                Thread.sleep(t);
            }
            catch (final InterruptedException e)
            {
                l.fatal("Failed to wait before confirming agent name and figure.");
                GameInstance.kill(GameInstance.EXIT_FATAL);
                return;
            }

            new PlayerValuesModel(EClientInformation.INSTANCE.getPrefAgentName(), EClientInformation.INSTANCE.getPrefAgentFigure()).send();

            if (Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady())
            {
                return;
            }

            l.info("Sending Agent ready request to server.");
            new SetStatusModel(true).send();

            return;
        }).start();

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
