package sep.server.model.game;

public class Tile {
    private static Coordinate coordinate;
    private Robot occupiedBy = null;
    private static ArrayList<FieldType> fieldTypes;

    public Tile(Coordinate fieldCoordinate, ArrayList<FieldTypes> arrayFieldTypes){
        this.coordinate = fieldCoordinate;
        this.fieldTypes = arrayFieldTypes;
    }

    public Robot getRobot(){
        return occupiedBy;
    }

    public void setRobot(Robot newRobot) throws OccupiedException{
        if(occupiedBy == null){
            occupiedBy = newRobot;
        }else{
            throw OccupiedException("This field already has a robot");
        }
    }

    public Coordinate getCoordinate(){
        return coordinate;
    }

    public ArrayList<FieldType> getFieldTypes() {
        return fieldTypes;
    }
}
