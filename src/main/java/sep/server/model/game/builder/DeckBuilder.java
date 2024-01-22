package sep.server.model.game.builder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.damage.SpamDamage;
import sep.server.model.game.cards.damage.TrojanHorseDamage;
import sep.server.model.game.cards.damage.VirusDamage;
import sep.server.model.game.cards.damage.WormDamage;
import sep.server.model.game.cards.programming.*;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.cards.upgrade.PermanentUpgrade.AdminPrivilege;
import sep.server.model.game.cards.upgrade.PermanentUpgrade.RearLaser;
import sep.server.model.game.cards.upgrade.TemporaryUpgrade.MemorySwap;
import sep.server.model.game.cards.upgrade.TemporaryUpgrade.SpamBlocker;

import java.util.ArrayList;

/**
 * Class responsible for building different types of card decks.
 */
public class DeckBuilder {
    private static final Logger l = LogManager.getLogger(GameState.class);
    public DeckBuilder(){}

    /**
     * Builds a programming deck containing move and turn cards.
     *
     * @return ArrayList of IPlayableCard representing the programming deck
     */
    public ArrayList<IPlayableCard> buildProgrammingDeck(){
        ArrayList<IPlayableCard> deck = new ArrayList<>();

        for(int i = 0; i < 5; i++){
            deck.add(new MoveI("MoveI"));
        }

        for(int i = 0; i < 3; i++){
            deck.add(new MoveII("MoveII"));
        }

        deck.add(new MoveIII("MoveIII"));

        for(int i = 0; i < 3; i++){
            deck.add(new TurnRight("TurnRight"));
        }

        for(int i = 0; i < 3; i++){
            deck.add(new TurnLeft("TurnLeft"));
        }

        deck.add(new BackUp("BackUp"));

        deck.add(new PowerUp("PowerUp"));

        for(int i = 0; i < 2; i++){
            deck.add(new Again("Again"));
        }

        deck.add(new UTurn("UTurn"));

        return deck;
    }

    /**
     * Builds a deck containing SpamDamage cards.
     *
     * @return ArrayList of SpamDamage representing the Spam deck
     */
    public ArrayList<SpamDamage> buildSpamDeck(){
        ArrayList<SpamDamage> spamDeck = new ArrayList<>();
        for(int i = 0; i < 38; i++){
            spamDeck.add(new SpamDamage("SpamDamage"));
        }
        return spamDeck;
    }

    /**
     * Builds a deck containing VirusDamage cards.
     *
     * @return ArrayList of VirusDamage representing the Virus deck
     */
    public ArrayList<VirusDamage> buildVirusDeck(){
        ArrayList<VirusDamage> virusDeck = new ArrayList<>();
        for(int i = 0; i < 18; i++){
            virusDeck.add(new VirusDamage("VirusDamage"));
        }
        return virusDeck;
    }

    /**
     * Builds a deck containing TrojanHorseDamage cards.
     *
     * @return ArrayList of TrojanHorseDamage representing the Trojan Horse deck
     */
    public ArrayList<TrojanHorseDamage> buildTrojanDeck(){
        ArrayList<TrojanHorseDamage> trojanDeck = new ArrayList<>();
        for(int i = 0; i < 12; i++){
            trojanDeck.add(new TrojanHorseDamage("TrojanHorseDamage"));
        }
        return trojanDeck;
    }

    /**
     * Builds a deck containing WormDamage cards.
     *
     * @return ArrayList of WormDamage representing the Worm deck
     */
    public ArrayList<WormDamage> buildWormDeck(){
        ArrayList<WormDamage> spamDeck = new ArrayList<>();
        for(int i = 0; i < 6; i++){
            spamDeck.add(new WormDamage("WormDamage"));
        }
        return spamDeck;
    }

    /**
     * Builds a deck containing various upgrade cards.
     *
     * @return ArrayList of AUpgradeCard representing the upgrade deck
     */
    public ArrayList<AUpgradeCard> buildUpgradeDeck(){
        ArrayList<AUpgradeCard> upgradeDeck = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            upgradeDeck.add(new AdminPrivilege("AdminPrivilege", 3));
            upgradeDeck.add(new RearLaser("RearLaser", 2));
            upgradeDeck.add(new MemorySwap("MemorySwap", 1));
            upgradeDeck.add(new SpamBlocker("SpamBlocker", 3));
        }
        return upgradeDeck;
    }
}
