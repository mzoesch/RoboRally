package sep.server.model.game.builder;

import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.programming.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DeckBuilder {
    public ArrayList<IPlayableCard> DeckBuilder(){
        return buildProgrammingDeck();
    }

    private ArrayList<IPlayableCard> buildProgrammingDeck(){
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
}
