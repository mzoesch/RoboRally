package sep.server.model.game.cards.damage;

import sep.server.json.game.activatingphase.ReplaceCardModel;
import sep.server.model.game.Player;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public class TrojanHorseDamage extends ADamageCard {
    public TrojanHorseDamage(String cardType) {
        super(cardType);
        this.cardType = "TrojanHorseDamage";
    }
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        if(!player.getGameMode().getSpamDeck().isEmpty()) {
            player.getDiscardPile().add(player.getGameMode().getSpamDeck().remove(0));
        }
        if(!player.getGameMode().getSpamDeck().isEmpty()) {
            player.getDiscardPile().add(player.getGameMode().getSpamDeck().remove(0));
        }

        //Karten akutalisieren
        player.getGameMode().getTrojanDeck().add((TrojanHorseDamage) player.getCardByRegisterIndex(currentRoundNumber));
        player.getRegisters()[currentRoundNumber] = null;

        IPlayableCard topCardFromDiscardPile = player.getPlayerDeck().get(0);
        player.setCardInRegister(currentRoundNumber, topCardFromDiscardPile);
        topCardFromDiscardPile.playCard(player, currentRoundNumber);

        String newCard = ((Card) topCardFromDiscardPile).getCardType();
        new ReplaceCardModel(player.getPlayerController().getClientInstance(),
                currentRoundNumber, player.getPlayerController().getPlayerID(),
                newCard).send();
    }
}
