package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.damage.ADamageCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

public class Again extends AProgrammingCard implements IPlayableCard {
    public Again(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {

        Robot robot = player.getPlayerRobot();
        IPlayableCard[] registers = player.getRegisters();
        IPlayableCard previousRoundCard = registers[currentRoundNumber - 2]; //-2 because the currentRoundNumber is numbered from 1 to 5

        //Check if the currentRoundNumber is higher than 1
        if (currentRoundNumber <= 1) {
            return;
        }

        // Check if the previous card is a Damage Card or a Upgrade Card
        if (previousRoundCard instanceof ADamageCard) {
            handleDamageCard(player, currentRoundNumber);
        } else if (previousRoundCard instanceof AUpgradeCard) {
            handleUpgradeCard(player,  currentRoundNumber);
        } else {
            playCard(player, currentRoundNumber - 2); //If not, play the Card in the last register.
        }
    }

    private void handleDamageCard(Player player,  int currentRoundNumber) {

        if (player.getPlayerDeck().isEmpty()) {
            player.shuffleAndRefillDeck();
        }

        IPlayableCard drawnCard = player.getPlayerDeck().remove(0);

        player.getRegisters()[currentRoundNumber - 2] = (IPlayableCard) drawnCard;

        playCard(player, currentRoundNumber - 2);
    }

    private void handleUpgradeCard(Player player,  int currentRoundNumber) {
        //... Incomplete, because the Upgrade Cards still missing.
    }




}


