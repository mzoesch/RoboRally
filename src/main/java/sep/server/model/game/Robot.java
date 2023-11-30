package sep.server.model.game;

import sep.server.json.game.effects.RebootModel;

public class Robot {
  String direction;
  private final Course course;
  private Tile currentTile;

  public Robot(Course course) {
    this.course = course;
    currentTile = null;
  }

  /**
   * prüft den übergebenen Startpunkt, ob entsprechendes Feld auf Course & StartingPoint & unbesetzt
   * @param x x-Koordinate des Startfelds
   * @param y y-Koordinate des Startfelds
   * @return 1, wenn erfolgreich; 0, wenn nicht auf Course; -1, wenn besetzt; -2, wenn kein StartingPoint
   */
  public int validStartingPoint(int x, int y){
    Tile chosenStart = course.getTileByNumbers(x,y);
    if(chosenStart != null) {
      if(chosenStart.isOccupied()){
        return -1;
      } else if(!chosenStart.isStartingPoint()){
        return -2;
      }
      else
        return 1;
      }
    else{
      return 0;
    }
  }
   public void setStartingPoint(int x, int y){
     Tile chosenStart = course.getTileByNumbers(x,y);
     direction = course.getStartingDirection();
     currentTile = chosenStart;
   }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public Tile getCurrentTile() {
    return currentTile;
  }

  public void setCurrentTile(Tile currentTile) {
    this.currentTile = currentTile;
  }
  public Course getCourse() {
    return course;
  }

  public void reboot() {
    for(Player player : GameState.gameMode.getPlayers()) {
      if(this.equals(player.playerRobot) && GameState.gameMode.spamCardDeck.size() >= 2) {

        for(Player player1 : GameState.gameMode.getPlayers()) {
          new RebootModel(player1.getPlayerController().getClientInstance(),
                  player.getPlayerController().getPlayerID()).send();
        }

        player.getDiscardPile().add(GameState.gameMode.spamCardDeck.get(0));
        player.getDiscardPile().add(GameState.gameMode.spamCardDeck.get(0));

        for (int i = 0; i < player.getRegisters().length; i++) {
            player.getDiscardPile().add(player.getCardInRegister(i));
            player.setCardInRegister(i,null);
        }

        //TODO finish
      }
    }
  }

  public boolean isMovable(Tile targetTile) {
    if (targetTile.hasAntenna()){
      return false;
    }
    if (targetTile.hasWall()){
      return false;
    }
    if (targetTile.hasUnmovableRobot()) {
      return false;
    }
    return true;
  }
}
