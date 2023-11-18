package sep.server.model.game.cards.damage;

import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public abstract class DamageCard extends Card implements IPlayableCard {

  public DamageCard(String cardType, boolean isProgrammable) {
    super(cardType, isProgrammable);
    setProgrammable(true);
  }

}
