package sep.view.clientcontroller;

import sep.server.model.game.cards.programming.MoveI;
import sep.view.json.DefaultServerRequestParser;
import sep.view.viewcontroller.SceneController;
import sep.view.viewcontroller.ViewSupervisor;
import sep.view.lib.EGamePhase;

import java.io.IOException;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static void closeSocket()
    {
        EClientInformation.INSTANCE.closeSocket();
        EClientInformation.INSTANCE.resetServerConnectionAfterDisconnect();

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
                final int escapeCharacter = this.bufferedReader.read();
                if (escapeCharacter == -1)
                {
                    GameInstance.handleServerDisconnect();
                    return;
                }

                final String r = String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine());

                try
                {
                    this.parseJSONRequestFromServer(new DefaultServerRequestParser(new JSONObject(r)));
                }
                catch (JSONException e)
                {
                    l.warn("Failed to parse JSON request from server. Ignoring.");
                    l.warn(e.getMessage());
                    l.warn(r);
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

        /* The server notifies the client about the nwe selected map. */
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
            String info = String.format("Player %s is now current Player", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getPlayerName());
            ViewSupervisor.handleChatInfo(info);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Error"))
        {
            l.debug("Received an error message from server. Message: {}.", dsrp.getErrorMessage());
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CardPlayed")) {
            String info = String.format("Player %s has played %s now current Player", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getPlayerName(), dsrp.getCardName());
            ViewSupervisor.handleChatInfo(info);
            l.debug("Received card played from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "StartingPointTaken"))
        {
            l.debug("Received starting point taken from server. Player {} took starting point {}.", dsrp.getPlayerID(), dsrp.getCoordinate().toString());
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).setStartingPosition(dsrp.getCoordinate());
            ViewSupervisor.updatePlayerTransforms();
            String info = String.format("Player %s is has selected a starting Point", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getPlayerName());
            ViewSupervisor.handleChatInfo(info);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "PlayerTurning")) {
            l.debug("Player {} turned {}.", dsrp.getPlayerID(), dsrp.getRotation());
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getRobotView().addRotation(dsrp.getRotation());
            ViewSupervisor.updatePlayerTransforms();
            return;
        }

        /* If a player has set one of their five register slots. */
        if (Objects.equals(dsrp.getType_v2(), "CardSelected"))
        {
            // TODO We want to update the UI-Footer with this method call. Check if the playerID is the local player.
            //      Currently we update regardless if the select card action was affirmed by the server.
            l.debug("Player {} updated their register {}. Filled: {}.", dsrp.getPlayerID(), dsrp.getRegister(), dsrp.getRegisterFilled() ? "true" : "false");
            return;
        }

        /* If one client has finished selecting their programming cards. */
        if (Objects.equals(dsrp.getType_v2(), "SelectionFinished")) {
            String info = String.format("Player %s has finished his card selection", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getPlayerName());
            ViewSupervisor.handleChatInfo(info);
            l.debug("Player {} finished their selection.", dsrp.getPlayerID());
            EGameState.INSTANCE.setSelectionFinished(dsrp.getPlayerID());
            return;
        }

        /* If this client's hand cards are being forced updated. */
        if (Objects.equals(dsrp.getType_v2(), "CardsYouGotNow")) {
            EGameState.INSTANCE.clearAllRegisters();
            for (String c : dsrp.getForcedCards())
            {
                EGameState.INSTANCE.addGotRegister(c);
                continue;
            }
            ViewSupervisor.updateFooter();
            String info = ("You didnt finished card selection in time. You recieved forced cards.");
            ViewSupervisor.handleChatInfo(info);
            l.debug("Player {} has not submitted their selection in time. Received new cards: {}", EClientInformation.INSTANCE.getPlayerID(), String.join(", ", Arrays.asList(dsrp.getForcedCards())));
            return;
        }

        /* The server notifies the client about the nine programming cards from another client. */
        if (Objects.equals(dsrp.getType_v2(), "NotYourCards")) {
            String info = String.format("Player %s has %s cards in his hand.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getPlayerName(), dsrp.getCardsInHand().length);
            ViewSupervisor.handleChatInfo(info);
            l.debug("Received not yours cards from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "ShuffleCoding")) {
            String info = String.format("The deck of player %s has been shuffled", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getPlayerName());
            ViewSupervisor.handleChatInfo(info);
            l.debug("Received shuffle coding from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "TimerEnded")) {
            String info = String.format("Timer has ended");
            ViewSupervisor.handleChatInfo(info);
            l.debug("Received timer ended from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "TimerStarted")) {
            //TODO Timer?
            String info = ("Timer has started");
            ViewSupervisor.handleChatInfo(info);
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

        /* The current cards in a register played by all clients. */
        if (Objects.equals(dsrp.getType_v2(), "CurrentCards"))
        {
            //TODO grafische Anzeige der jeweiligen Karte?
            final StringBuilder sb = new StringBuilder();
            sb.append(String.format("In this register %d cards were played (", dsrp.getActiveCards().length()));
            sb.append(IntStream.range(0, dsrp.getActiveCards().length()).mapToObj(i -> String.format("%s[Player %d played card %s]", i == 0 ? "" : ", ", dsrp.getPlayerIDFromActiveCardIdx(i), dsrp.getActiveCardFromIdx(i))).collect(Collectors.joining()));
            l.debug(sb.append(").").toString());
            String info = sb.append(").").toString();
            ViewSupervisor.handleChatInfo(info);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "ReplaceCard")) {
            if(dsrp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID()){
                String info = String.format("You recieved following card %s as replacement", dsrp.getNewCard());
                ViewSupervisor.handleChatInfo(info);
                EGameState.INSTANCE.addRegister(dsrp.getRegister(), dsrp.getNewCard());
            } else{
                String info = String.format("Player %s recieved following card %s as replacement",Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getPlayerName(),  dsrp.getNewCard());
                ViewSupervisor.handleChatInfo(info);
            }
            l.debug("Received replacing card from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Animation")) {
            l.debug("Received animation from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "CheckPointReached")) {
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).setCheckPointsReached(dsrp.getNumber());
            String info = String.format("Player %s has reached %s checkpoints", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getPlayerName(), dsrp.getNumber());
            ViewSupervisor.handleChatInfo(info);
            l.debug("Received checkpoint from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Energy")) {
            //keine Chatinfo, da ja Energy in PlayerInformationen angezeigt
            l.debug("Player {} EnergyCubeAmmount has been set to {}", dsrp.getPlayerID(), dsrp.getEnergyCount());
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).setEnergy(dsrp.getEnergyCount());
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "GameFinished")) {
            l.debug("Received game finished from server.");
            EGameState.INSTANCE.determineWinningPlayer(dsrp.getWinningPlayer());
            ViewSupervisor.getSceneController().renderNewScreen(SceneController.END_SCENE_ID, SceneController.PATH_TO_END_SCENE, true);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Movement")) {
            l.debug("Player {} has moved to {},{}", dsrp.getPlayerID(), dsrp.getCoordinate().x(), dsrp.getCoordinate().y());
            EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID()).getRobotView().setPosition(dsrp.getCoordinate(), false, true);
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "RebootDirection")) {
            //TODO die Nachricht geht doch vom Client to Server und wird von diesem dann als PlayerTurning an alle verschickt oder?
            l.debug("Received reboot direction from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "Reboot")) {
            String info = String.format("Robot of Player %s has been rebooted", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(dsrp.getPlayerID())).getPlayerName(), dsrp.getNumber());
            ViewSupervisor.handleChatInfo(info);
            l.debug("Received reboot from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "ConnectionUpdate")) {
            l.debug("Received connection update from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "DrawDamage")) {

            l.debug("Received draw a damage card from server.");
            return;
        }

        if (Objects.equals(dsrp.getType_v2(), "SelectedDamage")) {
            l.debug("Damage was selected.");
        }

        l.warn("Received unknown request from server. Ignoring.");
        l.warn(dsrp.getType_v2());

        return;
    }

}
