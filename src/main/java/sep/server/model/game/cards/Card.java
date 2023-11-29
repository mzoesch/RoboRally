package sep.server.model.game.cards;

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
