package sep.server.model.game.cards.damage;

import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Abstract class representing a damage card.
 */
public abstract class ADamageCard extends Card implements IPlayableCard {

  public ADamageCard(String cardType) {
    super(cardType);
  }

  @Override
  public String getCardType() {
    return super.getCardType();
  }

  @Override
  public String toString() {
    return super.getCardType();
  }
}
