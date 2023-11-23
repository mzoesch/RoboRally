package sep.server.model.game;

public class Robot {
  int positionX;
  int positionY;
  String direction;
  int priority;

  public Robot(int positionX, int positionY, String direction, int priority) {
    this.positionX = positionX;
    this.positionY = positionY;
    this.direction = direction;
    this.priority = priority;
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

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void reboot() {}
}
