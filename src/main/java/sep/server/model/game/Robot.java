package sep.server.model.game;

import sep.server.json.game.damage.DrawDamageModel;
import sep.server.model.game.tiles.FieldType;
import sep.server.model.game.tiles.RestartPoint;
import sep.server.viewmodel.PlayerController;
import sep.server.viewmodel.Session;
import sep.server.model.game.tiles.Coordinate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class Robot {
    private static final Logger l = LogManager.getLogger(GameState.class);

    String direction;
    private final Course course;
    private Tile startingPoint;
    private boolean rebootTriggered;

    /* TODO WE MUST REMOVE THIS ASAP. THIS CAUSES SO MANY PROBLEMS!!!! */
    /** @deprecated Make to gateway method */
    private Tile currentTile;

    private final Player possessor;

    private Boolean canShootBackward;

    public Robot(boolean rebootTriggered, Player possessor, Course course) {
        this.course = course;
        startingPoint = null;
        this.rebootTriggered = rebootTriggered;
        currentTile = null;
        this.possessor = possessor;
        this.canShootBackward = false;
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

    /**
     * Validates a passed starting point: Is it on the course? Is it a starting point? Is it still free?
     * @param x x coordinate of the starting point
     * @param y y coordinate of the starting point
     * @return 1 if successful, 0 if not on course, -1 if occupied by another robot, -2 if not a starting point
     */
    public int validStartingPoint(int x, int y) {
        Tile chosenStart = course.getTileByNumbers(x, y);
        if (chosenStart != null) {
            if (chosenStart.isOccupied()) {
                return -1;
            } else if (!chosenStart.isStartingPoint()) {
                return -2;
            } else
                return 1;
        } else {
            return 0;
        }
    }

    public void setStartingPoint(int x, int y) {
        startingPoint = course.getTileByNumbers(x, y);
        direction = setStartDirection();
        currentTile = startingPoint;
        currentTile.setOccupiedBy(this);
        startingPoint.setRobot(this);
    }

    private String setStartDirection() {
        switch (course.getStartingTurningDirection()) {
            case ("clockwise") -> {
                return "right";
            }
            case ("counterclockwise") -> {
                return "left";
            }
            case ("") -> {
                return "top";
            }
        }
        return "bottom";
    }

    /**
     * Moves the robot one tile based on the given direction.
     * Updates the robot's position.
     *
     * @param forward True if the robot should move forwards, false if backwards.
     * @param depth   How deep we already are in the recursion (should never exceed six).
     */
    public void moveRobotOneTile(final boolean forward, String direction, int depth) {
        final int dir = forward ? 1 : -1;
        final Coordinate currentCoordinate = this.getCurrentTile().getCoordinate();
        Coordinate targetCoordinate = null;

        switch (direction) {
            case "north", "top" -> targetCoordinate = new Coordinate(currentCoordinate.getX(), currentCoordinate.getY() - dir);
            case "south", "bottom" -> targetCoordinate = new Coordinate(currentCoordinate.getX(), currentCoordinate.getY() + dir);
            case "east", "right" -> targetCoordinate = new Coordinate(currentCoordinate.getX() + dir, currentCoordinate.getY());
            case "west", "left" -> targetCoordinate = new Coordinate(currentCoordinate.getX() - dir, currentCoordinate.getY());
            default -> l.error("Player {}'s robot has an invalid direction: {}", this.determineRobotOwner().getController().getPlayerID(), this.getDirection());
        }

        if (targetCoordinate == null) {
            l.error("Player {}'s robot has an invalid direction: {}", this.determineRobotOwner().getController().getPlayerID(), this.getDirection());
            return;
        }

        l.trace("Player {}'s robot wants to move from ({}, {}) to ({}, {}).", this.determineRobotOwner().getController().getPlayerID(), currentCoordinate.getX(), currentCoordinate.getY(), targetCoordinate.getX(), targetCoordinate.getY());

        if (!this.getCourse().isCoordinateWithinBounds(targetCoordinate)) {
            l.debug("Player {}'s robot moved to {} and fell off the board. Rebooting . . .", this.determineRobotOwner().getController().getPlayerID(), targetCoordinate.toString());
            this.reboot();
            return;
        }

        if(this.getCourse().getTileByCoordinate(targetCoordinate).isPit()) {
            l.debug("Player {}'s robot moved to {} and fell down a pit. Rebooting . . .", this.determineRobotOwner().getController().getPlayerID(), targetCoordinate.toString());
            this.reboot();
            return;
        }

        if (!this.isTraversable(this.getCurrentTile(), this.getCourse().getTileByCoordinate(targetCoordinate), depth)) {
            l.debug("Player {}'s robot wanted to traverse an impassable tile [from {} to {}]. Ignoring.", this.determineRobotOwner().getController().getPlayerID(), currentCoordinate.toString(), targetCoordinate.toString());
            return;
        }

        this.getCurrentTile().setOccupiedBy(null);
        this.getCourse().updateRobotPosition(this, targetCoordinate);
        getCurrentTile().setOccupiedBy(this);

        l.debug("Player {}'s robot moved [from {} to {}].", this.determineRobotOwner().getController().getPlayerID(), currentCoordinate.toString(), targetCoordinate.toString());
    }

    /**
     * Moves the robot one tile forwards based on the robot's current direction.
     * Updates the robot's position.
     */
    public void moveRobotOneTileForwards() {
        moveRobotOneTile(true, this.getDirection().toLowerCase(), 0);
    }

    /**
     * Moves the robot one tile backwards based on the robot's current direction.
     * Updates the robot's position.
     */
    public void moveRobotOneTileBackwards() {
        moveRobotOneTile(false, this.getDirection().toLowerCase(), 0);
    }

    /**
     * Rotates the robot 90 degrees to the right or to the left
     * Updates the robot's direction
     */
    public void rotateRobotOnTile(boolean isRightRotation) {
        String currentDirection = this.getDirection();
        String newDirection;

        switch (currentDirection.toLowerCase()) {
            case "north", "top" -> newDirection = isRightRotation ? "right" : "left";
            case "east", "right" -> newDirection = isRightRotation ? "bottom" : "top";
            case "south", "bottom" -> newDirection = isRightRotation ? "left" : "right";
            case "west", "left" -> newDirection = isRightRotation ? "top" : "bottom";
            default -> {
                l.error("Player {}'s robot has an invalid direction: {}", this.determineRobotOwner().getController().getPlayerID(), this.getDirection());
                return;
            }
        }
        this.setDirection(newDirection);
    }

    public void rotateRobotOnTileToTheRight() {
        rotateRobotOnTile(true);
    }

    public void rotateRobotOnTileToTheLeft() {
        rotateRobotOnTile(false);
    }

    /**
     * The following method handles the rebooting of a robot and sends all respective JSON messages.
     * The player draws two spam cards and the registers are emptied.
     * Disclaimer: Default rebootDirection is "top". If the client selects a rebootDirection (normally
     * handled in the GUI) until the end of the round, the reboot direction is updated accordingly
     * (in the GameState).
     */
    public void reboot() {
        Player robotOwner = determineRobotOwner();
        Tile sourceTile = this.getCurrentTile();
        Tile restartPoint = null;
        String restartDirection = "top";
        this.rebootTriggered = true;

        final Thread rebootAsync = new Thread(() ->
        {
            /* We do this to let the client finish their animations. */
            try
            {
                Thread.sleep(2_000);
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

            this.getAuthGameMode().getSession().broadcastReboot(robotOwner.getController().getPlayerID());

        });

        rebootAsync.start();

        if(this.getAuthGameMode().getSpamDeck().size() >= 2) {
            robotOwner.getDiscardPile().add(this.getAuthGameMode().getSpamDeck().get(0));
            robotOwner.getDiscardPile().add(this.getAuthGameMode().getSpamDeck().get(0));

            l.debug("Player {} has drawn two spam cards.", this.determineRobotOwner().getController().getPlayerID());

            if (robotOwner.getController() instanceof PlayerController pc)
            {
                //TODO noch nicht implementiert, was passiert, wenn keine Spam-Karten mehr vorhanden
                new DrawDamageModel(pc.getClientInstance(), robotOwner.getController().getPlayerID(), new String[]{"Spam", "Spam"}).send();
            }
            else
            {
                l.error("Agent draw damage not implemented yet.");
            }

        } else {
            l.error("Spam card deck is empty.");
        }

        if(this.determineRobotOwner().getRegisters().length > 0) {

            for (int i = 0; i < 5; i++) {
                robotOwner.getDiscardPile().add(robotOwner.getCardByRegisterIndex(i));
                robotOwner.setCardInRegister(i, null, true);
            }
            l.debug("Registers were emptied.");

        } else {
            l.warn("Registers can't be emptied.");
        }

        switch (sourceTile.getBoardName()) {
            case "StartA" -> {
                if((Objects.equals(this.getSession().getGameState().getCourseName(), "Death Trap")) ||
                        (Objects.equals(this.getSession().getGameState().getCourseName(), "DeathTrap"))) {
                    restartPoint = course.getTileByNumbers(12,9);
                } else {
                    restartPoint = startingPoint;
                }
            }
            case "1A", "4A" -> {
                restartPoint = course.getTileByNumbers(0, 0);
            }
            case "5B" -> {
                restartPoint = course.getTileByNumbers(7, 3);
            }
            case "6B" -> {
                restartPoint = course.getTileByNumbers(0, 7);
            }
        }

        //we need the direction of the rebootToken if we want to reboot on an occupied rebootField
        for(FieldType f : restartPoint.getFieldTypes()){
            if(f instanceof RestartPoint r){
                restartDirection = r.getRestartOrientation();
                l.debug("RebootDirection is set to {} ", restartDirection);
            }
        }

        if(restartPoint != null) {

            //TODO brauchen wir das so? Oder ist das eher hard-coded im client?

            final Thread rotAsync = new Thread(() ->
            {
                /* We do this to let the client finish their animations. */
                try
                {
                    Thread.sleep(2_000);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }

                switch(this.direction) {
                    case "right" -> this.getSession().broadcastRotationUpdate(robotOwner.getController().getPlayerID(), "counterclockwise");
                    case "bottom" -> {
                        this.getSession().broadcastRotationUpdate(robotOwner.getController().getPlayerID(), "counterclockwise");
                        this.getSession().broadcastRotationUpdate(robotOwner.getController().getPlayerID(), "counterclockwise");
                    }
                    case "left" -> this.getSession().broadcastRotationUpdate(robotOwner.getController().getPlayerID(), "clockwise");
                }
            });
            rotAsync.start();

            if(restartPoint.isOccupied() && restartPoint.hasUnmovableRobot(restartDirection, 0)) {
                restartPoint = startingPoint;
                l.debug("RebootField is occupied and occupying robot can not be moved. Rebooting on startingField");
            } else if(restartPoint.isOccupied()){
                restartPoint.getRobot().moveRobotOneTile(true, restartDirection, 0);
                l.debug("RebootField is occupied. Occupying robot should move one tile in direction: {} .", restartDirection);
            }

            this.setCurrentTile(restartPoint);
            this.getCurrentTile().setOccupiedBy(this);
            this.determineRobotOwner().getAuthGameMode().addDelay(1000);


            final Tile finalRestartPoint = restartPoint;
            final Thread posAsync = new Thread(() ->
            {
                /* We do this to let the client finish their animations. */
                try
                {
                    Thread.sleep(2_000);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }

                this.getSession().broadcastPositionUpdate(robotOwner.getController().getPlayerID(), finalRestartPoint.getCoordinate().getX(), finalRestartPoint.getCoordinate().getY());
            });
            posAsync.start();

            this.determineRobotOwner().getAuthGameMode().addDelay(1000);
            l.debug("Player {} was assigned a restart point.", this.determineRobotOwner().getController().getPlayerID());

        } else {
            l.error("No restart point was assigned.");
        }

        this.setDirection("top");
    }

    /**
     * The following method checks if a tile is traversable meaning if it is an antenna, if it has a wall,
     * or if it is occupied by an unmovable robot.
     *
     * @param source tile the robot is coming from
     * @param t1     tile the robot is moving to
     * @param depth  how deep we already are in the recursion (should never exceed six).
     *               As there is only a maximum of six players in a game.
     * @return true if traversable, false if not
     */
    public boolean isTraversable(final Tile source, final Tile t1, int depth)
    {
        if (depth > 7)
        {
            l.warn("Recursion depth exceeded while checking for traversable of tile {}. Marking tile as unpassable.", t1.getCoordinate().toString());
            return false;
        }

        if (t1.hasAntennaModifier()) {
            l.trace("Robot is unmovable because of the antenna modifier");
            return false;
        }

        if (source.hasWallModifier()) {
            if (source.isEastOf(t1) && source.isWallWest()) {
                l.trace("Robot cannot traverse west because of a wall modifier.");
                return false;
            }

            if (source.isWestOf(t1) && source.isWallEast()) {
                l.trace("Robot cannot traverse east because of a wall modifier.");
                return false;
            }

            if (source.isNorthOf(t1) && source.isWallSouth()) {
                l.trace("Robot cannot traverse south because of a wall modifier.");
                return false;
            }

            if (source.isSouthOf(t1) && source.isWallNorth()) {
                l.trace("Robot cannot traverse north because of a wall modifier.");
                return false;
            }
        }

        if (t1.hasWallModifier()) {
            if (source.isEastOf(t1) && t1.isWallEast()) {
                l.trace("Robot cannot traverse west because of a wall modifier.");
                return false;
            }

            if (source.isWestOf(t1) && t1.isWallWest()) {
                l.trace("Robot cannot traverse east because of a wall modifier.");
                return false;
            }

            if (source.isNorthOf(t1) && t1.isWallNorth()) {
                l.trace("Robot cannot traverse south because of a wall modifier.");
                return false;
            }

            if (source.isSouthOf(t1) && t1.isWallSouth()) {
                l.trace("Robot cannot traverse north because of a wall modifier.");
                return false;
            }
        }

        if (t1.hasUnmovableRobot(direction, depth + 1)){
            l.trace("Robot is unmovable because of another unmovable robot");
            return false;
        }

        return true;
    }

    /**
     * The following method is used to find the owner (player) of a robot.
     * @return owner
     */
    public Player determineRobotOwner() {
        for(Player player : this.getAuthGameMode().getPlayers()) {
            if(player.getPlayerRobot() == this) {
                return player;
            }
        }
        return null;
    }

    public GameMode getAuthGameMode() {
        return this.possessor.getAuthGameMode();
    }

    public Session getSession()
    {
        return this.getAuthGameMode().getSession();
    }

    public Player getPossessor()
    {
        return possessor;
    }

    /** @deprecated  */
    public void setCanShootBackward(Boolean canShootBackward) {
        this.canShootBackward = canShootBackward;
    }

    public Boolean getCanShootBackward() {
        return this.hasRearLaserCard();
    }

    private boolean hasRearLaserCard()
    {
        l.debug("Checking if player {} has RearLaser card. Their bought upgrade cards are: {}", this.getPossessor().getController().getPlayerID(), this.getPossessor().getBoughtUpgradeCards());
        return this.getPossessor().getBoughtUpgradeCards().contains("RearLaser");
    }

    public boolean isRebootTriggered() {
        return rebootTriggered;
    }

    public void setRebootTriggered(boolean rebootTriggered) {
        this.rebootTriggered = rebootTriggered;
    }
}
