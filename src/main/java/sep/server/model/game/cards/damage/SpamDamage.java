package sep.server.model.game.cards.damage;

public class SpamDamage extends ADamageCard {
    public SpamDamage(String cardType) {
        super(cardType);
        this.cardType = "SpamDamage";
    }

    @Override
    public void playCard() {}
}
