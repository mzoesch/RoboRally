package sep.server.model.game.cards.damage;

import sep.server.json.game.activatingphase.ReplaceCardModel;
import sep.server.json.game.damage.DrawDamageModel;
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

        new DrawDamageModel(player.getPlayerController().getClientInstance(), player.getPlayerController().getPlayerID(), new String[]{"Spam", "Spam"}).send();



        //Karten akutalisieren
        player.getGameMode().getTrojanDeck().add((TrojanHorseDamage) player.getCardByRegisterIndex(currentRoundNumber));
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
}
