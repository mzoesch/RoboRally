package sep.view.clientcontroller;

import sep.view.json.DefaultServerRequestParser;
import sep.view.viewcontroller.ViewSupervisor;
import sep.view.lib.EGamePhase;

import java.io.IOException;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Arrays;

/**
 * We create a special object for listening to the server socket on a separate
 * thread to avoid blocking the main thread of the application.
 */
public class ServerListener implements Runnable
{
    private static final Logger l = LogManager.getLogger(ServerListener.class);

    /** Escape character to close the connection to the server. In ASCII this is the dollar sign. */
    private static final int ESCAPE_CHARACTER = 36;

    private final Socket socket;
    private final InputStreamReader inputStreamReader;
    private final BufferedReader bufferedReader;

    public ServerListener(Socket socket, InputStreamReader inputStreamReader, BufferedReader bufferedReader)
    {
        super();

        this.socket = socket;
        this.inputStreamReader = inputStreamReader;
        this.bufferedReader = bufferedReader;

        return;
    }

    public static void closeSocket(BufferedWriter bufferedWriter)
    {
        if (bufferedWriter == null)
        {
            return;
        }

        // TODO ALL DEPRECATED PLEASE REMOVE IF WE KNOW HOW TO DISCONNECT FROM SERVER!
        //      This is not to the norm of Protocol v0.1!
        try
        {
            bufferedWriter.write((char) ServerListener.ESCAPE_CHARACTER);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch (IOException e)
        {
            l.error("Failed to send escape character.");
            EClientInformation.INSTANCE.setServerListener(null);
            return;
        }

        l.debug("Send closing connection request to server.");
        EClientInformation.INSTANCE.setServerListener(null);

        l.info("Client disconnected from server.");

        return;
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                // If the server closed the connection in an orderly way, we will receive -1;
                int escapeCharacter = this.bufferedReader.read();
                if (escapeCharacter == -1)
                {
                    GameInstance.handleServerDisconnect();
                    return;
                }

                try
                {
                    this.parseJSONRequestFromServer(new DefaultServerRequestParser(new JSONObject(String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine()))));
                }
                catch (JSONException e)
                {
                    l.warn("Failed to parse JSON request from server. Ignoring.");
                    l.warn(e.getMessage());
                    continue;
                }

                continue;
            }
        }
        catch (IOException e)
        {
            l.fatal("Failed to read from server.");
            l.fatal(e.getMessage());
            GameInstance.handleServerDisconnect();
            return;
        }
    }

    private void parseJSONRequestFromServer(DefaultServerRequestParser dsrp) throws JSONException
    {
        l.trace("Received request from server. Parsing: {}", dsrp.getRequest().toString(2));

        if (Objects.equals(dsrp.getType_v2(), "Alive"))
        {
            l.trace("Received keep-alive from server. Responding. Ok.");
            try
            {
                GameInstance.respondToKeepAlive();
            }
            catch (IOException e)
            {
                GameInstance.handleServerDisconnect();
                return;
            }

            return;
        }

        /* Core player attributes have changed. */
        if (Objects.equals(dsrp.getType_v2(), "PlayerAdded"))
        {
            l.debug("Received player added for client {}.", dsrp.getPlayerID());
            EGameState.addRemotePlayer(dsrp);
            return;
        }

        /* New chat message. */
        if (Objects.equals(dsrp.getType_v2(), "ReceivedChat"))
        {
            l.debug("New chat message received: [{}] from {}.", dsrp.getChatMsg(), dsrp.getChatMsgSourceID());
            ViewSupervisor.handleChatMessage(dsrp);
            return;
        }

        /* If a client is ready to start in the lobby menu. */
        if (Objects.equals(dsrp.getType_v2(), "PlayerStatus"))
        {
            l.debug("Received player status update. Client {} is ready: {}.", dsrp.getPlayerID(), dsrp.isLobbyPlayerStatusReady());
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).setReady(dsrp.isLobbyPlayerStatusReady());
            ViewSupervisor.updatePlayerStatus(dsrp);
            return;
        }

        /* The receiving client may choose the server map. */
        if (Objects.equals(dsrp.getType_v2(), "SelectMap"))
        {
            l.debug("Received course selected from server. Available courses: {}.", String.join(", ", Arrays.asList(dsrp.getAvailableCourses())));
            EGameState.INSTANCE.setServerCourses(dsrp.getAvailableCourses());
            ViewSupervisor.updateAvailableCourses(true);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "MapSelected"))
        {
            l.debug("Received course selected from server. Selected course: {}.", dsrp.getCourseName() == null || dsrp.getCourseName().isEmpty() ? "none" : dsrp.getCourseName());
            EGameState.INSTANCE.setCurrentServerCourse(dsrp.getCourseName());
            ViewSupervisor.updateCourseSelected();
            return;
        }

        /* Currently only supports the mock game start. */
        if (Objects.equals(dsrp.getType_v2(), "GameStarted"))
        {
            l.debug("Received start game from server.");
            ViewSupervisor.startGame(dsrp.getGameCourse());
            return;
        }

        /* If the game enters a new phase. */
        if (Objects.equals(dsrp.getType_v2(), "ActivePhase"))
        {
            l.debug("Received game phase update. New phase: {}.", EGamePhase.fromInt(dsrp.getPhase()));
            EGameState.INSTANCE.setCurrentPhase(EGamePhase.fromInt(dsrp.getPhase()));
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CurrentPlayer"))
        {
            l.debug("Received current player update. New current player: {}.", dsrp.getPlayerID());
            EGameState.INSTANCE.setCurrentPlayer(dsrp.getPlayerID());
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Error"))
        {
            l.debug("Received an error message from server. Message: {}.", dsrp.getErrorMessage());
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CardPlayed")) {
            l.debug("Received card played from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "StartingPointTaken"))
        {
            l.debug("Received starting point taken from server. Player {} took starting point {}.", dsrp.getPlayerID(), dsrp.getCoordinate().toString());
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).setStartingPosition(dsrp.getCoordinate());
            ViewSupervisor.updatePlayerPosition();
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CardSelected")) {
            l.debug("Received card selected from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "SelectionFinished")) {
            l.debug("Received selection finished from client.");
            return;
        }

        /* The server notifies the client about the nine programming cards from another client. */
        if (Objects.equals(dsrp.getType_v2(), "CardsYouGotNow")) {
            l.debug("Player {}'s hand cards are updated.", dsrp.getPlayerID());
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "NotYourCards")) {
            l.debug("Received not yours cards from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "ShuffleCoding")) {
            l.debug("Received shuffle coding from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "TimerEnded")) {
            l.debug("Received timer ended from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "TimerStarted")) {
            l.debug("Received timer started from server.");
            return;
        }

        /* The nine cards a player gets at the beginning of a programming phase. */
        if (Objects.equals(dsrp.getType_v2(), "YourCards")) {
            l.debug("Received nine new programming cards from server {}.", String.join(", ", Arrays.asList(dsrp.getCardsInHand())));
            EGameState.INSTANCE.clearAllRegisters();
            for (String c : dsrp.getCardsInHand())
            {
                EGameState.INSTANCE.addGotRegister(c);
                continue;
            }
            ViewSupervisor.updateFooter();
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CurrentCards")) {
            l.debug("Received your current cards from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "ReplaceCard")) {
            l.debug("Received replacing card from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Animation")) {
            l.debug("Received animation from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CheckPointReached")) {
            l.debug("Received checkpoint from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Energy")) {
            l.debug("Received energy from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "GameFinished")) {
            l.debug("Received game finished from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Movement")) {
            l.debug("Received movement from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "PlayerTurning")) {
            l.debug("Received player turning from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "RebootDirection")) {
            l.debug("Received reboot direction from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Reboot")) {
            l.debug("Received reboot from server.");
            return;
        }

        l.warn("Received unknown request from server. Ignoring.");
        l.warn(dsrp.getType_v2());

        return;
    }

}
