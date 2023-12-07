package sep.server.model.game.cards.special;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class EnergyRoutine extends ASpecialProgrammingCard implements IPlayableCard {
    public EnergyRoutine(String cardType) {
        super(cardType);
        this.cardType = "EnergyRoutine";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber){

    }
}
