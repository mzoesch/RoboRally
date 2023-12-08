package sep.server.model.game;

import sep.server.json.game.effects.RebootModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Robot
{
    private static final Logger l = LogManager.getLogger(GameState.class);

    String direction;
    private final Course course;

    /**
     * @deprecated Make to gateway method
     */
    private Tile currentTile;

    public Robot(Course course)
    {
        this.course = course;
        currentTile = null;
    }

    /**
     * prüft den übergebenen Startpunkt, ob entsprechendes Feld auf Course & StartingPoint & unbesetzt
     *
     * @param x x-Koordinate des Startfelds
     * @param y y-Koordinate des Startfelds
     * @return 1, wenn erfolgreich; 0, wenn nicht auf Course; -1, wenn besetzt; -2, wenn kein StartingPoint
     */
    public int validStartingPoint(int x, int y)
    {
        Tile chosenStart = course.getTileByNumbers(x, y);
        if (chosenStart != null)
        {
            if (chosenStart.isOccupied())
            {
                return -1;
            } else if (!chosenStart.isStartingPoint())
            {
                return -2;
            } else
                return 1;
        } else
        {
            return 0;
        }
    }

    public void setStartingPoint(int x, int y)
    {
        Tile chosenStart = course.getTileByNumbers(x, y);
        direction = setStartDirection();
        currentTile = chosenStart;
    }

    private String setStartDirection()
    {
        switch (course.getStartingTurningDirection())
        {
            case ("clockwise") ->
            {
                return "right";
            }
            case ("counterclockwise") ->
            {
                return "left";
            }
            case ("") ->
            {
                return "top";
            }
        }
        return "bottom";
    }

    public String getDirection()
    {
        return direction;
    }

    public void setDirection(String direction)
    {
        this.direction = direction;
    }

    public Tile getCurrentTile()
    {
        return currentTile;
    }

    public void setCurrentTile(Tile currentTile)
    {
        this.currentTile = currentTile;
    }

    public Course getCourse()
    {
        return course;
    }

    public void reboot()
    {
        for (Player player : GameState.gameMode.getPlayers())
        {
            if (this.equals(player.getPlayerRobot()) && GameState.gameMode.getSpamDeck().size() >= 2)
            {

                for (Player player1 : GameState.gameMode.getPlayers())
                {
                    new RebootModel(player1.getPlayerController().getClientInstance(),
                            player.getPlayerController().getPlayerID()).send();
                }

                player.getDiscardPile().add(GameState.gameMode.getSpamDeck().get(0));
                player.getDiscardPile().add(GameState.gameMode.getSpamDeck().get(0));

                for (int i = 0; i < player.getRegisters().length; i++)
                {
                    player.getDiscardPile().add(player.getCardByRegisterIndex(i));
                    player.setCardInRegister(i, null);
                }

                //TODO finish
            }
        }
    }

    public boolean isNotTraversable(final Tile source, final Tile t1)
    {
        if (t1.hasAntennaModifier())
        {
            l.debug("Robot is unmovable because of the antenna modifier");
            return true;
        }

        if (t1.hasWallModifier())
        {
            if (source.isEastOf(t1) && t1.isWallWest())
            {
                l.debug("Robot cannot traverse east because of a wall modifier.");
                return true;
            }
            if (source.isWestOf(t1) && t1.isWallEast())
            {
                l.debug("Robot cannot traverse west because of a wall modifier.");
                return true;
            }
            if (source.isNorthOf(t1) && t1.isWallSouth())
            {
                l.debug("Robot cannot traverse north because of a wall modifier.");
                return true;
            }
            if (source.isSouthOf(t1) && t1.isWallNorth())
            {
                l.debug("Robot cannot traverse south because of a wall modifier.");
                return true;
            }
        }

        if (t1.hasUnmovableRobot())
        {
            l.debug("Robot is unmovable because of another unmovable robot");
            return true;
        }

        return false;
    }

}
