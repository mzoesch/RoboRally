package sep.server.model.game.tiles;

public class Gear implements FieldType {

    private static String rotationalDirection;

    public Gear(String gearRotationalDirection) {
        rotationalDirection = gearRotationalDirection;
    }
}
