package sep.view.lib;

public final class Types
{
    private Types() throws IllegalStateException
    {
        throw new IllegalStateException("Types class");
    }

    public enum EFigure
    {
        INVALID (  -1,  "Invalid"       ),
        HAMMER  (   0,  "Hammer Bot"    ),
        TRUNDLE (   1,  "Trundle Bot"   ),
        SQUASH  (   2,  "Squash Bot"    ),
        X90     (   3,  "Hulk x90"      ),
        SPIN    (   4,  "Spin Bot"      ),
        TWONKY  (   5,  "Twonky"        ),
        TWITCH  (   6,  "Twitch"        ),
        NUM     (   7,  "FALSE USE"     )
        ;

        public final int i;
        private final String s;

        private EFigure(final int i, final String name)
        {
            this.i = i;
            this.s = name;

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
        public String toString()
        {
            return this.s;
        }

    }

    public enum EPopUp
    {
        ERROR,
    }

    public record RPopUpMask(EPopUp type, String header, String msg)
    {
        public RPopUpMask
        {
        }

        public RPopUpMask(final EPopUp type)
        {
            this(type, type.toString(), null);
        }

        public RPopUpMask(final EPopUp type, final String msg)
        {
            this(type, type.toString() , msg);
        }

    }

}
