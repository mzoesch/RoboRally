package sep.server.model.game.cards.damage;

import sep.server.json.game.damage.DrawDamageModel;
import sep.server.model.game.Player;
import sep.server.model.game.Tile;
import sep.server.model.game.cards.Card;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.cards.IPlayableCard;

public class VirusDamage extends ADamageCard {
    public VirusDamage(String cardType) {
        super(cardType);
        this.cardType = "VirusDamage";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {

        int RADIUS = 6;
        Tile centerTile = player.getPlayerRobot().getCurrentTile();

        for(Player p : player.getAuthGameMode().getPlayers()) {
            if (player != p) {
                Tile otherTile = p.getPlayerRobot().getCurrentTile();
                if (getDistanceBetweenTwoRobots(centerTile, otherTile) <= RADIUS) {
                    if(!p.getAuthGameMode().getVirusDeck().isEmpty()) {
                        p.getDiscardPile().add(player.getAuthGameMode().getVirusDeck().remove(0));

                        if (p.getController() instanceof final PlayerController pc) {

                            /* TODO Move to interface if agent needs an implementation else to pc! */
                    new DrawDamageModel(pc.getClientInstance(), pc.getPlayerID(), new String[]{"Virus"}).send();
                        }

                        else { /* TODO There is nothing to do on the client side, right? So no need for agent logic?? */}

                    }
                }
            }

        }

        player.updateRegisterAfterDamageCardWasPlayed("VirusDamage", currentRoundNumber);
    }

    public static int getDistanceBetweenTwoRobots (Tile t1, Tile t2) {
        int xDistance = Math.abs(t1.getCoordinate().getX() - t2.getCoordinate().getX());
        int yDistance = Math.abs(t1.getCoordinate().getY() - t2.getCoordinate().getY());
        return xDistance + yDistance;
    }
}
