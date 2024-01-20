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

    private static final int    IMPOSSIBLE_TRANSITION_PENALTY   = -10_000;
    private static final int    MATCHING_TILE_PENALTY           = -10_000;
    /** TODO This is a temporal solution as it should be decreased later. */
    private static final int    EFFECTS_PENALTY                 = -1;
    private static final int    EMPTY_TILE_PENALTY              = -1;
    private static final int    GOAL_REWARD                     = 1_000;

    private static final int    EPISODES                        = 1_000;
    private static final int    CALCULATE_AVERAGE_ITERATIONS    = 100;
    private static final int    MAX_EPISODE_ITERATIONS          = 2_000;

    private static final float  EPSILON                         = 0.9f;
    private static final float  DISCOUNT_FACTOR                 = 0.9f;
    private static final float  LEARNING_RATE                   = 0.9f;

    // region Cached members

    /**
     * This variable has to be manually set to nullptr if the goal location changed or a new goal has to be hunted.
     */
    private RGoalMask       cachedGoal;
    private Tile[][]        cachedTiles;

    // endregion Cached members

    /**
     * Each matrix cell represents the travel from one state to another.
     */
    private float[][]       rewards;
    private float[][][]     qualities;

    private EEnvironment()
    {
        this.cachedGoal     = null;
        this.cachedTiles    = null;

        return;
    }

    // region Quality learning

    // region Helper methods

    private float calculateReward(final Tile current, final Tile target)
    {
        final int distanceX     = Math.abs(current.getCoordinate().x() - target.getCoordinate().x());
        final int distanceY     = Math.abs(current.getCoordinate().y() - target.getCoordinate().y());
        final int distance      = distanceX + distanceY;

        if (distance > 1)
        {
            return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
        }

        if (Objects.equals(current, target))
        {
            return EEnvironment.MATCHING_TILE_PENALTY;
        }

        if (target.hasWall() || current.hasWall())
        {
            if ((current.getCoordinate().y() < target.getCoordinate().y()) && (Objects.equals(target.getWallOrientation(), "top")))
            {
                return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
            }

            if ((current.getCoordinate().x() > target.getCoordinate().x()) && (Objects.equals(target.getWallOrientation(), "right")))
            {
                return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
            }

            if ((current.getCoordinate().y() > target.getCoordinate().y()) && (Objects.equals(target.getWallOrientation(), "bottom")))
            {
                return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
            }

            if ((current.getCoordinate().x() < target.getCoordinate().x()) && (Objects.equals(target.getWallOrientation(), "left")))
            {
                return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
            }

            if ((current.getCoordinate().y() < target.getCoordinate().y()) && (Objects.equals(current.getWallOrientation(), "bottom")))
            {
                return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
            }

            if ((current.getCoordinate().x() > target.getCoordinate().x()) && (Objects.equals(current.getWallOrientation(), "left")))
            {
                return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
            }

            if ((current.getCoordinate().y() > target.getCoordinate().y()) && (Objects.equals(current.getWallOrientation(), "top")))
            {
                return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
            }

            if ((current.getCoordinate().x() < target.getCoordinate().x()) && (Objects.equals(current.getWallOrientation(), "right")))
            {
                return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
            }
        }

        if (target.isConveyorBelt())
        {
            // TODO:    We should change this as sometimes there is no other
            //          possibility than crossing a belt (e.g. Dizzy Highway).
            return EEnvironment.EFFECTS_PENALTY;
        }

        if (target.isLaser())
        {
            return EEnvironment.EFFECTS_PENALTY;
        }

        if (target.isPit())
        {
            return EEnvironment.EFFECTS_PENALTY;
        }

        if (target.isCheckpoint())
        {
            /* TODO Check if it is the right checkpoint. */
            return EEnvironment.GOAL_REWARD;
        }

        if (target.isAntenna())
        {
            return EEnvironment.IMPOSSIBLE_TRANSITION_PENALTY;
        }

        return EEnvironment.EMPTY_TILE_PENALTY;
    }

    /**
     * @param iteration Only used for pseudorandom seed generation.
     */
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

    /**
     * @param epsilon The higher the epsilon threshold is, the more likely it is that the agent will choose the best
     *                action currently known for him. A threshold of one means that the agent will always choose the
     *                best action.
     */
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
        case NORTH:
        {
            if (location.y() - 1 < 0)
            {
                return location;
            }

            return new RCoordinate(location.x(), location.y() - 1);
        }

        case EAST:
        {
            if (location.x() + 1 >= EEnvironment.INSTANCE.getFiles())
            {
                return location;
            }

            return new RCoordinate(location.x() + 1, location.y());
        }

        case SOUTH:
        {
            if (location.y() + 1 >= EEnvironment.INSTANCE.getRanks())
            {
                return location;
            }

            return new RCoordinate(location.x(), location.y() + 1);
        }

        case WEST:
        {
            if (location.x() - 1 < 0)
            {
                return location;
            }

            return new RCoordinate(location.x() - 1, location.y());
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
    private int evaluateAnEpisode(final RCoordinate start)
    {
        int             iterations  = 0;
        RCoordinate     cursor      = start;

        while (true)
        {
            if (iterations >= EEnvironment.MAX_EPISODE_ITERATIONS)
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
            final float temporalDifference  = reward + (EEnvironment.DISCOUNT_FACTOR * this.qualities[next.x()][next.y()][Objects.requireNonNull(this.getNextAction(next, 1.0f)).ordinal()]) - deprecatedQuality;
            final float updatedQuality      = deprecatedQuality + (EEnvironment.LEARNING_RATE * temporalDifference);

            this.qualities[cursor.x()][cursor.y()][action.ordinal()] = updatedQuality;

            ++iterations;
            cursor = next;

            continue;
        }

        if (iterations >= EEnvironment.MAX_EPISODE_ITERATIONS)
        {
            l.warn("Agent {} exceeded the maximum amount of iterations per episode and did not reach a terminal state.", EClientInformation.INSTANCE.getPlayerID());
        }

        return iterations;
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

    public void evaluateQualityMatrix()
    {
        int sum = 0;

        for (int i = 0; i < EEnvironment.EPISODES; ++i)
        {
            sum += this.evaluateAnEpisode(this.getPseudorandomQualityStart(i));

            if ((i + 1) % EEnvironment.CALCULATE_AVERAGE_ITERATIONS == 0)
            {
                l.debug("Agent {} has evaluated {} episodes. Average iterations per episode of the last {}: {}.", EClientInformation.INSTANCE.getPlayerID(), i + 1, EEnvironment.CALCULATE_AVERAGE_ITERATIONS, sum / EEnvironment.CALCULATE_AVERAGE_ITERATIONS);
                sum = 0;
            }

            continue;
        }

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
                direction = "S";
            }
            else if (state.x() == actionState.x() && state.y() == actionState.y() + 1)
            {
                direction = "N";
            }
            else if (state.x() == actionState.x() - 1 && state.y() == actionState.y())
            {
                direction = "E";
            }
            else if (state.x() == actionState.x() + 1 && state.y() == actionState.y())
            {
                direction = "W";
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
        l.info("Qualities:");

        for (int file = 0; file < this.qualities.length; ++file)
        {
            for (int rank = 0; rank < this.qualities[file].length; ++rank)
            {
                l.info(this.getQualityStateAsString(new RCoordinate(file, rank)));
                continue;
            }

            continue;
        }

        return;
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

    public AgentSL_v2(final BufferedReader br)
    {
        super(br);
        return;
    }

    private void evaluateProgrammingPhaseWithQLearning()
    {
        EEnvironment.INSTANCE.initRewardMatrix();

        l.info(String.format("Current Course: Files: %d, Ranks: %d, Goal: %s", EEnvironment.INSTANCE.getFiles(), EEnvironment.INSTANCE.getRanks(), Objects.requireNonNull(EEnvironment.INSTANCE.getQualityGoal()).toString()));

        EEnvironment.INSTANCE.outputRewards();

        EEnvironment.INSTANCE.initQualityMatrix();
        EEnvironment.INSTANCE.evaluateQualityMatrix();

        EEnvironment.INSTANCE.outputQualities();

        return;
    }

    private void evaluateProgrammingPhaseWithRandom()
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

        for (int i = 0; i < 5; ++i)
        {
            new SelectedCardModel(i, EGameState.INSTANCE.getRegister(i)).send();
            continue;
        }

        return;
    }

    private void evaluateProgrammingPhase()
    {
        if (EClientInformation.INSTANCE.getAgentDifficulty() == EAgentDifficulty.RANDOM)
        {
            this.evaluateProgrammingPhaseWithRandom();
        }

        if (EClientInformation.INSTANCE.getAgentDifficulty() == EAgentDifficulty.QLEARNING)
        {
            this.evaluateProgrammingPhaseWithQLearning();
        }

        l.info("Agent {} evaluated for the current programming phase. The determined cards are: {}.", EClientInformation.INSTANCE.getPlayerID(), Arrays.toString(EGameState.INSTANCE.getRegisters()));

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
