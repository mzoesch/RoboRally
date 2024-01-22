package sep.server.model.game.cards;

/**
 * Represents an abstract card in the game.
 * All specific card types should extend this class.
 */
public abstract class Card {

  public String cardType;

  public Card(String cardType) {
    this.cardType = cardType;
  }

  public String getCardType() {
    return cardType;
  }

  public void setCardType(String cardType) {
    this.cardType = cardType;
  }

}
