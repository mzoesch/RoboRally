package sep.server.model.game.tiles;

public class Wall implements FieldType {
    private String[] orientations;

    public Wall(String[] wallOrientations){
            this.orientations = wallOrientations;
    }
}
