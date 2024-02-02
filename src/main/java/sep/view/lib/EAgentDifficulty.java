package sep.view.lib;

public enum EAgentDifficulty
{
    RANDOM,
    QLEARNING,
    ;

    public static EAgentDifficulty fromInt(final int difficulty) throws IllegalArgumentException
    {
        return switch (difficulty)
        {
            case    0   -> RANDOM;
            case    1   -> QLEARNING;
            default     -> throw new IllegalArgumentException(String.format("Invalid difficulty: %s.", difficulty));
        };
    }

}
