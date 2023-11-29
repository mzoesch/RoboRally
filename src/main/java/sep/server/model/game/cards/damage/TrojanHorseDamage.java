package sep.server.model.game.cards.damage;

public class TrojanHorseDamage extends ADamageCard {
    public TrojanHorseDamage(String cardType) {
        super(cardType);
        this.cardType = "TrojanHorseDamage";
    }

    @Override
    public void playCard() {}
}
