package sep.server.model.game.tiles;

public class Laser implements FieldType {
    private static int laserCount;


    public Laser(int thisLaserCount) {
        laserCount = thisLaserCount;
    }
}
