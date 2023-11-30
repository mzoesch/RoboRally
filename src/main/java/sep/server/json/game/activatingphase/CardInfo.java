package sep.server.json.game.activatingphase;

public class CardInfo {

    private final int clientID;
    private final String card;

    public CardInfo(int clientID, String card) {
        this.clientID = clientID;
        this.card = card;
    }

    public int getClientID() {
        return clientID;
    }

    public String getCard() {
        return card;
    }
}
