package sep.server.model.game.tiles;

/**
 * Klasse, die die X und Y Koordinate eines Tiles im Course sowie die benachbarten Koordinaten speichert
 */
public class Coordinate{
    private final int xCoordinate;

    private final int yCoordinate;

    private Coordinate topNeighbor;
    private Coordinate rightNeighbor;
    private Coordinate bottomNeighbor;
    private Coordinate leftNeighbor;

    public Coordinate(int x, int y){
        xCoordinate = x;
        yCoordinate = y;

    }
    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    /**
     * gibt die Koordinate über der aktuellen
     * @return Koordinate überhalb oder null, wenn dort das Board zuende ist
     */
    public Coordinate getTopNeighbor() {
        return topNeighbor;
    }


    public void setTopNeighbor(Coordinate topNeighbor) {
        this.topNeighbor = topNeighbor;
    }

    /**
     * gibt die Koordinate rechts neben der aktuellen
     * @return Koordinate rechts daneben oder null, wenn dort das Board zuende ist
     */
    public Coordinate getRightNeighbor() {
        return rightNeighbor;
    }

    public void setRightNeighbor(Coordinate rightNeighbor) {
        this.rightNeighbor = rightNeighbor;
    }

    /**
     * gibt die Koordinate unterhalb der aktuellen
     * @return Koordinate unterhalb oder null, wenn dort das Board zuende ist
     */
    public Coordinate getBottomNeighbor() {
        return bottomNeighbor;
    }

    public void setBottomNeighbor(Coordinate bottomNeighbor) {
        this.bottomNeighbor = bottomNeighbor;
    }

    /**
     * gibt die Koordinate links neben der aktuellen
     * @return Koordiante links daneben oder null, wenn dort das Board zuende ist
     */
    public Coordinate getLeftNeighbor() {
        return leftNeighbor;
    }

    public void setLeftNeighbor(Coordinate leftNeighbor) {
        this.leftNeighbor = leftNeighbor;
    }

    @Override
    public String toString()
    {
        return String.format("(%d, %d)", this.xCoordinate, this.yCoordinate);
    }
}
