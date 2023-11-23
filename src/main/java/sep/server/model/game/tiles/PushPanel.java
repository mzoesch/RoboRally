package sep.server.model.game.tiles;

import java.util.ArrayList;

public class PushPanel implements FieldType {

    private static int[] activateAtRegister;

    private static String orientation;

    public PushPanel(String pushOrientation,int[] activationRegisters) {
        orientation = pushOrientation;

        activateAtRegister = activationRegisters;
    }
}
