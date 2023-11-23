package sep.server.model.game.tiles;

import java.util.ArrayList;

public class PushPanel implements FieldType {
    private static ArrayList<Integer> activateAtRegister;

    public PushPanel(ArrayList<Integer> activationRegisters) {
        activateAtRegister = activationRegisters;
    }
}
