package sep.view.clientcontroller;

import sep.view.json.DefaultServerRequestParser;
import sep.view.viewcontroller.ViewLauncher;

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
            l.trace("Received keep-alive from server. Responding.");
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

        if (Objects.equals(dsrp.getType_v2(), "PlayerAdded"))
        {
            l.debug("Received player added from server.");
            EGameState.addRemotePlayer(dsrp);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "ReceivedChat"))
        {
            l.debug("Received chat message from server.");
            ViewLauncher.handleChatMessage(dsrp);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "PlayerStatus"))
        {
            l.debug("Received player status update.");
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).setReady(dsrp.isLobbyPlayerStatusReady());
            ViewLauncher.updatePlayerStatus(dsrp);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "SelectMap"))
        {
            l.debug("Received course selected from server.");
            EGameState.INSTANCE.setServerCourses(dsrp.getAvailableCourses());
            ViewLauncher.updateAvailableCourses(true);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "MapSelected"))
        {
            l.debug("Received course selected from server.");
            EGameState.INSTANCE.setCurrentServerCourse(dsrp.getCourseName());
            ViewLauncher.updateCourseSelected();
            return;
        }

        /* Currently only supports the mock game start. */
        if (Objects.equals(dsrp.getType_v2(), "GameStarted"))
        {
            l.debug("Received start game from server.");
            ViewLauncher.startGame(dsrp.getGameCourse());
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CurrentPlayer")) {
            l.debug("Received current player from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Error")) {
            l.debug("Received an error message from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CardPlayed")) {
            l.debug("Received card played from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "GameStarted")) {
            l.debug("Received game started from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "StartingPointTaken")) {
            l.debug("Received starting point from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CardSelected")) {
            l.debug("Received card selected from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CardsYouGorNow")) {
            l.debug("Received cards you got now from server.");
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

        if (Objects.equals(dsrp.getType_v2(), "YourCards")) {
            l.debug("Received your cards from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Animation")) {
            l.debug("Received animation from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CheckPoint")) {
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
            l.debug("Received reboot diretion from server.");
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
