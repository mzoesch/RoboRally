package sep.server.model;

import sep.server.model.game.GameMode;
import sep.server.model.game.Player;

public interface IOwnershipable
{
    public static final int INVALID_FIGURE = -1;

    public String getName();
    public int getPlayerID();
    public void setFigure(final int figure);
    public int getFigure();

    public GameMode getAuthGameMode();

    public void setPlayer(final Player p);

}
