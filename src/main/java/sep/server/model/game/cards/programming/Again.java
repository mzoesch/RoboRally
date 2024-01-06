package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.damage.ADamageCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

public class Again extends AProgrammingCard implements IPlayableCard {
    public Again(String cardType) {
        super(cardType);
        this.cardType = "Again";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {

        if (currentRoundNumber == 0) {
            return;
        }

        IPlayableCard[] registers = player.getRegisters();
        IPlayableCard previousRoundCard = registers[currentRoundNumber];

        // Check if the previous card is a Damage Card or a Upgrade Card
        if (previousRoundCard instanceof ADamageCard) {
            handleDamageCard(player, currentRoundNumber);
        } else if (previousRoundCard instanceof AUpgradeCard) {
            handleUpgradeCard(player,  currentRoundNumber);
        } else {
            previousRoundCard.playCard(player, currentRoundNumber); //If not, play the Card
        }
    }

    private void handleDamageCard(Player player,  int currentRoundNumber) {

        if (player.getPlayerDeck().isEmpty()) {
            player.shuffleAndRefillDeck();
        }

        IPlayableCard drawnCard = player.getPlayerDeck().remove(0);

        player.setCardInRegister(currentRoundNumber-1, drawnCard);

        drawnCard.playCard(player, currentRoundNumber - 1);
    }

    private void handleUpgradeCard(Player player,  int currentRoundNumber) {
        //... Incomplete, because the Upgrade Cards still missing.
    }




}


