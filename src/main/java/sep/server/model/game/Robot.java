package sep.server.model.game;

import sep.server.model.game.tiles.Coordinate;

public class Robot {
  int positionX;
  int positionY;
  String direction;
  private Course course;
  private Tile currentTile;

  //TODO Konstruktor Robot-Klasse anpassen
  public Robot(int positionX, int positionY, String direction) {
    this.positionX = positionX;
    this.positionY = positionY;
    this.direction = direction;
  }

  public int getPositionX() {
    return positionX;
  }

  public void setPositionX(int positionX) {
    this.positionX = positionX;
  }

  public int getPositionY() {
    return positionY;
  }

  public void setPositionY(int positionY) {
    this.positionY = positionY;
  }
  public Tile getCurrentTile() {
    return currentTile;
  }

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }
  public void setCurrentTile(Tile currentTile) {
    this.currentTile = currentTile;
  }
  public Course getCourse() {
    return course;
  }


  public void reboot() {}

  public boolean isMovable(Tile targetTile) {
    if (targetTile.hasAntenna()){
      return false;
    };
    if (targetTile.hasWall()){
      return false;
    }
    if (targetTile.hasUnmovableRobot()) {
      return false;
    }
    return true;
  }
}
