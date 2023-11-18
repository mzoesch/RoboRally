package sep.server.model.game.cards.damage;

public class VirusDamage extends DamageCard{
    public VirusDamage(String cardType, boolean isProgrammable, int damageValue) {
        super(cardType, isProgrammable);
    }
    @Override
    public void playCard() {}
}
