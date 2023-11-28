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

        if (Objects.equals(dsrp.getType_v2(), "CurrentPlayer")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Error")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CardPlayed")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "GameStarted")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "StartingPointTaken")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CardSelected")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CardsYouGorNow")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "NotYourCards")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "ShuffleCoding")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "TimerEnded")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "TimerStarted")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "YourCards")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Animation")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CheckPoint")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Energy")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "GameFinished")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Movement")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "PlayerTurning")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "RebootDirection")) {
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Reboot")) {
            return;
        }
        l.warn("Received unknown request from server. Ignoring.");
        l.warn(dsrp.getType_v2());

        return;
    }

}
