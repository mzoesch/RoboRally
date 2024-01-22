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
import java.util.                   ArrayList;
import java.util.concurrent.atomic. AtomicBoolean;

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
    public abstract     void            setCourse(final JSONObject course);
    public abstract     int             getFiles();
    public abstract     int             getRanks();
    public abstract     Tile[][]        getTiles();
    public abstract     RCoordinate     getNextFreeStartPoint();
}

enum EEnvironment implements ICourse
{
    INSTANCE;

    private final record RGoalMask(int count, RCoordinate location)
    {
    }

    private final record RQualityStateAction(EAction action, float quality)
    {
    }

    enum EAction
    {
        NORTH_SINGLE,
        NORTH_DOUBLE,
        NORTH_TRIPLE,
        EAST_SINGLE,
        EAST_DOUBLE,
        EAST_TRIPLE,
        SOUTH_SINGLE,
        SOUTH_DOUBLE,
        SOUTH_TRIPLE,
        WEST_SINGLE,
        WEST_DOUBLE,
        WEST_TRIPLE,
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

        // region Getters and Setters

        public JSONObject getCourse()
        {
            return this.course;
        }

        @Override
        public void setCourse(final JSONObject course)
        {
            this.course = course;
            return;
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
        public RCoordinate getNextFreeStartPoint() throws JSONException
        {
            final Tile[][] tiles = this.getTiles();

            for (int file = 0; file < this.getFiles(); ++file)
            {
                for (int rank = 0; rank < this.getRanks(); ++rank)
                {
                    if (!tiles[file][rank].isStartPoint())
                    {
                        continue;
                    }

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

            l.fatal("Failed to find a free starting points.");
            GameInstance.kill();

            return null;
        }

        // endregion Getters and Setters

    }

    private static final Logger l = LogManager.getLogger(EEnvironment.class);

    private static final int    MAX_REACHABLE_DISTANCE          = 3;

    private static final float  IMPOSSIBLE_TRANSITION_PENALTY   = -10_000.0f;
    private static final float  MATCHING_TILE_PENALTY           = -10_000.0f;
    private static final float  EFFECTS_PENALTY                 = -50.0f;
    private static final float  EMPTY_TILE_PENALTY              = -1.0f;
    private static final float  GOAL_REWARD                     = 1_000.0f;

    public static final int     EPISODES                        = 5_000;
    public static final int     CALCULATE_AVERAGE_ACTIONS       = 100;
    private static final int    MAX_EPISODE_ACTIONS             = 2_000;

    /**
     * Hyperparameter for the learning rate. High values will yield a fast learning process but also an increased
     * risk of instability and wrong decisions.
     */
    private static final float  ALPHA                           = 0.3f;
    /**
     * Eagerness threshold. AKA Exploration and Exploitation Trade-Off parameter. High values will yield in an eminent
     * exploitation of the learned knowledge. Low values will let the agent forget their life goals and explore the
     * environment.
     */
    private static final float  EPSILON                         = 0.7f;
    /**
     * Discount factor. High values will yield in a long-term thinking agent while low values will yield in a
     * myopic agent. It reflects the agent's preference for immediate rewards over delayed ones.
     */
    private static final float  GAMMA                           = 0.9f;

    // region Cached members

    /** This variable has to be manually set to nullptr if the goal location changed or a new goal has to be hunted. */
    private RGoalMask       cachedGoal;
    private Tile[][]        cachedTiles;

    // endregion Cached members

    public final Object             lock                        = new Object();

    private final AtomicBoolean     finishedQualityLearning;

    /** Each matrix cell represents the travel from one state to another. */
    private float[][]               rewards;
    private float[][][]             qualities;

    private EEnvironment()
    {
        this.cachedGoal                 = null;
        this.cachedTiles                = null;

        this.finishedQualityLearning    = new AtomicBoolean(false);

        this.rewards                    = null;
        this.qualities                  = null;

        return;
    }

    // region Quality learning

    // region Helper methods

    /** If a pawn cannot traverse from one state to another (e.g., there is no wall or pit between them). */
    private boolean isBlocked(final Tile origin, final Tile target)
    {
        final int distanceX     = Math.abs(origin.getCoordinate().x() - target.getCoordinate().x());
        final int distanceY     = Math.abs(origin.getCoordinate().y() - target.getCoordinate().y());
        final int distance      = distanceX + distanceY;

        if (origin.getLocation().x() != target.getLocation().x() && origin.getLocation().y() != target.getLocation().y())
        {
            return true;
        }

        if (distance > 1)
        {
            final boolean areHorizontalAligned = origin.getLocation().y() == target.getLocation().y();

            final Tile newOrigin = this.getTile(
                new RCoordinate(
                    areHorizontalAligned
                    ? origin.getLocation().x() > target.getLocation().x()
                        ? origin.getLocation().x() - 1
                        : origin.getLocation().x() + 1
                    : origin.getLocation().x()
                    ,
                    areHorizontalAligned
                    ? origin.getLocation().y()
                    : origin.getLocation().y() > target.getLocation().y()
                        ? origin.getLocation().y() - 1
                        : origin.getLocation().y() + 1
                )
            );

            assert newOrigin != null;

            if (this.isBlocked(newOrigin, target))
            {
                return true;
            }
        }

        if (origin.hasWall() || target.hasWall())
        {
            if ((origin.getCoordinate().y() < target.getCoordinate().y()) && (Objects.equals(target.getWallOrientation(), "top")))
            {
                return true;
            }

            if ((origin.getCoordinate().x() > target.getCoordinate().x()) && (Objects.equals(target.getWallOrientation(), "right")))
            {
                return true;
            }

            if ((origin.getCoordinate().y() > target.getCoordinate().y()) && (Objects.equals(target.getWallOrientation(), "bottom")))
            {
                return true;
            }

            if ((origin.getCoordinate().x() < target.getCoordinate().x()) && (Objects.equals(target.getWallOrientation(), "left")))
            {
                return true;
            }

            if ((origin.getCoordinate().y() < target.getCoordinate().y()) && (Objects.equals(origin.getWallOrientation(), "bottom")))
            {
                return true;
            }

            if ((origin.getCoordinate().x() > target.getCoordinate().x()) && (Objects.equals(origin.getWallOrientation(), "left")))
            {
                return true;
            }

            if ((origin.getCoordinate().y() > target.getCoordinate().y()) && (Objects.equals(origin.getWallOrientation(), "top")))
            {
                return true;
            }

            if ((origin.getCoordinate().x() < target.getCoordinate().x()) && (Objects.equals(origin.getWallOrientation(), "right")))
            {
                return true;
            }
        }

        if (origin.isPit() || target.isPit()) /* can we remove target? */
        {
            return true;
        }

        return false;
    }

    private float calculateReward(final Tile current, final Tile target)
    {
        final int distanceX     = Math.abs(current.getCoordinate().x() - target.getCoordinate().x());
        final int distanceY     = Math.abs(current.getCoordinate().y() - target.getCoordinate().y());
        final int distance      = distanceX + distanceY;

        if (distance > EEnvironment.MAX_REACHABLE_DISTANCE)
        {
            return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
        }

        if (Objects.equals(current, target))
        {
            return EEnvironment.MATCHING_TILE_PENALTY;
        }

        if (this.isBlocked(current, target))
        {
            return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
        }

        if (target.isConveyorBelt())
        {
            // TODO     We should change this as sometimes there is no other
            //          possibility than crossing a belt (e.g. Dizzy Highway).
            return EEnvironment.EFFECTS_PENALTY;
        }

        if (target.isLaser())
        {
            return EEnvironment.EFFECTS_PENALTY;
        }

        if (target.isPit())
        {
            return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
        }

        if (target.isAntenna())
        {
            return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
        }

        if (target.isCheckpoint())
        {
            /* TODO Check if it is the right checkpoint. */
            return EEnvironment.GOAL_REWARD;
        }

        return EEnvironment.EMPTY_TILE_PENALTY;
    }

    /** @param iteration Only used for pseudorandom seed generation. */
    public RCoordinate getPseudorandomQualityStart(final int iteration)
    {
        final Tile t = this.getTile(new RCoordinate((int) (this.getTrulyRandomDouble(iteration) * this.getFiles()), (int) (this.getTrulyRandomDouble(iteration) * this.getRanks())));

        assert t != null;

        if (t.isTerminalState())
        {
            return this.getPseudorandomQualityStart(iteration);
        }

        return t.getLocation();
    }

    public RCoordinate getQualityGoal()
    {
        /* TODO Here we have to check if we collected another checkpoint and now our target goal has changed. */

        if (this.cachedGoal != null)
        {
            return this.cachedGoal.location;
        }

        for (int file = 0; file < this.getFiles(); ++file)
        {
            for (int rank = 0; rank < this.getRanks(); ++rank)
            {
                final Tile t = this.getTile(new RCoordinate(file, rank));

                assert t != null;

                if (t.isCheckpoint())
                {
                    this.cachedGoal = new RGoalMask(0, t.getLocation());
                    return this.getQualityGoal();
                }

                continue;
            }

            continue;
        }

        l.fatal("Failed to find a checkpoint.");
        GameInstance.kill();

        return null;
    }

    /** @see EEnvironment#EPSILON */
    private EEnvironment.EAction getNextAction(final RCoordinate location, final float epsilon)
    {
        if (epsilon < 0.0f || epsilon > 1.0f)
        {
            l.fatal("Epsilon threshold must be between 0.0 and 1.0. Current value: {}.", epsilon);
            GameInstance.kill();
            return null;
        }

        final double randomness = this.getTrulyRandomDouble();

        if (randomness <= epsilon)
        {
            int maxIdx = 0;
            for (int i = 1; i < this.qualities[location.x()][location.y()].length; ++i)
            {
                if (this.qualities[location.x()][location.y()][i] > this.qualities[location.x()][location.y()][maxIdx])
                {
                    maxIdx = i;
                }

                continue;
            }

            return EEnvironment.EAction.fromInt(maxIdx);
        }

        return EEnvironment.EAction.fromInt( (int) (randomness * EEnvironment.EAction.NUM.ordinal()) );
    }

    private EEnvironment.EAction getNextAction(final RCoordinate location)
    {
        return this.getNextAction(location, EEnvironment.EPSILON);
    }

    private static RCoordinate getNextState(final RCoordinate location, final EEnvironment.EAction action)
    {
        switch (action)
        {
        case NORTH_SINGLE:
        {
            if (location.y() - 1 < 0)
            {
                return location;
            }

            return new RCoordinate(location.x(), location.y() - 1);
        }

        case NORTH_DOUBLE:
        {
            if (location.y() - 2 < 0)
            {
                return new RCoordinate(location.x(), 0);
            }

            return new RCoordinate(location.x(), location.y() - 2);
        }

        case NORTH_TRIPLE:
        {
            if (location.y() - 3 < 0)
            {
                return new RCoordinate(location.x(), 0);
            }

            return new RCoordinate(location.x(), location.y() - 3);
        }

        case EAST_SINGLE:
        {
            if (location.x() + 1 >= EEnvironment.INSTANCE.getFiles())
            {
                return location;
            }

            return new RCoordinate(location.x() + 1, location.y());
        }

        case EAST_DOUBLE:
        {
            if (location.x() + 2 >= EEnvironment.INSTANCE.getFiles())
            {
                return new RCoordinate(EEnvironment.INSTANCE.getFiles() - 1, location.y());
            }

            return new RCoordinate(location.x() + 2, location.y());
        }

        case EAST_TRIPLE:
        {
            if (location.x() + 3 >= EEnvironment.INSTANCE.getFiles())
            {
                return new RCoordinate(EEnvironment.INSTANCE.getFiles() - 1, location.y());
            }

            return new RCoordinate(location.x() + 3, location.y());
        }

        case SOUTH_SINGLE:
        {
            if (location.y() + 1 >= EEnvironment.INSTANCE.getRanks())
            {
                return location;
            }

            return new RCoordinate(location.x(), location.y() + 1);
        }

        case SOUTH_DOUBLE:
        {
            if (location.y() + 2 >= EEnvironment.INSTANCE.getRanks())
            {
                return new RCoordinate(location.x(), EEnvironment.INSTANCE.getRanks() - 1);
            }

            return new RCoordinate(location.x(), location.y() + 2);
        }

        case SOUTH_TRIPLE:
        {
            if (location.y() + 3 >= EEnvironment.INSTANCE.getRanks())
            {
                return new RCoordinate(location.x(), EEnvironment.INSTANCE.getRanks() - 1);
            }

            return new RCoordinate(location.x(), location.y() + 3);
        }

        case WEST_SINGLE:
        {
            if (location.x() - 1 < 0)
            {
                return location;
            }

            return new RCoordinate(location.x() - 1, location.y());
        }

        case WEST_DOUBLE:
        {
            if (location.x() - 2 < 0)
            {
                return new RCoordinate(0, location.y());
            }

            return new RCoordinate(location.x() - 2, location.y());
        }

        case WEST_TRIPLE:
        {
            if (location.x() - 3 < 0)
            {
                return new RCoordinate(0, location.y());
            }

            return new RCoordinate(location.x() - 3, location.y());
        }

        default:
        {

            l.fatal("Failed to get next state. Action {} is not supported.", action.toString());
            GameInstance.kill();
            return null;
        }
        }
    }

    /** @return The amount of iterations the agent required to get to any terminal state. */
    public int evaluateAnEpisode(final RCoordinate start)
    {
        int             actions     = 0;
        RCoordinate     cursor      = start;

        while (true)
        {
            if (actions >= EEnvironment.MAX_EPISODE_ACTIONS)
            {
                break;
            }

            if (Objects.requireNonNull(this.getTile(cursor)).isTerminalState())
            {
                break;
            }

            final EEnvironment.EAction  action      = this.getNextAction(cursor);
            final RCoordinate           next        = EEnvironment.getNextState(cursor, action);

            assert next != null;

            final float reward              = this.rewards[cursor.x() + cursor.y() * this.getFiles()][next.x() + next.y() * this.getFiles()];
            final float deprecatedQuality   = this.qualities[cursor.x()][cursor.y()][action.ordinal()];
            final float temporalDifference  = reward + (EEnvironment.GAMMA * this.qualities[next.x()][next.y()][Objects.requireNonNull(this.getNextAction(next, 1.0f)).ordinal()]) - deprecatedQuality;
            final float updatedQuality      = deprecatedQuality + (EEnvironment.ALPHA * temporalDifference);

            this.qualities[cursor.x()][cursor.y()][action.ordinal()] = updatedQuality;

            ++actions;
            cursor = next;

            continue;
        }

        if (actions >= EEnvironment.MAX_EPISODE_ACTIONS)
        {
            l.warn("Agent {} exceeded the maximum amount of actions per episode and did not reach a terminal state.", EClientInformation.INSTANCE.getPlayerID());
        }

        return actions;
    }

    private ArrayList<RQualityStateAction> getQualityActionsInDescendingOrder(final RCoordinate state)
    {
        final ArrayList<RQualityStateAction> actions = new ArrayList<RQualityStateAction>();

        for (int i = 0; i < EEnvironment.EAction.NUM.ordinal(); ++i)
        {
            actions.add(new RQualityStateAction(EEnvironment.EAction.fromInt(i), this.qualities[state.x()][state.y()][i]));
            continue;
        }

        actions.sort((a, b) -> Float.compare(b.quality, a.quality));

        return actions;

    }

    // endregion Helper methods

    public void initRewardMatrix()
    {
        this.rewards = new float[this.getTileCount()][this.getTileCount()];

        for (int currentState = 0; currentState < this.getTileCount(); ++currentState)
        {
            for (int targetState = 0; targetState < this.getTileCount(); ++targetState)
            {
                final int currentFile = RCoordinate.fromIndex(currentState, this.getFiles()).x();
                final int currentRank = RCoordinate.fromIndex(currentState, this.getFiles()).y();

                final int targetFile = RCoordinate.fromIndex(targetState, this.getFiles()).x();
                final int targetRank = RCoordinate.fromIndex(targetState, this.getFiles()).y();

                this.rewards[currentState][targetState] = this.calculateReward(Objects.requireNonNull(this.getTiles())[currentFile][currentRank], Objects.requireNonNull(this.getTiles())[targetFile][targetRank]);

                continue;
            }

            continue;
        }

        return;
    }

    public void initQualityMatrix()
    {
        this.qualities = new float[EEnvironment.INSTANCE.getFiles()][EEnvironment.INSTANCE.getRanks()][EEnvironment.EAction.NUM.ordinal()];
        Arrays.stream(this.qualities).forEach(files -> Arrays.stream(files).forEach(ranks -> Arrays.fill(ranks, 0)));
        return;
    }

    public void setRegisterCardsBasedOnExploredKnowledge()
    {
        final AgentRemotePlayerData agent   = (AgentRemotePlayerData) Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer());
        RCoordinate predictedState          = agent.getLocation();

        int oldIterationI = -1;

        for (int i = 0; i < 5; ++i)
        {
            if (oldIterationI != -1)
            {
                for (int j = oldIterationI; j < i; ++j)
                {
                    l.info("Agent {} has chosen for register {}: {}.", EClientInformation.INSTANCE.getPlayerID(), j, EGameState.INSTANCE.getRegister(j));
                    continue;
                }
            }

            oldIterationI = i;

            final ArrayList<RQualityStateAction> actions = this.getQualityActionsInDescendingOrder(predictedState);

            assert predictedState != null;

            l.debug("Agent {} has the following actions in descending order of quality for state {}: {}.", EClientInformation.INSTANCE.getPlayerID(), predictedState.toString(), actions);

            nextActionLoop: while (true)
            {
                if (actions.isEmpty())
                {
                    /* TODO We might want to implement a search tree here instead of playing random cards. */
                    l.warn("Agent {} has no more actions to explore. Selecting {} random elements.", EClientInformation.INSTANCE.getPlayerID(), 5 - i);

                    for (int j = i; j < 5; ++j)
                    {
                        int gotRegister = 0;
                        for (int k = 0; k < EGameState.INSTANCE.getGotRegisters().size(); ++k)
                        {
                            if (EGameState.INSTANCE.getGotRegister(k) == null)
                            {
                                continue ;
                            }

                            gotRegister = k;
                            break ;
                        }

                        EGameState.INSTANCE.setRegister(j, gotRegister);
                        l.warn("Agent {} has chosen randomly for register {}: {}.", EClientInformation.INSTANCE.getPlayerID(), j, EGameState.INSTANCE.getRegister(j));

                        continue;
                    }

                    return;
                }

                final RQualityStateAction action = actions.remove(0);

                switch (action.action)
                {
                case NORTH_SINGLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveI"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedNorth())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveI towards north. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 90 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 270 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 180 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case NORTH_DOUBLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveII"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedNorth())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveII towards north. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 90 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 270 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 180 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case NORTH_TRIPLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveIII"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedNorth())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveIII towards north. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 90 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 270 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 180 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case EAST_SINGLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveI"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedEast())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveI towards east. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 0 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 180 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 270 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case EAST_DOUBLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveII"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedEast())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveII towards east. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 0 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 180 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 270 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case EAST_TRIPLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveIII"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedEast())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveIII towards east. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 0 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 180 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 270 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case SOUTH_SINGLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveI"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedSouth())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveI towards south. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 90 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 270 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 0 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case SOUTH_DOUBLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveII"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedSouth())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveII towards south. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 90 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 270 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 0 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case SOUTH_TRIPLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveIII"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedSouth())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveIII towards south. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 90 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 270 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 0 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case WEST_SINGLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveI"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedWest())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveI towards west. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 0 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 180 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 90 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveI"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case WEST_DOUBLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveII"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedWest())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveII towards west. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 0 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 180 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 90 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                case WEST_TRIPLE:
                {
                    if (!EGameState.INSTANCE.getGotRegisters().contains("MoveIII"))
                    {
                        continue nextActionLoop;
                    }

                    if (agent.isRotatedWest())
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    l.debug("Agent {} has chosen MoveIII towards west. But he is rotated {} deg. Trying to update rotation.", EClientInformation.INSTANCE.getPlayerID(), agent.getRotation());

                    if (agent.getRotation() == 0 && EGameState.INSTANCE.getGotRegisters().contains("TurnLeft"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnLeft"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 180 && EGameState.INSTANCE.getGotRegisters().contains("TurnRight"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("TurnRight"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    if (agent.getRotation() == 90 && EGameState.INSTANCE.getGotRegisters().contains("UTurn"))
                    {
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("UTurn"));
                        ++i;
                        EGameState.INSTANCE.setRegister(i, EGameState.INSTANCE.getGotRegisters().indexOf("MoveIII"));
                        predictedState = EEnvironment.getNextState(predictedState, action.action);
                        break nextActionLoop;
                    }

                    continue nextActionLoop;
                }

                default:
                {
                    l.fatal("Agent could not understand action: {}.", action.action.toString());
                    GameInstance.kill(GameInstance.EXIT_FATAL);
                    return;
                }
                }
            }

            continue;
        }

        if (oldIterationI != -1)
        {
            for (int j = oldIterationI; j < 5; ++j)
            {
                l.info("Agent {} has chosen for register {}: {}.", EClientInformation.INSTANCE.getPlayerID(), j, EGameState.INSTANCE.getRegister(j));
                continue;
            }
        }

        return;
    }

    // endregion Quality learning

    // region Getters and Setters

    public double getTrulyRandomDouble(final int iteration)
    {
        return new Random(( (long) iteration << 8 ) + System.nanoTime()).nextDouble();
    }

    public double getTrulyRandomDouble()
    {
        return this.getTrulyRandomDouble( (int) System.currentTimeMillis() << 4 );
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
    public void setCourse(final JSONObject course)
    {
        ECourseImpl.INSTANCE.setCourse(course);
        return;
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


    public Tile getTile(final RCoordinate location) throws JSONException
    {
        if (this.isCourseMissing())
        {
            return null;
        }

        return ECourseImpl.INSTANCE.getTiles()[location.x()][location.y()];
    }

    @Override
    public Tile[][] getTiles() throws JSONException
    {
        if (this.isCourseMissing())
        {
            return null;
        }

        if (this.cachedTiles != null)
        {
            return this.cachedTiles;
        }

        this.cachedTiles = ECourseImpl.INSTANCE.getTiles();

        return this.getTiles();
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

    private int getTileCount()
    {
        return this.getFiles() * this.getRanks();
    }

    public float[][] getRewards()
    {
        return this.rewards;
    }

    private String getRewardStateAsString(final int originalState)
    {
        final RCoordinate       state   = RCoordinate.fromIndex(originalState, this.getFiles());
        final StringBuilder     sb      = new StringBuilder();

        sb.append(String.format("%d. Reward State: %s -> [", originalState, state.toString()));

        for (int targetState = 0; targetState < this.getFiles() * this.getRanks(); ++targetState)
        {
            if (this.rewards[originalState][targetState] == EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY)
            {
                continue;
            }

            final String        direction;
            final RCoordinate   actionState = RCoordinate.fromIndex(targetState, this.getFiles());

            if (state.x() == actionState.x() && state.y() == actionState.y() - 1)
            {
                direction = "SS";
            }
            else if (state.x() == actionState.x() && state.y() == actionState.y() - 2)
            {
                direction = "SD";
            }
            else if (state.x() == actionState.x() && state.y() == actionState.y() - 3)
            {
                direction = "ST";
            }
            else if (state.x() == actionState.x() && state.y() == actionState.y() + 1)
            {
                direction = "NS";
            }
            else if (state.x() == actionState.x() && state.y() == actionState.y() + 2)
            {
                direction = "ND";
            }
            else if (state.x() == actionState.x() && state.y() == actionState.y() + 3)
            {
                direction = "NT";
            }
            else if (state.x() == actionState.x() - 1 && state.y() == actionState.y())
            {
                direction = "ES";
            }
            else if (state.x() == actionState.x() - 2 && state.y() == actionState.y())
            {
                direction = "ED";
            }
            else if (state.x() == actionState.x() - 3 && state.y() == actionState.y())
            {
                direction = "ET";
            }
            else if (state.x() == actionState.x() + 1 && state.y() == actionState.y())
            {
                direction = "WS";
            }
            else if (state.x() == actionState.x() + 2 && state.y() == actionState.y())
            {
                direction = "WD";
            }
            else if (state.x() == actionState.x() + 3 && state.y() == actionState.y())
            {
                direction = "WT";
            }
            else
            {
                direction = "XXXXX";
            }

            final String s = String.format(Locale.US, "{%s, %.2f},", direction, this.rewards[originalState][targetState]);

            sb.append(s);

            continue;
        }

        sb.append("]");

        return sb.toString();
    }

    private String getQualityStateAsString(final RCoordinate state)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d. Quality State: %s -> [", state.toIndex(this.getFiles()), state.toString()));

        for (int i = 0; i < EEnvironment.EAction.NUM.ordinal(); ++i)
        {
            sb.append(String.format(Locale.US, "{%s, %.2f}, ", EEnvironment.EAction.fromInt(i).toString(), this.qualities[state.x()][state.y()][i]));
            continue;
        }

        sb.append("]");

        return sb.toString();
    }

    public void outputRewards()
    {
        l.debug("Rewards (all impossible transitions are hidden):");

        for (int i = 0; i < this.getRewards().length; ++i)
        {
            l.debug(this.getRewardStateAsString(i));
            describeConstable();
        }

        return;
    }

    public void outputQualities()
    {
        l.debug("Qualities:");

        for (int file = 0; file < this.qualities.length; ++file)
        {
            for (int rank = 0; rank < this.qualities[file].length; ++rank)
            {
                l.debug(this.getQualityStateAsString(new RCoordinate(file, rank)));
                continue;
            }

            continue;
        }

        return;
    }

    /** If a checkpoint has moved, for example.  */
    public void onCourseChanged()
    {
        this.cachedTiles = null;

        this.finishedQualityLearning.set(false);

        this.rewards     = null;
        this.qualities   = null;

        return;
    }

    public boolean hasToRecalculateQualities()
    {
        return this.rewards == null || this.qualities == null;
    }

    public boolean setHasFinishedQualityLearning(final boolean b)
    {
        return this.finishedQualityLearning.compareAndSet(!b, b);
    }

    public boolean hasFinishedQualityLearning()
    {
        return this.finishedQualityLearning.get();
    }

    // endregion Getters and Setters

}

/**
 * The master class of an agent. An object of this class must always run on the main thread.
 * {@inheritDoc}
 */
public final class AgentSL_v2 extends ServerListener
{
    private static final Logger l = LogManager.getLogger(AgentSL_v2.class);

    private Thread  qualityLearningService;
    private Thread  registerCardBroadcastService;

    public AgentSL_v2(final BufferedReader br)
    {
        super(br);

        this.qualityLearningService         = null;
        this.registerCardBroadcastService   = null;

        return;
    }

    private void sendSelectedCards()
    {
        for (int i = 0; i < EGameState.INSTANCE.getRegisters().length; ++i)
        {
            new SelectedCardModel(i, EGameState.INSTANCE.getRegister(i)).send();
            continue;
        }

        return;
    }

    private void evaluateProgrammingPhaseWithQLearning(final boolean bSetRegisterCards)
    {
        if (this.qualityLearningService == null || !this.qualityLearningService.isAlive())
        {
            if (EEnvironment.INSTANCE.hasFinishedQualityLearning())
            {
                if (!bSetRegisterCards)
                {
                    l.fatal("Quality learning has finished, but the agent is not allowed to set register cards.");
                    GameInstance.kill(GameInstance.EXIT_FATAL);
                    return;
                }

                EEnvironment.INSTANCE.setRegisterCardsBasedOnExploredKnowledge();
                this.sendSelectedCards();
                l.info("Agent {} evaluated for the current programming phase. The determined cards are: {}.", EClientInformation.INSTANCE.getPlayerID(), Arrays.toString(EGameState.INSTANCE.getRegisters()));

                return;
            }

            this.qualityLearningService = this.createQualityLearningService();
            this.qualityLearningService.setName("QualityLearningService");
            this.qualityLearningService.start();

            if (!bSetRegisterCards)
            {
                return;
            }

            this.registerCardBroadcastService            = this.createRegisterCardBroadcastService();
            assert this.registerCardBroadcastService    != null;
            this.registerCardBroadcastService.setName("RegisterCardBroadcastService");
            this.registerCardBroadcastService.start();

            return;
        }

        if (!bSetRegisterCards)
        {
            l.fatal("Already evaluating quality states.");
            GameInstance.kill(GameInstance.EXIT_FATAL);
            return;
        }

        this.registerCardBroadcastService            = this.createRegisterCardBroadcastService();
        assert this.registerCardBroadcastService    != null;
        this.registerCardBroadcastService.setName("RegisterCardBroadcastService");
        this.registerCardBroadcastService.start();

        return;
    }

    private synchronized void evaluateProgrammingPhaseWithRandom()
    {
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

        this.sendSelectedCards();

        l.info("Agent {} evaluated for the current programming phase. The determined cards are: {}.", EClientInformation.INSTANCE.getPlayerID(), Arrays.toString(EGameState.INSTANCE.getRegisters()));

        return;
    }

    private void evaluateProgrammingPhaseAsync(final boolean bSetRegisterCards)
    {
        final Thread eval = new Thread(() ->
        {
            if (EClientInformation.INSTANCE.getAgentDifficulty() == EAgentDifficulty.RANDOM)
            {
                if (!bSetRegisterCards)
                {
                    l.fatal("Agent is not allowed to set register cards, but the agent difficulty is set to RANDOM. There is no need to evaluate the programming phase.");
                    GameInstance.kill(GameInstance.EXIT_FATAL);
                    return;
                }

                this.evaluateProgrammingPhaseWithRandom();

                return;
            }

            if (EClientInformation.INSTANCE.getAgentDifficulty() == EAgentDifficulty.QLEARNING)
            {
                this.evaluateProgrammingPhaseWithQLearning(bSetRegisterCards);

                return;
            }

            return;
        });

        eval.start();

        if (EClientInformation.INSTANCE.isMockView())
        {
            try
            {
                eval.join();
            }
            catch (final InterruptedException e)
            {
                l.fatal("Failed to join evaluation thread.");
                GameInstance.kill(GameInstance.EXIT_FATAL);
                return;
            }
        }

        return;
    }

    public void onDevelopmentEvaluation()
    {
        EEnvironment.INSTANCE.setCourse(EGameState.INSTANCE.getAssumedServerCourseRawJSON());
        this.evaluateProgrammingPhaseAsync(true);
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

        this.evaluateProgrammingPhaseAsync(true);

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
