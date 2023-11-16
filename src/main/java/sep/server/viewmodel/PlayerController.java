package sep.server.viewmodel;

import sep.server.json.ChatMessageModel;

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
    private final String playerName;
    private final Session session;

    public PlayerController(ClientInstance clientInstance, String playerName, Session session)
    {
        super();

        this.clientInstance = clientInstance;
        this.playerName = playerName;
        this.session = session;

        return;
    }

    public void sendChatMessage(String caller, String message)
    {
        new ChatMessageModel(this.clientInstance, caller, message).send();
        return;
    }

    public Session getSession()
    {
        return this.session;
    }

    public String getPlayerName()
    {
        return this.playerName;
    }

    public ClientInstance getClientInstance()
    {
        return this.clientInstance;
    }

}
