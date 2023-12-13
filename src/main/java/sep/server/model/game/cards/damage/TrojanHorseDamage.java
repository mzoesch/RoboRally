package sep.server.model.game.cards.damage;

import sep.server.json.game.activatingphase.ReplaceCardModel;
import sep.server.json.game.damage.DrawDamageModel;
import sep.server.model.game.Player;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.viewmodel.PlayerController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrojanHorseDamage extends ADamageCard {

    private final static Logger l = LogManager.getLogger(TrojanHorseDamage.class);

    public TrojanHorseDamage(String cardType) {
        super(cardType);
        this.cardType = "TrojanHorseDamage";
    }
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        if(!player.getAuthGameMode().getSpamDeck().isEmpty()) {
            player.getDiscardPile().add(player.getAuthGameMode().getSpamDeck().remove(0));
        }
        if(!player.getAuthGameMode().getSpamDeck().isEmpty()) {
            player.getDiscardPile().add(player.getAuthGameMode().getSpamDeck().remove(0));
        }

        if (player.getController() instanceof PlayerController pc)
        {
            new DrawDamageModel(pc.getClientInstance(), player.getController().getPlayerID(), new String[]{"Spam", "Spam"}).send();
        }
        else
        {
            l.error("Agent draw damage not implemented yet.");
        }


        /* TODO This will not work. All below must be in a separate callback method */

        //Karten akutalisieren
        player.getAuthGameMode().getTrojanDeck().add((TrojanHorseDamage) player.getCardByRegisterIndex(currentRoundNumber));
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
