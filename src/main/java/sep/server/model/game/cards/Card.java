package sep.server.model.game.cards;

public abstract class Card {
  String cardType;
  boolean isProgrammable;

  public Card(String cardType, boolean isProgrammable) {
    this.cardType = cardType;
    this.isProgrammable = isProgrammable;
  }

  public String getCardType() {
    return cardType;
  }

  public void setCardType(String cardType) {
    this.cardType = cardType;
  }

  public boolean isProgrammable() {
    return isProgrammable;
  }

  public void setProgrammable(boolean programmable) {
    isProgrammable = programmable;
  }
}
