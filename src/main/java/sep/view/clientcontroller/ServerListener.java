package sep.view.clientcontroller;

import sep.view.json.DefaultServerRequestParser;
import sep.view.scenecontrollers.LobbyJFXController;
import sep.view.viewcontroller.ViewLauncher;

import java.io.IOException;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * We create a special object for listening to the server socket on a separate
 * thread to avoid blocking the main thread of the application.
 */
public class ServerListener implements Runnable
{
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

        try
        {
            bufferedWriter.write((char) ServerListener.ESCAPE_CHARACTER);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch (IOException e)
        {
            System.err.println("[CLIENT] Failed to send escape character.");
            EClientInformation.INSTANCE.setServerListener(null);
            return;
        }

        System.out.printf("[CLIENT] Send closing connection request to server.%n");
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
                    System.err.println("[CLIENT] Failed to parse JSON request from server. Ignoring.");
                    System.err.println(e.getMessage());
                    continue;
                }

                continue;
            }
        }
        catch (IOException e)
        {
            System.err.printf("[CLIENT] Failed to read from server.%n");
            System.err.println(e.getMessage());
            GameInstance.handleServerDisconnect();
            return;
        }
    }

    private void parseJSONRequestFromServer(DefaultServerRequestParser dsrp) throws JSONException
    {
        if (Objects.equals(dsrp.getType(), "chatMessage"))
        {
            // TODO To interface which controllers can receive chat messages.
            LobbyJFXController ctrl;
            try
            {
                ctrl = (LobbyJFXController) ViewLauncher.getSceneController().getCurrentController();
            }
            catch (ClassCastException e)
            {
                System.err.println("[CLIENT] Failed to cast controller to LobbyFXController.");
                System.err.println(e.getMessage());
                return;
            }

            ctrl.addMessage(dsrp.getCaller(), dsrp.getChatMessage());

            return;
        }

        if (Objects.equals(dsrp.getType(), "sessionState"))
        {
            // TODO To interface which controllers can receive session state updates.
            LobbyJFXController ctrl;
            try
            {
                ctrl = (LobbyJFXController) ViewLauncher.getSceneController().getCurrentController();
            }
            catch (ClassCastException e)
            {
                System.err.println("[CLIENT] Failed to cast controller to LobbyFXController.");
                System.err.println(e.getMessage());
                return;
            }

            EGameState.INSTANCE.update(dsrp);
            ctrl.updatePlayerNames();

            return;
        }

        System.err.println("[CLIENT] Received unknown request from server.");
        System.err.println(dsrp.getRequest().toString(1));

        return;
    }

}