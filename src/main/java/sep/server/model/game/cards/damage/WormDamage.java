package sep.server.model.game.cards.damage;

public class WormDamage extends ADamageCard {
    public WormDamage(String cardType) {
        super(cardType);
        this.cardType = "WormDamage";
    }

    @Override
    public void playCard() {}
}
