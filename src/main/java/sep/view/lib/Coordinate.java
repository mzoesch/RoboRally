package sep.view.lib;

public class Coordinate
{
    private final int x;
    private final int y;

    public Coordinate(int x, int y)
    {
        this.x = x;
        this.y = y;
        return;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof Coordinate c))
        {
            return false;
        }

        return (this.x == c.x) && (this.y == c.y);
    }

    @Override
    public int hashCode()
    {
        return (this.x * 31) + this.y;
    }

    @Override
    public String toString()
    {
        return String.format("(%d, %d)", this.x, this.y);
    }

}
