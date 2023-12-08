package sep.view.lib;

public record RCoordinate(int x, int y)
{
    public RCoordinate(final int x, final int y)
    {
        this.x = x;
        this.y = y;

        return;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof final RCoordinate c))
        {
            return false;
        }

        return (this.x == c.x) && (this.y == c.y);
    }

    @Override
    public String toString()
    {
        return String.format("(%d, %d)", this.x, this.y);
    }

}
