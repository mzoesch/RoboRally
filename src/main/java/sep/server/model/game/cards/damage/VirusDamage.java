package sep.server.model.game.cards.damage;

import sep.server.model.game.Player;
import sep.server.model.game.Tile;

public class VirusDamage extends ADamageCard {
    public VirusDamage(String cardType) {
        super(cardType);
        this.cardType = "VirusDamage";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {

        int RADIUS = 6;
        Tile centerTile = player.getPlayerRobot().getCurrentTile();

        for(Player p : player.getGameMode().getPlayers()) {
            if (player != p) {
                Tile otherTile = p.getPlayerRobot().getCurrentTile();
                if (getDistanceBetweenTwoRobots(centerTile, otherTile) <= RADIUS) {
                    if(!p.getGameMode().getVirusDeck().isEmpty()) {
                        p.getDiscardPile().add(player.getGameMode().getVirusDeck().remove(0));
                    }
                }
            }

        }
    }

    public static int getDistanceBetweenTwoRobots (Tile t1, Tile t2) {
        int xDistance = Math.abs(t1.getCoordinate().getXCoordinate() - t2.getCoordinate().getXCoordinate());
        int yDistance = Math.abs(t1.getCoordinate().getYCoordinate() - t2.getCoordinate().getYCoordinate());
        return xDistance + yDistance;
    }
}
