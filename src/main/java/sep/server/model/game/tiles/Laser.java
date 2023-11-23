package sep.server.model.game.tiles;

public class Laser implements FieldType {

    private static int laserCount;

    private String orientation;

    public Laser(String laserOrientation, int thisLaserCount) {

        orientation = laserOrientation;
        laserCount = thisLaserCount;
    }
}
