package sep.server.model.game;
import java.util.List;
import sep.server.model.game.cards.Card;

public class Player {
  String playerName;
  int playerID;
  int playerScore;
  Robot playerRobot;
  List<Card> handCards;
}
