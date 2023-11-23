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

        l.warn("Received unknown request from server. Ignoring.");
        l.warn(dsrp.getType_v2());

        return;
    }

}
