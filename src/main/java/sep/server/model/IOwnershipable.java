package sep.server.model;

import sep.server.model.game.GameMode;
import sep.server.model.game.Player;

public interface IOwnershipable
{
    public String getName();
    public int getPlayerID();
    public int getFigure();

    public GameMode getAuthGameMode();

    public void setPlayer(final Player p);

}
