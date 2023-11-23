package sep.server.model.game.tiles;

public class Coordinate{
    private static int xCoordinate;

    private static int yCoordinate;

    private Coordinate topNeighbor;
    private Coordinate rightNeighbor;
    private Coordinate bottomNeighbor;
    private Coordinate leftNeighbor;

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

    public Coordinate getTopNeighbor() {
        return topNeighbor;
    }

    public void setTopNeighbor(Coordinate topNeighbor) {
        this.topNeighbor = topNeighbor;
    }

    public Coordinate getRightNeighbor() {
        return rightNeighbor;
    }

    public void setRightNeighbor(Coordinate rightNeighbor) {
        this.rightNeighbor = rightNeighbor;
    }

    public Coordinate getBottomNeighbor() {
        return bottomNeighbor;
    }

    public void setBottomNeighbor(Coordinate bottomNeighbor) {
        this.bottomNeighbor = bottomNeighbor;
    }

    public Coordinate getLeftNeighbor() {
        return leftNeighbor;
    }

    public void setLeftNeighbor(Coordinate leftNeighbor) {
        this.leftNeighbor = leftNeighbor;
    }
}
