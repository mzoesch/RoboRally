package sep.server.model.game.tiles;

public class Coordinate{
    private static int xCoordinate;

    private static int yCoordinate;

    private Coordinate northNeighbor;
    private Coordinate eastNeighbor;
    private Coordinate southNeighbor;
    private Coordinate westNeighbor;

    public Coordinate(int x, int y){
        xCoordinate = x;
        yCoordinate = y;

    }
    public static int getXCoordinate() {
        return xCoordinate;
    }

    public static int getYCoordinate() {
        return yCoordinate;
    }

    public Coordinate getNorthNeighbor() {
        return northNeighbor;
    }

    public Coordinate getEastNeighbor() {
        return eastNeighbor;
    }

    public Coordinate getSouthNeighbor() {
        return southNeighbor;
    }

    public Coordinate getWestNeighbor() {
        return westNeighbor;
    }

    public void setNorthNeighbor(Coordinate northNeighbor) {
        this.northNeighbor = northNeighbor;
    }

    public void setEastNeighbor(Coordinate eastNeighbor) {
        this.eastNeighbor = eastNeighbor;
    }

    public void setSouthNeighbor(Coordinate southNeighbor) {
        this.southNeighbor = southNeighbor;
    }

    public void setWestNeighbor(Coordinate westNeighbor) {
        this.westNeighbor = westNeighbor;
    }
}
