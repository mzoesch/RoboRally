package sep.view.lib;

public enum EGamePhase
{
    INVALID(-1),
    REGISTRATION(0),
    UPGRADE(1),
    PROGRAMMING(2),
    ACTIVATION(3);

    public final int i;

    private EGamePhase(int i)
    {
        this.i = i;
        return;
    }

    public static EGamePhase fromInt(int i)
    {
        for (EGamePhase e : EGamePhase.values())
        {
            if (e.i == i)
            {
                return e;
            }
        }

        return EGamePhase.INVALID;
    }

    public String phaseDescription(){
        switch(i){
            case(0):
                return "Choose a starting point for your Robot, if it is your turn to do it";
            case(1):
                return "Choose upgradeCards to buy with your energyCubes";
            case(2):
                return "Select programmingCards by clicking on a card in your hand and clicking on a register";
            case(3):
                return "Watch your robot move";
        }
        return "Can not describe ProgrammingPhase";
    }

}
