package sep.server.viewmodel;

import sep.server.json.ChatMessageModel;

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
