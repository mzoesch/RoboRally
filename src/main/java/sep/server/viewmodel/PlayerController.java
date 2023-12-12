package sep.server.viewmodel;

import sep.server.json.common.ChatMsgModel;
import sep.server.model.game.GameMode;
import sep.server.model.game.Player;
import sep.server.model.IOwnershipable;

/**
 * The interface between the Pawn in the game and the human player controlling it. The Player Controller essentially
 * represents the human player's will. The Player Controller is created at client session connection and is not
 * destroyed until the client disconnects.
 *
 * <p> The Player Controller should never hold relevant information about the current game state. It should only be
 * used to control the Pawn. A Player Controller could always be destroyed mid-game, for example, if the client
 * disconnects or a new one crated if a new client connects and posses an already existing pawn (e.g., a pawn that was
 * previously controlled by a bot).
 */
public final class PlayerController implements IOwnershipable
{
    private final ClientInstance clientInstance;
    private String playerName;
    private final int playerID;

    private final Session session;
    private int figure;
    private boolean bIsReady;

    private Player player;

    public PlayerController(final ClientInstance clientInstance, final String playerName, final int playerID, final Session session)
    {
        super();

        this.clientInstance = clientInstance;
        this.playerName = playerName;
        this.playerID = playerID;

        this.session = session;
        this.figure = -1;
        this.bIsReady = false;

        this.player = null;

        return;
    }

    public void sendChatMessage(final int caller, final String message, final boolean bIsPrivate)
    {
        new ChatMsgModel(this.clientInstance, caller, message, bIsPrivate).send();
        return;
    }

    // region Getters and Setters

    public Session getSession()
    {
        return this.session;
    }

    @Override
    public String getName()
    {
        return this.playerName;
    }

    public void setPlayerName(final String playerName)
    {
        this.playerName = playerName;
        return;
    }

    @Override
    public int getPlayerID()
    {
        return this.playerID;
    }

    @Override
    public int getFigure()
    {
        return this.figure;
    }

    public void setFigure(final int figure)
    {
        this.figure = figure;
        return;
    }

    @Override
    public void setPlayer(final Player p) {
        this.player = p;
    }

    public ClientInstance getClientInstance()
    {
        return this.clientInstance;
    }

    public boolean isReady()
    {
        return this.bIsReady;
    }

    public void setReady(final boolean bIsReady)
    {
        this.bIsReady = bIsReady;
        this.session.broadcastPlayerLobbyReadyStatus(this);
        return;
    }

    public void setSelectedCardInRegister(final String selectedCard, final int selectedRegister)
    {
        this.player.setCardToRegister(selectedCard, selectedRegister);
    }

    public Player getPlayer()
    {
        return this.player;
    }

    @Override
    public GameMode getAuthGameMode()
    {
        return this.session.getGameState().getAuthGameMode();
    }

    // endregion Getters and Setters

}
