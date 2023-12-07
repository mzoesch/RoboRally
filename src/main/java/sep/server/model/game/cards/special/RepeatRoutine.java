package sep.server.model.game.cards.special;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class RepeatRoutine extends ASpecialProgrammingCard implements IPlayableCard {
    public RepeatRoutine(String cardType) {
        super(cardType);
        this.cardType = "RepeatRoutine";
    }
    @Override
    public void playCard(Player player, int currentRoundNumber)  {

    }
}
