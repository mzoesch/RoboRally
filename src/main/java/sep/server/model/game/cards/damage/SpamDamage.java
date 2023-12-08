package sep.server.model.game.cards.damage;

import sep.server.json.game.activatingphase.ReplaceCardModel;
import sep.server.model.game.Player;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public class SpamDamage extends ADamageCard {
    public SpamDamage(String cardType) {
        super(cardType);
        this.cardType = "SpamDamage";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber)
    {
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
