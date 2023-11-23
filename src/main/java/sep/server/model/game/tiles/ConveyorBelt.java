package sep.server.model.game.tiles;

import java.util.ArrayList;

public class ConveyorBelt implements FieldType {
    private static String incomingFlowDirection;
    private static ArrayList<String> outcomingFlowDirection;
    private static int speed;

    public ConveyorBelt(String inDirection, ArrayList<String> outDirection, int beltSpeed){
        incomingFlowDirection = inDirection;
        outcomingFlowDirection = outDirection;
        speed = beltSpeed;
    }

    public static String getIncomingFlowDirection() {
        return incomingFlowDirection;
    }

    public static ArrayList<String> getOutcomingFlowDirection() {
        return outcomingFlowDirection;
    }

    public static int getSpeed() {
        return speed;
    }
}
