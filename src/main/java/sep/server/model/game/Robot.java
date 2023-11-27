package sep.server.model.game;

public class Robot {
  int positionX;
  int positionY;
  String direction;

  private Course course;

  private Tile occupies;

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

  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public void reboot() {}
}
