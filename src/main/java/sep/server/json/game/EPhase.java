package sep.server.json.game;

public enum EPhase {
    BUILD("BuildPhase", 0),
    UPGRADE("UpgradePhase", 1),
    PROGRAM("ProgramPhase", 2),
    ACTIVATION("ActivationPhase", 3);

    private final String messageType;
    private final int phaseNumber;

    EPhase(String messageType, int phaseNumber) {
        this.messageType = messageType;
        this.phaseNumber = phaseNumber;
    }

    public String getMessageType() {
        return messageType;
    }

    public int getPhaseNumber() {
        return phaseNumber;
    }
}
