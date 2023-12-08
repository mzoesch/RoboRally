package sep.server.model.game.cards.damage;

import sep.server.json.game.activatingphase.ReplaceCardModel;
import sep.server.json.game.damage.DrawDamageModel;
import sep.server.model.game.Player;
import sep.server.model.game.Tile;
import sep.server.model.game.cards.Card;
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

        for(Player p : player.getGameMode().getPlayers()) {
            if (player != p) {
                Tile otherTile = p.getPlayerRobot().getCurrentTile();
                if (getDistanceBetweenTwoRobots(centerTile, otherTile) <= RADIUS) {
                    if(!p.getGameMode().getVirusDeck().isEmpty()) {
                        p.getDiscardPile().add(player.getGameMode().getVirusDeck().remove(0));

                        new DrawDamageModel(p.getPlayerController().getClientInstance(), p.getPlayerController().getPlayerID(), new String[]{"Virus"}).send();

                    }
                }
            }

        }

        //Karten akutalisieren
        player.getGameMode().getVirusDeck().add((VirusDamage) player.getCardByRegisterIndex(currentRoundNumber));
        player.getRegisters()[currentRoundNumber] = null;

        if(player.getPlayerDeck().isEmpty()){
            player.shuffleAndRefillDeck();
        }

        IPlayableCard newCard = player.getPlayerDeck().remove(0);
        player.setCardInRegister(currentRoundNumber, newCard);
        newCard.playCard(player, currentRoundNumber);

        String newCardString = ((Card) newCard).getCardType();
        new ReplaceCardModel(player.getPlayerController().getClientInstance(),
                currentRoundNumber, player.getPlayerController().getPlayerID(),
                newCardString).send();
    }

    public static int getDistanceBetweenTwoRobots (Tile t1, Tile t2) {
        int xDistance = Math.abs(t1.getCoordinate().getX() - t2.getCoordinate().getX());
        int yDistance = Math.abs(t1.getCoordinate().getY() - t2.getCoordinate().getY());
        return xDistance + yDistance;
    }
}
