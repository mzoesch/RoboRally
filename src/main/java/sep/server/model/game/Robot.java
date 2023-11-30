package sep.server.model.game;

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
    Tile choosenStart = course.getTileByNumbers(x,y);
    if(choosenStart != null) {
      if(choosenStart.isOccupied()){
        return -1;
      } else if(!choosenStart.isStartingPoint()){
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
     Tile choosenStart = course.getTileByNumbers(x,y);
     direction = course.getStartingDirection();
     currentTile = choosenStart;
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
