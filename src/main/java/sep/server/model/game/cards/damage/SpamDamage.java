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
        player.getAuthGameMode().getSpamDeck().add((SpamDamage) player.getCardByRegisterIndex(currentRoundNumber));
        player.getRegisters()[currentRoundNumber] = null;

        if(player.getPlayerDeck().isEmpty()){
            player.shuffleAndRefillDeck();
        }

        IPlayableCard newCard = player.getPlayerDeck().remove(0);
        player.setCardInRegister(currentRoundNumber, newCard);
        newCard.playCard(player, currentRoundNumber);

        String newCardString = ((Card) newCard).getCardType();
        player.getAuthGameMode().getSession().broadcastReplacedCard(player.getController().getPlayerID(), currentRoundNumber, newCardString);

    }
}
