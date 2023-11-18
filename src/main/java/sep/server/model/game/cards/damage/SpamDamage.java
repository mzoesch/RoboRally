package sep.server.model.game.cards.damage;

public class SpamDamage extends DamageCard {
    public SpamDamage(String cardType, boolean isProgrammable) {
        super(cardType, isProgrammable);
    }
    @Override
    public void playCard() {}
}
