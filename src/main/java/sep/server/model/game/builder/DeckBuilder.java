package sep.server.model.game.builder;

import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.damage.SpamDamage;
import sep.server.model.game.cards.damage.TrojanHorseDamage;
import sep.server.model.game.cards.damage.VirusDamage;
import sep.server.model.game.cards.damage.WormDamage;
import sep.server.model.game.cards.programming.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DeckBuilder {
    public void DeckBuilder(){

    }

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
            deck.add(new RightTurn("RightTurn"));
        }

        for(int i = 0; i < 3; i++){
            deck.add(new LeftTurn("LeftTurn"));
        }

        deck.add(new BackUp("BackUp"));

        deck.add(new PowerUp("PowerUp"));

        for(int i = 0; i < 2; i++){
            deck.add(new Again("Again"));
        }

        deck.add(new UTurn("UTurn"));

        return deck;
    }

    public ArrayList<SpamDamage> buildSpamDeck(){
        ArrayList<SpamDamage> spamDeck = new ArrayList<>();
        for(int i = 0; i < 38; i++){
            spamDeck.add(new SpamDamage("SpamDamage"));
        }
        return spamDeck;
    }

    public ArrayList<VirusDamage> buildVirusDeck(){
        ArrayList<VirusDamage> spamDeck = new ArrayList<>();
        for(int i = 0; i < 18; i++){
            spamDeck.add(new VirusDamage("VirusDamage"));
        }
        return spamDeck;
    }

    public ArrayList<TrojanHorseDamage> buildTrojanDeck(){
        ArrayList<TrojanHorseDamage> spamDeck = new ArrayList<>();
        for(int i = 0; i < 12; i++){
            spamDeck.add(new TrojanHorseDamage("TrojanHorseDamage"));
        }
        return spamDeck;
    }
    public ArrayList<WormDamage> buildWormDeck(){
        ArrayList<WormDamage> spamDeck = new ArrayList<>();
        for(int i = 0; i < 6; i++){
            spamDeck.add(new WormDamage("WormDamage"));
        }
        return spamDeck;
    }
}
