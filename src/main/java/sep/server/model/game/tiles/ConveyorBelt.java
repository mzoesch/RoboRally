package sep.server.model.game.tiles;

public class ConveyorBelt implements FieldType {

    private static String incomingFlowDirection;
    private static String [] outcomingFlowDirection;
    private static int speed;

    public ConveyorBelt(int beltSpeed, String inDirection, String [] outDirection) {

        incomingFlowDirection = inDirection;
        outcomingFlowDirection = outDirection;
        speed = beltSpeed;
    }

    public static String getIncomingFlowDirection() {
        return incomingFlowDirection;
    }

    public static String[] getOutcomingFlowDirection() {
        return outcomingFlowDirection;
    }

    public static int getSpeed() {
        return speed;
    }
}
