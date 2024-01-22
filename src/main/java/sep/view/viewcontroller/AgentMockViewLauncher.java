package sep.view.viewcontroller;

import sep.view.json.               RDefaultServerRequestParser;
import sep.view.clientcontroller.   EClientInformation;
import sep.view.clientcontroller.   EGameState;
import sep.view.clientcontroller.   AgentSL_v2;
import sep.view.clientcontroller.   AgentRemotePlayerData;
import sep.view.lib.                EGamePhase;
import sep.view.lib.                RCoordinate;

import org.json.                    JSONObject;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.util.                   Objects;

public final class AgentMockViewLauncher implements IMockView
{
    private static final Logger l = LogManager.getLogger(AgentMockViewLauncher.class);

    private static final int    MOCK_PLAYER_COUNT      = 2;


    public AgentMockViewLauncher()
    {
        super();
        return;
    }

    @Override
    public void run()
    {
        EClientInformation.INSTANCE.setMockView(true);
        EClientInformation.INSTANCE.setPlayerID(0);

        /* Players */
        for (int i = 0; i < AgentMockViewLauncher.MOCK_PLAYER_COUNT; ++i)
        {
            EGameState.addRemotePlayer(new RDefaultServerRequestParser(new JSONObject(String.format("{\"messageType\":\"PlayerAdded\",\"messageBody\":{\"clientID\": %d,\"name\":\"Player %d\", \"figure\": %d}}", i, i, i))));
            continue;
        }

        EGameState.INSTANCE.setCurrentServerCourseJSON(new RDefaultServerRequestParser(new JSONObject("{\"messageType\":\"GameStarted\",\"messageBody\":{\"gameMap\":[[[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"StartPoint\",\"isOnBoard\":\"StartA\"}],[{\"orientations\":[\"right\"],\"type\":\"Antenna\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"StartPoint\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}]],[[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"StartPoint\",\"isOnBoard\":\"StartA\"}],[{\"orientations\":[\"top\"],\"type\":\"Wall\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"StartPoint\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"StartPoint\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"orientations\":[\"bottom\"],\"type\":\"Wall\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"StartPoint\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}]],[[{\"orientations\":[\"right\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"StartA\",\"speed\":1}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"orientations\":[\"right\"],\"type\":\"Wall\",\"isOnBoard\":\"StartA\"}],[{\"orientations\":[\"right\"],\"type\":\"Wall\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"StartA\"}],[{\"orientations\":[\"right\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"StartA\",\"speed\":1}]],[[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"right\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"right\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"count\":1,\"type\":\"EnergySpace\",\"isOnBoard\":\"5B\"}]],[[{\"orientations\":[\"bottom\",\"top\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"bottom\",\"top\",\"right\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"bottom\",\"top\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"bottom\",\"top\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"bottom\",\"top\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"bottom\",\"top\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"bottom\",\"top\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"bottom\",\"top\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"right\",\"top\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}]],[[{\"orientations\":[\"bottom\",\"top\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"left\",\"top\",\"right\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"count\":1,\"type\":\"EnergySpace\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"right\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}]],[[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"left\",\"right\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"top\"],\"type\":\"Wall\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"bottom\"],\"type\":\"Wall\",\"isOnBoard\":\"5B\"},{\"orientations\":[\"top\"],\"count\":1,\"type\":\"Laser\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"left\"],\"type\":\"Wall\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"right\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}]],[[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"left\",\"right\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"bottom\"],\"type\":\"RestartPoint\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"count\":1,\"type\":\"EnergySpace\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"right\"],\"type\":\"Wall\",\"isOnBoard\":\"5B\"},{\"orientations\":[\"left\"],\"count\":1,\"type\":\"Laser\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"right\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}]],[[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"left\",\"right\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"left\"],\"type\":\"Wall\",\"isOnBoard\":\"5B\"},{\"orientations\":[\"right\"],\"count\":1,\"type\":\"Laser\",\"isOnBoard\":\"5B\"}],[{\"count\":1,\"type\":\"EnergySpace\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"right\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}]],[[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"left\",\"right\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"right\"],\"type\":\"Wall\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"top\"],\"type\":\"Wall\",\"isOnBoard\":\"5B\"},{\"orientations\":[\"bottom\"],\"count\":1,\"type\":\"Laser\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"bottom\"],\"type\":\"Wall\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"right\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}]],[[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"left\",\"right\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"count\":1,\"type\":\"EnergySpace\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"right\",\"bottom\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"top\",\"bottom\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}]],[[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"left\",\"right\",\"bottom\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"top\",\"right\",\"bottom\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"top\",\"bottom\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"top\",\"bottom\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"top\",\"bottom\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"top\",\"bottom\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"top\",\"bottom\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"top\",\"bottom\",\"left\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"top\",\"bottom\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}]],[[{\"count\":1,\"type\":\"EnergySpace\",\"isOnBoard\":\"5B\"}],[{\"orientations\":[\"left\",\"right\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"orientations\":[\"left\",\"right\"],\"type\":\"ConveyorBelt\",\"isOnBoard\":\"5B\",\"speed\":2}],[{\"count\":1,\"type\":\"CheckPoint\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}],[{\"type\":\"Empty\",\"isOnBoard\":\"5B\"}]]]}}")).getGameCourse());
        EGameState.INSTANCE.setCurrentPhase(EGamePhase.PROGRAMMING);

        final String[] mockPCards = new String[] {"MoveI", "MoveII", "MoveIII", "Again", "TurnRight", "TurnLeft", "UTurn", "MoveII", "MoveI"};
        for (final String s : mockPCards)
        {
            EGameState.INSTANCE.addGotRegister(s);
        }

        Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).setStartingPosition(new RCoordinate(0, 3));
        ( (AgentRemotePlayerData) EGameState.INSTANCE.getClientRemotePlayer() ).setRotation(90);

        new AgentSL_v2(null).onDevelopmentEvaluation();

        l.info("Mock view killed.");

        return;
    }
}
