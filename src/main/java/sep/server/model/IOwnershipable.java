package sep.server.model;

import sep.server.model.game.   GameMode;
import sep.server.model.game.   Player;

public interface IOwnershipable
{
    public static final int              INVALID_FIGURE                  = -1;

    public abstract String               getName();
    public abstract int                  getPlayerID();
    public abstract void                 setFigure(final int figure);
    public abstract int                  getFigure();

    public abstract GameMode             getAuthGameMode();

    public abstract void                 setPlayer(final Player p);

    public abstract Player               getPlayer();
}
