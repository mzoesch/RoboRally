package sep.server.model.game.cards.damage;

public class TrojanHorseDamage extends DamageCard{
    public TrojanHorseDamage(String cardType, boolean isProgrammable, int damageValue) {
        super(cardType, isProgrammable);
    }
    @Override
    public void playCard() {}
}
