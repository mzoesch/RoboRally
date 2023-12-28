package sep.view.lib;

public class Types
{
    private Types() throws IllegalStateException
    {
        throw new IllegalStateException("Types class");
    }

    public enum EFigure
    {
        INVALID(-1),
        HAMMER(0),
        TRUNDLE(1),
        SQUASH(2),
        X90(3),
        SPIN(4),
        TWONKY(5),
        TWITCH(6),
        NUM(7);

        private static final String[] FIGURE_NAMES = new String[] {"Hammer Bot", "Trundle Bot", "Squash Bot", "Hulk x90", "Spin Bot", "Twonky", "Twitch"};

        public final int i;

        private EFigure(final int i)
        {
            this.i = i;
            return;
        }

        public static EFigure fromInt(final int i)
        {
            for (final EFigure e : EFigure.values())
            {
                if (e.i == i)
                {
                    return e;
                }

                continue;
            }

            return null;
        }

        @Override
        public String toString() throws ArrayIndexOutOfBoundsException
        {
           return Types.EFigure.FIGURE_NAMES[this.i];
        }

    }

}
