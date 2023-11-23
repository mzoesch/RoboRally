package sep.server.model.game.tiles;

public class EnergySpace implements FieldType {
    private int availableEnergy;

    public EnergySpace(int availableEnergy) {
        this.availableEnergy = availableEnergy;
    }
}
