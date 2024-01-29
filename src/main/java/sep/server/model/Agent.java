package sep.server.model;

import sep.server.viewmodel.        Session;
import sep.server.model.game.       GameMode;
import sep.server.model.game.       Player;
import sep.server.model.game.       Tile;

import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.util.                   Arrays;

/** @deprecated */
public final class Agent implements IOwnershipable
{
    private static final Logger     l               = LogManager.getLogger(Agent.class);

    public static final String      AGENT_PREFIX    = "[BOT]";
    public static final String[]    AGENT_NAMES     = new String[] { "Martinez", "Anderson", "Reynolds", "Thompson", "Mitchell", "Parker", "Turner", "Bennett", "Foster", "Ramirez" };

    private final String            agentName;
    private final int               agentID;
    private final Session           session;

    private int                     figure;

    private Player                  possessing;

    public Agent(final String agentName, final int agentID, final Session session)
    {
        super();

        this.agentName      = agentName;
        this.agentID        = agentID;
        this.session        = session;

        this.figure         = IOwnershipable.INVALID_FIGURE;

        this.possessing     = null;

        return;
    }

    public void evaluateStartingPoint()
    {
        l.debug("Agent {} is evaluating starting point.", this.agentID);
        final Tile t;
        if ((t = this.possessing.getPlayerRobot().getCourse().getNextFreeStartingPoint()) != null)
        {
            l.debug("Agent {} evaluated starting point at {}.", this.agentID, t.getCoordinate().toString());
            this.getAuthGameMode().setStartingPoint(this, t.getCoordinate().getX(), t.getCoordinate().getY());
            return;
        }

        l.error("Agent {} could not evaluate starting point.", this.agentID);

        return;
    }

    public void evaluateProgrammingPhase()
    {
        l.debug("Agent {} is evaluating for the current programming phase.", this.agentID);

        /* Very, very primitive. Just a framework for now. Open for later construction. */
        for (int i = 0; i < 5; ++i)
        {
            this.possessing.setCardToRegister(this.possessing.getPlayerHand().get(i).getCardType(), i);
            continue;
        }

        l.debug("Agent {} evaluated for the current programming phase. The determined cards are: {}.", this.agentID, Arrays.toString(this.possessing.getRegisters()));

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

    @Override
    public Player getPlayer()
    {
        return this.possessing;
    }
    
}
