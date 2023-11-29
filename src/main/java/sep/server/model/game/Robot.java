package sep.server.model.game;

public class Robot {
  String direction;
  private final Course course;
  private Tile currentTile;

  public Robot(String direction, Course course, Tile currentTile) {
    this.direction = direction;
    this.course = course;
    this.currentTile = currentTile;
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

  public void reboot() {}

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
