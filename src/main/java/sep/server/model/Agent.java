package sep.server.model;

import sep.server.model.game.GameMode;
import sep.server.viewmodel.Session;
import sep.server.model.game.Player;

public class Agent implements IOwnershipable
{
    public static final String AGENT_PREFIX = "[BOT]";
    public static final String[] AGENT_NAMES = new String[] { "Martinez", "Anderson", "Reynolds", "Thompson", "Mitchell", "Parker", "Turner", "Bennett", "Foster", "Ramirez" };

    private final String agentName;
    private final int agentID;
    private final Session session;

    private int figure;

    private Player possessing;

    public Agent(final String agentName, final int agentID, final Session session)
    {
        super();

        this.agentName = agentName;
        this.agentID = agentID;
        this.session = session;

        this.figure = IOwnershipable.INVALID_FIGURE;

        this.possessing = null;

        return;
    }

    @Override
    public String getName()
    {
        return this.agentName;
    }

    @Override
    public int getPlayerID()
    {
        return this.agentID;
    }

    @Override
    public void setFigure(final int figure)
    {
        this.figure = figure;
        return;
    }

    @Override
    public int getFigure()
    {
        return this.figure;
    }

    @Override
    public GameMode getAuthGameMode()
    {
        return this.session.getGameState().getAuthGameMode();
    }

    @Override
    public void setPlayer(final Player p)
    {
        this.possessing = p;
        return;
    }

}
