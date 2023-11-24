package sep.server.model.game.tiles;

public class Antenna implements FieldType {
    private static String direction;
    private static String isOnBoard;

    public Antenna(String antennaDirection) {
        direction = antennaDirection;
    }
}
