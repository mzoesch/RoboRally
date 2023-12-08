package sep.server.model.game.cards.damage;

import sep.server.json.game.activatingphase.ReplaceCardModel;
import sep.server.model.game.Player;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public class WormDamage extends ADamageCard {
    public WormDamage(String cardType) {
        super(cardType);
        this.cardType = "WormDamage";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().reboot();
    }
}
