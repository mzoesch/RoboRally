package sep.server.viewmodel;

import sep.server.json.common.ChatMsgModel;
import sep.server.model.game.Player;

import java.util.ArrayList;


/**
 * The interface between the Pawn in the game and the human player controlling it. The Player Controller essentially
 * represents the human player's will. The Player Controller is created at client session connection and is not
 * destroyed until the client disconnects.
 *
 * <p> The Player Controller should never hold relevant information about the current game state. It should only be
 * used to control the Pawn. A Player Controller could always be destroyed mid-game, for example, if the client
 * disconnects or a new one crated if a new client connects and posses an already existing pawn (e.g., a pawn that was
 * previously controlled by a bot).
 * (We know that we have to write some sort of AI in an upcoming milestone.)
 */
public final class PlayerController
{
    private final ClientInstance clientInstance;
    private String playerName;
    private final int playerID;

    private final Session session;
    private int figure;
    private boolean bIsReady;



    public PlayerController(ClientInstance clientInstance, String playerName, int playerID, Session session)
    {
        super();

        this.clientInstance = clientInstance;
        this.playerName = playerName;
        this.playerID = playerID;
        this.session = session;
        this.figure = -1;
        this.bIsReady = false;

        return;
    }

    public void sendChatMessage(int caller, String message, boolean bIsPrivate)
    {
        new ChatMsgModel(this.clientInstance, caller, message, bIsPrivate).send();
        return;
    }

    // region Getters and Setters

    public Session getSession()
    {
        return this.session;
    }

    public String getPlayerName()
    {
        return this.playerName;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
        return;
    }

    public int getPlayerID()
    {
        return this.playerID;
    }

    public int getFigure()
    {
        return this.figure;
    }

    public void setFigure(int figure)
    {
        this.figure = figure;
        return;
    }

    public ClientInstance getClientInstance()
    {
        return this.clientInstance;
    }

    public boolean isReady()
    {
        return this.bIsReady;
    }

    public void setReady(boolean bIsReady)
    {
        this.bIsReady = bIsReady;
        this.session.broadcastPlayerLobbyReadyStatus(this);
        return;
    }

    public void getSelectedCard(String selectedCard, int selectedRegister){

        Player playerWhoSelectedCard = null;

        for (Player player : session.getGameState().getAuthGameMode().getPlayers()) {
            if (player.getPlayerController().getPlayerID() == playerID) {
                playerWhoSelectedCard = player;
                break;
            }
        }
        playerWhoSelectedCard.addCardToRegister(selectedCard, selectedRegister);
    }



    // endregion Getters and Setters

}
