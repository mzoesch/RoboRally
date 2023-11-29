package sep.server.model.game.cards.damage;

public class VirusDamage extends ADamageCard {
    public VirusDamage(String cardType) {
        super(cardType);
        this.cardType = "VirusDamage";
    }

    @Override
    public void playCard() {}
}
