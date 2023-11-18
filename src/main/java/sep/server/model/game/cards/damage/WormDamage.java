package sep.server.model.game.cards.damage;

public class WormDamage extends DamageCard{
    public WormDamage(String cardType, boolean isProgrammable, int damageValue) {
        super(cardType, isProgrammable);
    }
    @Override
    public void playCard() {}
}
