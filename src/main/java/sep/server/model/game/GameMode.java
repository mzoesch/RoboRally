package sep.server.model.game;

import sep.Types;
import sep.server.json.common.ErrorMsgModel;
import sep.server.json.game.damage.DrawDamageModel;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.tiles.*;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.builder.DeckBuilder;
import sep.server.model.game.cards.damage.*;
import sep.server.viewmodel.Session;
import sep.server.model.IOwnershipable;
import sep.server.model.Agent;
import sep.server.viewmodel.EServerInstance;
import java.util.*;
import sep.server.model.game.cards.damage.SpamDamage;
import sep.server.json.game.programmingphase.YourCardsModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.stream.Collectors;

/**
 * The rules of the game are implemented here. It is a high-level manager object for one game and controls the
 * overall flow of it. It is spawned at the start of a game and not destroyed until the game has ended.
 */
public class GameMode {

    private static final Logger l = LogManager.getLogger(Session.class);

    public static final int STARTING_ENERGY = 5;
    private static final int NEW_PROGRAMMING_CARDS = 9;
    private static final int REGISTER_PHASE_COUNT = 5;

    private final Course course;
    private final int availableCheckPoints;
    private final GameState gameState;
    private EGamePhase gamePhase;

    private final ArrayList<SpamDamage> spamCardDeck;
    private final ArrayList<TrojanHorseDamage> trojanCardDeck;
    private final ArrayList<VirusDamage> virusCardDeck;
    private final ArrayList<WormDamage> wormDamageDeck;
    private final ArrayList<AUpgradeCard> upgradeDeck;

    private final ArrayList<Player> players;
    private Player curPlayerInRegistration;

    private int currentRegisterIndex;

    private int energyBank;
    private final ArrayList<AUpgradeCard> upgradeShop;

    private Thread activationPhaseThread;

    private boolean bFirstRound;

    private final ArrayList<Player> upgradePhasePlayersSortedByPriority;

    private Thread programmingPhaseTimerService;

    public GameMode(final String courseName, final GameState gameState) {
        super();

        l.debug("Starting game with the following course: {}", courseName);

        this.course = new Course(courseName);
        this.availableCheckPoints = this.getAvailableCheckpoints(courseName);
        this.gameState = gameState;
        this.gamePhase = EGamePhase.INVALID;

        DeckBuilder deckBuilder = new DeckBuilder();
        this.spamCardDeck = deckBuilder.buildSpamDeck();
        this.trojanCardDeck = deckBuilder.buildTrojanDeck();
        this.virusCardDeck = deckBuilder.buildVirusDeck();
        this.wormDamageDeck = deckBuilder.buildWormDeck();

        this.upgradeDeck = deckBuilder.buildUpgradeDeck();
        Collections.shuffle(this.upgradeDeck);
        this.upgradeShop = new ArrayList<AUpgradeCard>();

        this.energyBank = 48;

        this.players = Arrays.stream(this.getControllers()).map(ctrl -> new Player(ctrl, this.course)).collect(Collectors.toCollection(ArrayList::new));
        Arrays.stream(this.getControllers()).forEach(ctrl -> this.players.stream().filter(p -> p.getController() == ctrl).findFirst().ifPresent(ctrl::setPlayer));
        this.curPlayerInRegistration = this.players.get(0);
        this.currentRegisterIndex = 0;

        this.bFirstRound = true;

        this.upgradePhasePlayersSortedByPriority = new ArrayList<Player>();

        this.handleNewPhase(EGamePhase.REGISTRATION);

        this.programmingPhaseTimerService = null;

    }

    // region Game Phases

    // region Registration Phase Helpers

    /**
     * The following method checks if the starting point selection has been finished.
     *
     * @return true if finished, false if not finished
     */
    private boolean startingPointSelectionFinished() {
        for (Player player : players) {
            if (player.getPlayerRobot().getCurrentTile() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * The following method is used to set a starting point (as long it is a valid one).
     * Afterward, next player for choosing a starting point is chosen or, if not possible, the phase is ended.
     *
     * @param ctrl player that wants to set a starting point
     * @param x    x coordinate of the starting point
     * @param y    y coordinate of the starting point
     */
    public synchronized void setStartingPoint(IOwnershipable ctrl, int x, int y) {
        if (ableToSetStartPoint(ctrl)) {

            int validation = curPlayerInRegistration.getPlayerRobot().validStartingPoint(x, y);
            if (validation == 1) {

                curPlayerInRegistration.getPlayerRobot().setStartingPoint(x, y);
                l.info("StartingPointSelected from PlayerID: " + ctrl.getPlayerID() + " with Coordinates: " + x + " , " + y);
                ctrl.getAuthGameMode().getSession().broadcastSelectedStartingPoint(ctrl.getPlayerID(), x, y);

                if (startingPointSelectionFinished()) {
                    l.debug("Registration Phase has concluded. Upgrade Phase must be started.");
                    this.handleNewPhase(EGamePhase.UPGRADE);

                } else {
                    for (Player player : players) {
                        if (player.getPlayerRobot().getCurrentTile() == null) {
                            curPlayerInRegistration = player;
                            l.info("Now Player with ID: " + player.getController().getPlayerID() + " has to set StartingPoint");
                            ctrl.getAuthGameMode().getSession().broadcastCurrentPlayer(player.getController().getPlayerID());
                            if (player.getController() instanceof final Agent a) {
                                a.evaluateStartingPoint();
                            }
                            return;
                        }
                    }
                }
            } else {
                l.warn("StartingPointSelection failed. Error Code from method validStartingPoint(): " + validation);
                if (ctrl instanceof PlayerController pc) {
                    new ErrorMsgModel(pc.getClientInstance(), "StartingPointSelection failed");
                } else {
                    l.error("The agent {} tried to do something illegal. Starting point selection failed.", ctrl.getPlayerID());
                }
            }
        }
    }

    /**
     * Checks if setting a starting point is possible and prints corresponding error messages.
     *
     * @param ctrl Player that wants to set a starting point
     * @return true if possible, false if not possible
     */
    public boolean ableToSetStartPoint(IOwnershipable ctrl) {
        if (gamePhase != EGamePhase.REGISTRATION) {
            l.debug("Unable to set StartPoint due to wrong GamePhase");
            if (ctrl instanceof PlayerController pc) {
                new ErrorMsgModel(pc.getClientInstance(), "Wrong GamePhase");
            } else {
                l.error("The agent {} tried to do something illegal.", ctrl.getPlayerID());
            }
            return false;

        } else if (ctrl.getPlayerID() != curPlayerInRegistration.getController().getPlayerID()) {
            l.error("Unable to set StartPoint due to wrong Player. Choosing Player is not currentPlayer. [CurrentPlayer: {}, ChoosingPlayer: {}]", curPlayerInRegistration.getController().getPlayerID(), ctrl.getPlayerID());
            if (ctrl instanceof PlayerController pc) {
                new ErrorMsgModel(pc.getClientInstance(), "Your are not CurrentPlayer");
            } else {
                l.error("The agent {} tried to do something illegal.", ctrl.getPlayerID());
            }
            return false;

        } else {
            return true;
        }
    }

    // endregion Registration Phase Helpers

    /**
     * The following method triggers the registration phase.
     */
    private void triggerRegistrationPhase() {
        this.getSession().broadcastGameStart(this.course.getCourse());

        /* The current player is the first player that joined the game. */
        this.getSession().broadcastCurrentPlayer(this.curPlayerInRegistration.getController().getPlayerID());

        if (this.curPlayerInRegistration.getController() instanceof final Agent a) {
            l.fatal("An agent must never be the first current player in a game. Fault agent ID: {}.", a.getPlayerID());
            EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
            return;
        }

        l.debug("Registration Phase started. Waiting for players to set their starting positions . . .");
    }

    //region Upgrade Phase helpers

    private static final class RefilledCards
    {
        public final ArrayList<String> out;
        public RefilledCards() { super(); this.out = new ArrayList<String>();
        }
    }

    /**
     * Sets up the upgrade shop by either refilling it or exchanging upgrade slots.
     *
     * @return True if the upgrade shop was exchanged, false otherwise.
     */
    private boolean setupUpgradeShop(final RefilledCards outRefilledCards)
    {
        if (!this.isUpgradeShopRightSize())
        {
            l.warn("The upgrade shop is not the right size. Expected {}, got {}. Adjusting upgrade shop.", this.players.size(), this.upgradeShop.size());

            for (final AUpgradeCard upgradeCard : this.upgradeShop)
            {
                if (upgradeCard == null)
                {
                    continue;
                }

                this.upgradeDeck.add(upgradeCard);

                continue;
            }

            this.upgradeShop.clear();

            for (int i = 0; i < this.players.size(); ++i)
            {
                this.upgradeShop.add(null);
                continue;
            }

            l.debug("Adjust upgrade shop size to {}.", this.upgradeShop.size());
        }

        final boolean bExchange = !this.isACardMissingInUpgradeShop();

        if (bExchange)
        {
            l.debug("No upgrade cards were bought last phase. Exchanging upgrade slots. Old shop: {}.", this.upgradeShop.toString());
        }
        else
        {
            l.debug("Upgrade cards were bought last phase. Refilling upgrade shop. Old shop: {}.", this.upgradeShop.toString());
        }

        if (this.isACardMissingInUpgradeShop())
        {
            this.refillUpgradeShop(outRefilledCards);
        }
        else
        {
            this.exchangeUpgradeSlots();
        }

        return bExchange;
    }

    private boolean isACardMissingInUpgradeShop()
    {
        for (final AUpgradeCard upgradeCard : this.upgradeShop)
        {
            if (upgradeCard == null)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Refills the upgrade shop with new upgrade cards.
     */
    private void refillUpgradeShop(final RefilledCards outRefilledCards)
    {
        for (int i = 0; i < this.upgradeShop.size(); ++i)
        {
            if (this.upgradeShop.get(i) == null)
            {
                if (this.upgradeDeck.get(0) == null)
                {
                    l.error("Something went wrong while refilling the upgrade shop. The upgrade deck is: {}.", this.upgradeDeck.toString());
                    EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
                    return;
                }

                this.upgradeShop.set(i, this.upgradeDeck.get(0));
                this.upgradeDeck.remove(0);

                outRefilledCards.out.add(this.upgradeShop.get(i).getCardType());

                l.debug("Refilled upgrade shop at index {} with {}.", i, this.upgradeShop.get(i).toString());
            }

            continue;
        }

        l.info("Upgrade shop refilled. The new cards are: {}.", this.upgradeShop.toString());

    }

    /**
     * Exchanges upgrade slots in the upgrade shop.
     */
    private void exchangeUpgradeSlots()
    {
        for (int i = 0; i < this.upgradeShop.size(); ++i)
        {
            if (this.upgradeShop.get(i) != null)
            {
                this.upgradeDeck.add(this.upgradeShop.get(i));
                this.upgradeShop.set(i, null);
            }

            this.upgradeShop.set(i, this.upgradeDeck.get(0));
            this.upgradeDeck.remove(0);

            l.debug("Refilled upgrade shop at index {} with {}.", i, upgradeShop.get(i).toString());
        }

        l.info("Cards in upgrade shop slots exchanged. The new cards are: {}.", upgradeShop.toString());

    }

    private void removeCardFromShop(final String card)
    {
        for (int i = 0; i < this.upgradeShop.size(); ++i)
        {
            if (this.upgradeShop.get(i) == null)
            {
                continue;
            }

            if (this.upgradeShop.get(i).getCardType().equals(card))
            {
                this.upgradeShop.set(i, null);
                break;
            }

            continue;
        }

    }

    //endregion Upgrade Phase helpers

    /**
     * The following method triggers the upgrade phase.
     */
    private void triggerUpgradePhase()
    {
        final RefilledCards     out         = new RefilledCards();
        final boolean           bExchanged  = this.setupUpgradeShop(out);

        /* We always send the refill shop request to the client in the first round. */
        if (this.bFirstRound)
        {
            this.getSession().broadcastShopRefill(this.upgradeShop.stream().filter(Objects::nonNull).map(AUpgradeCard::getCardType).collect(Collectors.toCollection(ArrayList::new)));
            this.bFirstRound = false;
        }
        else
        {
            if (bExchanged)
            {
                this.getSession().broadcastShopExchange(this.upgradeShop.stream().filter(Objects::nonNull).map(AUpgradeCard::getCardType).collect(Collectors.toCollection(ArrayList::new)));
            }
            else
            {
                this.getSession().broadcastShopRefill(out.out);
            }
        }

        this.evaluateUpgradePhasePriorities();
        this.getSession().broadcastCurrentPlayer(this.upgradePhasePlayersSortedByPriority.get(0).getController().getPlayerID());
        this.upgradePhasePlayersSortedByPriority.remove(0);

    }

    private void executePostBuyBehavior()
    {
        if (!this.upgradePhasePlayersSortedByPriority.isEmpty())
        {
            l.debug("Waiting for client {} to finish their upgrade phase.", this.upgradePhasePlayersSortedByPriority.get(0).getController().getPlayerID());
            this.getSession().broadcastCurrentPlayer(this.upgradePhasePlayersSortedByPriority.get(0).getController().getPlayerID());
            this.upgradePhasePlayersSortedByPriority.remove(0);

            return;
        }

        this.handleNewPhase(EGamePhase.PROGRAMMING);

    }

    public void onUpgradeCardBought(final PlayerController pc, final String card)
    {
        if (card == null)
        {
            l.debug("Player {} bought nothing. Continuing.", pc.getPlayerID());
            this.executePostBuyBehavior();
            return;
        }

        if (!this.doesUpgradeShopContains(card))
        {
            l.error("Player {} tried to buy a card that is not in the upgrade shop. They requested {} but the upgrade shop is: {}. Ignoring.", pc.getPlayerID(), card, this.upgradeShop.toString());
            new ErrorMsgModel(pc.getClientInstance(), String.format("The card %s you tried to buy is not in the upgrade shop.", card));
            this.executePostBuyBehavior();
            return;
        }

        if (this.getUpgradeCardCost(card) == -1)
        {
            l.error("Player {} tried to buy a card that is not in the upgrade shop. They requested {} but the upgrade shop is: {}. Ignoring", pc.getPlayerID(), card, this.upgradeShop.toString());
            new ErrorMsgModel(pc.getClientInstance(), String.format("The card %s you tried to buy is not in the upgrade shop.", card));
            this.executePostBuyBehavior();
            return;
        }

        if (pc.getPlayer().getEnergyCollected() < this.getUpgradeCardCost(card))
        {
            l.error("Player {} tried to buy a card but does not have enough energy. They had {} but needed {}.", pc.getPlayerID(), pc.getPlayer().getEnergyCollected(), this.getUpgradeCardCost(card));
            new ErrorMsgModel(pc.getClientInstance(), String.format("You need %d energy to buy %s.", this.getUpgradeCardCost(card), card));
            this.executePostBuyBehavior();
            return;
        }

        final int cost = this.getUpgradeCardCost(card);
        this.removeCardFromShop(card);
        pc.getPlayer().getBoughtUpgradeCards().add(card);
        pc.getPlayer().setEnergyCollected(pc.getPlayer().getEnergyCollected() - cost);
        this.getSession().broadcastBoughtUpgradeCard(pc.getPlayerID(), card);
        l.debug("Player {} bought {}. The new upgrade shop is: {}.", pc.getPlayerID(), card, this.upgradeShop.toString());
        this.getSession().broadcastEnergyUpdate(pc.getPlayerID(), pc.getPlayer().getEnergyCollected(), "EnergySpent");
        this.executePostBuyBehavior();

    }

    public void onCardPlayed(final PlayerController pc, final String card)
    {
        /* TODO Some validation is required. A player may not always play a card at any given time. */

        if (Objects.equals(card, "MemorySwap"))
        {
            this.getSession().broadcastPlayedCard(pc.getPlayerID(), card);

            /* The three new cards the client will get. */
            final ArrayList<String> newCards = new ArrayList<String>();
            for (int i = 0; i < 3; ++i)
            {
                if (pc.getPlayer().getPlayerDeck().isEmpty())
                {
                    pc.getPlayer().shuffleAndRefillDeck();
                }

                newCards.add(pc.getPlayer().getPlayerDeck().get(0).getCardType());
                pc.getPlayer().getPlayerHand().add(pc.getPlayer().getPlayerDeck().remove(0));
                continue;
            }

            l.debug("Player {} played Memory Swap. The new cards are: {}. Their current hand is now: {}.", pc.getPlayerID(), newCards.toString(), pc.getPlayer().getPlayerHand());

            pc.getSession().sendHandCardsToPlayer(pc, newCards.toArray(new String[0]));

            pc.getPlayer().getBoughtUpgradeCards().remove("MemorySwap");
            l.debug("Player {}'s Memory Swap card was removed from their bought upgrade cards because it is a one-time use card. Their bought upgrade cards are now: {}.", pc.getPlayerID(), pc.getPlayer().getBoughtUpgradeCards().toString());

            return;
        }

        if (Objects.equals(card, "SpamBlocker"))
        {
            this.getSession().broadcastPlayedCard(pc.getPlayerID(), card);

            final ArrayList<IPlayableCard> spam = new ArrayList<IPlayableCard>();

            for (int i = 0; i < pc.getPlayer().getPlayerHand().size(); ++i)
            {
                final IPlayableCard cursor = pc.getPlayer().getPlayerHand().get(i);

                if (cursor instanceof final SpamDamage sd)
                {
                    spam.add(sd);
                }

                continue;
            }

            pc.getPlayer().getBoughtUpgradeCards().remove("SpamBlocker");
            l.debug("Player {}'s Spam Blocker card was removed from their bought upgrade cards because it is a one-time use card. Their bought upgrade cards are now: {}.", pc.getPlayerID(), pc.getPlayer().getBoughtUpgradeCards().toString());

            if (spam.isEmpty())
            {
                l.debug("Player {} played Spam Blocker but had no spam cards in their hand. Sending empty programming cards model.", pc.getPlayerID());
                new YourCardsModel(pc.getClientInstance(), new String[0]).send();
                return;
            }

            final ArrayList<IPlayableCard> newCards = new ArrayList<IPlayableCard>();

            for (final IPlayableCard sd : spam)
            {
                pc.getPlayer().getPlayerHand().remove(sd);
                pc.getAuthGameMode().getSpamDeck().add( (SpamDamage) sd);

                if (pc.getPlayer().getPlayerDeck().isEmpty())
                {
                    pc.getPlayer().shuffleAndRefillDeck();
                }

                newCards.add(pc.getPlayer().getPlayerDeck().get(0));

                pc.getPlayer().getPlayerHand().add(pc.getPlayer().getPlayerDeck().remove(0));

                continue;
            }

            if (pc.getPlayer().getPlayerHand().size() > 9)
            {
                l.fatal("Player {} has more than 9 cards in their hand after Spam Blocker behavior execution. Their hand is: {}.", pc.getPlayerID(), pc.getPlayer().getPlayerHand().toString());
                EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
                return;
            }

            l.debug("Player {} played Spam Blocker. The new cards are: {}. Their current hand is now: {}.", pc.getPlayerID(), newCards.toString(), pc.getPlayer().getPlayerHand());
            new YourCardsModel(pc.getClientInstance(), newCards.stream().map(IPlayableCard::getCardType).toArray(String[]::new)).send();

            return;
        }

        l.error("Player {} tried to play a card that is not allowed. They tried to play {}.", pc.getPlayerID(), card);
        new ErrorMsgModel(pc.getClientInstance(), String.format("You tried to play %s but that was not allowed.", card)).send();

    }

    /**
     * The following method triggers the programming phase and prepares the player decks.
     */
    private void triggerProgrammingPhase()
    {
        for (final Player p : this.players)
        {
            p.clearOldHand();
            p.clearOldRegister();

            int maxCards = Math.min(GameMode.NEW_PROGRAMMING_CARDS, p.getPlayerDeck().size());

            for (int i = 0; i < 9; ++i)
            {
                while (true)
                {
                    if (p.getPlayerDeck().isEmpty())
                    {
                        l.debug("Player {} has no cards in their deck. Refilling it.", p.getController().getPlayerID());
                        p.shuffleAndRefillDeck();
                        this.getSession().sendShuffleCodingNotification(p.getController().getPlayerID());
                        continue;
                    }

                    if (p.getPlayerDeck().get(0) == null)
                    {
                        p.getPlayerDeck().remove(0);
                        l.warn("Player {} has a null card in their deck. Removing it. The new deck: {}.", p.getController().getPlayerID(), p.getPlayerDeck().toString());
                        continue;
                    }

                    break;
                }

                p.getPlayerHand().add(p.getPlayerDeck().remove(0));

                continue;
            }

            l.debug("Client {} has following programming cards in his Hand: {}", p.getController().getPlayerID(), Arrays.toString(p.getPlayerHandAsStringArray()));

            if (p.getController() instanceof final PlayerController pc)
            {
                pc.getSession().sendHandCardsToPlayer(pc, p.getPlayerHandAsStringArray());
                continue;
            }

            if (p.getController() instanceof final Agent a)
            {
                a.evaluateProgrammingPhase();
                continue;
            }

            l.error("No matching instance found for handling the programming phase for player {}.", p.getController().getPlayerID());

            continue;
        }

        l.debug("Programming Phase started. All remote controllers have received their cards. Waiting for them to set their cards . . .");

        return;
    }

    // region Activation Phase Helpers

    private ArrayList<Player> getOverriddenPrioritiesPlayersASC()
    {
        final ArrayList<Player> out = new ArrayList<Player>();

        for (final Player player : this.players)
        {
            if (player.getChosenRegisterAdminPrivilegeUpgrade() == null)
            {
                continue;
            }

            if (player.getChosenRegisterAdminPrivilegeUpgrade().register() == this.currentRegisterIndex)
            {
                out.add(player);
                continue;
            }

            continue;
        }

        if (out.isEmpty() || out.size() == 1)
        {
            return out;
        }

        // If multiple players have set their override request for the current register.
        // We sort all players by the time they have sent their request.
        // The player with the lowest time passed will get the highest priority.
        for (int i = 0; i < out.size(); ++i)
        {
            int min = i;

            for (int j = i + 1; j < out.size(); ++j)
            {
                if (out.get(j).getChosenRegisterAdminPrivilegeUpgrade().in() < out.get(min).getChosenRegisterAdminPrivilegeUpgrade().in())
                {
                    min = j;
                    continue;
                }

                continue;
            }

            final Player tmp = out.get(i);
            out.set(i, out.get(min));
            out.set(min, tmp);

            continue;
        }

        return out;
    }

    private void resetOverriddenPrioritiesForRegister()
    {
        for (final Player player : this.players)
        {
            if (player.getChosenRegisterAdminPrivilegeUpgrade() == null)
            {
                continue;
            }

            if (player.getChosenRegisterAdminPrivilegeUpgrade().register() == this.currentRegisterIndex)
            {
                player.setChosenRegisterAdminPrivilegeUpgrade(null);
                l.debug("Reset overridden priority for player {} for register {}.", player.getController().getPlayerID(), this.currentRegisterIndex);
                continue;
            }

            continue;
        }

    }

    /**
     * The following method calculates the priorities for all players: First the distance from each robot to the
     * antenna is calculated. Next the priorities are assigned. The closest player gets the highest priority.
     */
    private void determinePriorities()
    {
        final Coordinate antennaCoordinate = this.course.getPriorityAntennaCoordinate();
        l.debug("Determining priorities for all players. Found Priority Antenna at {}.", antennaCoordinate.toString());

        final int[] distances = new int[this.players.size()];
        for (int i = 0; i < this.players.size(); ++i)
        {
            distances[i] = Math.abs(antennaCoordinate.getX() - this.players.get(i).getPlayerRobot().getCurrentTile().getCoordinate().getX()) + Math.abs(antennaCoordinate.getY() - this.players.get(i).getPlayerRobot().getCurrentTile().getCoordinate().getY());
            continue;
        }

        int currentPriority = this.players.size();

        /* Priorities with distance calculations. */

        for (int i = 0; i < distances.length; ++i)
        {
            int min = 0;

            for (int j = 0; j < distances.length; ++j)
            {
                if (distances[j] < distances[min])
                {
                    min = j;
                    continue;
                }

                continue;
            }

            this.players.get(min).setPriority(currentPriority--);
            distances[min] = Integer.MAX_VALUE;

            continue;
        }

        /* Priorities with admin privilege cards. */

        final ArrayList<Player> overriddenPrioritiesPlayers = this.getOverriddenPrioritiesPlayersASC();

        if (overriddenPrioritiesPlayers.isEmpty())
        {
            l.debug("No players have overridden priorities. The priorities are: {}.", this.getPrioritiesAsString().toString());
            return;
        }

        l.debug("The following players have overridden priorities: {}.", overriddenPrioritiesPlayers.toString());

        int priority = 1_000;
        for (final Player player : overriddenPrioritiesPlayers)
        {
            player.setPriority(priority++);
            continue;
        }

        l.debug("The priorities for register {} are: {}.", this.currentRegisterIndex, this.getPrioritiesAsString().toString());

        this.resetOverriddenPrioritiesForRegister();

    }

    private ArrayList<String> getPrioritiesAsString()
    {
        final ArrayList<String> out = new ArrayList<String>();

        for (final Player player : this.players)
        {
            out.add(String.format("%d", player.getPriority()));
            continue;
        }

        return out;
    }

    /**
     * The following method sorts all players in the players list according to their priorities.
     */
    private void sortPlayersByPriorityInDesc() {
        this.players.sort(Comparator.comparingInt(Player::getPriority).reversed());
    }


    /**
     * Method to activate the conveyor belts in the right order (first blue, then second) and
     * broadcast the players movement to everybody
     */
    public void activateConveyorBelts() {

        activateBlueConveyorBelts();
        for (Player player : players) {
            this.getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition().getX(), player.getPosition().getY());
        }
        activateBlueConveyorBelts();
        for (Player player : players) {
            this.getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition().getX(), player.getPosition().getY());
        }
        activateGreenConveyorBelts();
        for (Player player : players) {
            this.getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition().getX(), player.getPosition().getY());
        }
    }

    /**
     * Method to activate all blue conveyor belts with a robot on top of it (only moves them one tile, so
     * this method needs to be activated twice per activation phase).
     */
    private void activateBlueConveyorBelts() {

        for (Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if (fieldType instanceof ConveyorBelt conveyorBelt) {
                    int beltSpeed = conveyorBelt.getSpeed();

                    if (beltSpeed == 2) {
                        Coordinate oldCoordinate = currentTile.getCoordinate();
                        String outDirection = conveyorBelt.getOutcomingFlowDirection();
                        Coordinate targetCoordinate = calculateNewCoordinate(outDirection, oldCoordinate);

                        if(!course.getTileByCoordinate(targetCoordinate).isOccupied()){
                            curvedArrowCheck(player, targetCoordinate, outDirection);
                            player.getPlayerRobot().moveRobotOneTile(true, outDirection, 0);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(null);
                            course.updateRobotPosition(player.getPlayerRobot(), targetCoordinate);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(player.getPlayerRobot());
                        }
                    }
                }
            }
        }
    }


    /**
     * Method to activate all green conveyor belts with a robot on top of it.
     */
    public void activateGreenConveyorBelts(){
        for (Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if (fieldType instanceof ConveyorBelt conveyorBelt) {
                    int beltSpeed = conveyorBelt.getSpeed();

                    if (beltSpeed == 1) {
                        Coordinate oldCoordinate = currentTile.getCoordinate();
                        String outDirection = conveyorBelt.getOutcomingFlowDirection();
                        Coordinate targetCoordinate = calculateNewCoordinate(outDirection, oldCoordinate);
                        curvedArrowCheck(player, targetCoordinate, outDirection);

                        if(!course.getTileByCoordinate(targetCoordinate).isOccupied()){
                            player.getPlayerRobot().moveRobotOneTile(true, outDirection, 0);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(null);
                            course.updateRobotPosition(player.getPlayerRobot(), targetCoordinate);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(player.getPlayerRobot());
                        }
                    }
                }
            }
        }
    }

    /**
     * method for moving checkpoints, if they are placed on conveyorBelts (like in course twister)
     */
    public void moveCheckpoints(){
        if(course.getMovingCheckpoints()){
            ArrayList<Coordinate> oldCheckpointCoordinates = course.getCheckpointCoordinates();

            for(Coordinate oldCoordinate : oldCheckpointCoordinates){
                //removing the checkpoint from his old tile
                Tile oldTile = course.getTileByCoordinate(oldCoordinate);
                CheckPoint checkpoint = oldTile.removeCheckpoint();

                //get the new Coordinate for the CheckPoint
                ConveyorBelt conveyorBelt = oldTile.getConveyorBelt();
                String outDirection = conveyorBelt.getOutcomingFlowDirection();
                Coordinate targetCoordinate = calculateNewCoordinate(outDirection, oldCoordinate);

                if(conveyorBelt.getSpeed()>1) {
                    ConveyorBelt nextConveyorBelt = course.getTileByCoordinate(targetCoordinate).getConveyorBelt();
                    targetCoordinate = calculateNewCoordinate(nextConveyorBelt.getOutcomingFlowDirection(), targetCoordinate);
                }

                //add the Checkpoint to the new Tile
                Tile newTile = course.getTileByCoordinate(targetCoordinate);
                newTile.addCheckPoint(checkpoint);

                this.getSession().broadcastCheckPointMoved(checkpoint.getCheckpointNumber(), targetCoordinate.getX(), targetCoordinate.getY());
            }
        }
    }

    /**
     * The following method calculates the new coordinates for activating conveyor belts and push panels.
     * @param orientation direction the robot is moved to
     * @param oldCoordinate coordinates of the current push panel pushing a robot
     * @return new coordinate
     */
    public Coordinate calculateNewCoordinate(String orientation, Coordinate oldCoordinate) {
        Coordinate newCoordinate = null;
        switch (orientation) {
            case "top" -> newCoordinate = new Coordinate(oldCoordinate.getX(),
                    oldCoordinate.getY() - 1);
            case "right" -> newCoordinate = new Coordinate(oldCoordinate.getX() + 1,
                    oldCoordinate.getY());
            case "bottom" -> newCoordinate = new Coordinate(oldCoordinate.getX(),
                    oldCoordinate.getY() + 1);
            case "left" -> newCoordinate = new Coordinate(oldCoordinate.getX() - 1,
                    oldCoordinate.getY());
        }
        return newCoordinate;
    }

    /**
     * The following method is required during the conveyor belt activation period.
     * It checks if the robot moved onto another conveyor belt tile. If yes, the method checks
     * if the new conveyor belt tile has a curved arrow by comparing the incoming flow direction
     * with the out-coming flow direction. If yes, the direction of the robot is changed accordingly
     * and the corresponding JSON message is sent.
     * @param player Owner of the current robot
     * @param coordinate Coordinate of the new tile the robot moved onto
     * @param direction Direction from which the robot was moved into the conveyor belt
     */
    public void curvedArrowCheck(Player player, Coordinate coordinate, String direction) {
        Tile newTile = course.getTileByCoordinate(coordinate);
        for(FieldType newFieldType : newTile.getFieldTypes()) {
            if(newFieldType instanceof ConveyorBelt conveyorBelt) {
                String outDirection = conveyorBelt.getOutcomingFlowDirection();

                if(direction != null && outDirection != null) {
                        if((Objects.equals(direction, "bottom") && outDirection.equals("right")) ||
                                (Objects.equals(direction, "left") && outDirection.equals("bottom")) ||
                                (Objects.equals(direction, "top") && outDirection.equals("left")) ||
                                (Objects.equals(direction, "right") && outDirection.equals("top"))) {

                            //rotate robot counterclockwise
                            player.getPlayerRobot().rotateRobotOnTileToTheLeft();

                            addDelay(3000);
                            this.getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");

                        } else if((Objects.equals(direction, "bottom") && outDirection.equals("left")) ||
                                (Objects.equals(direction, "left") && outDirection.equals("top")) ||
                                (Objects.equals(direction, "top") && outDirection.equals("right")) ||
                                (Objects.equals(direction, "right") && outDirection.equals("bottom"))) {

                            //rotate robot clockwise
                            player.getPlayerRobot().rotateRobotOnTileToTheRight();

                            this.getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");

                        }
                }
            }
        }
    }

    /**
     * The following method handles the activation of push panels and sends the corresponding JSON messages.
     * The robot is moved to the next field in the direction of the panel's pushOrientation.
     */
    private void activatePushPanels() {
        this.getSession().broadcastAnimation(EAnimation.PUSH_PANEL);

        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof PushPanel pushPanel) {
                    int[] activateAtRegister = pushPanel.getActivateAtRegister();

                    for(int register : activateAtRegister) {
                        if(register == (currentRegisterIndex +1)) {
                            l.debug("Push Panel is activated.");
                            String pushOrientation = pushPanel.getOrientation();
                            Coordinate oldCoordinate = currentTile.getCoordinate();

                            Coordinate targetCoordinate = calculateNewCoordinate(pushOrientation, oldCoordinate);

                            if (!player.getPlayerRobot().getCourse().isCoordinateWithinBounds(targetCoordinate) ||
                                    player.getPlayerRobot().getCourse().getTileByCoordinate(targetCoordinate).isPit()) {
                                l.debug("Player {}'s robot moved to {} and fell off the board. Rebooting . . .",
                                        player.getPlayerRobot().determineRobotOwner().getController().getPlayerID(),
                                        targetCoordinate.toString());
                                player.getPlayerRobot().reboot();
                                return;
                            }

                            if(player.getPlayerRobot().getCourse().getTileByCoordinate(targetCoordinate).isPit()) {
                                l.debug("Player {}'s robot moved to {} and fell down a pit. Rebooting . . .",
                                        player.getPlayerRobot().determineRobotOwner().getController().getPlayerID(),
                                        targetCoordinate.toString());
                                player.getPlayerRobot().reboot();
                                return;
                            }

                            if (!player.getPlayerRobot().isTraversable(player.getPlayerRobot().getCourse().
                                            getTileByCoordinate(oldCoordinate),
                                    player.getPlayerRobot().getCourse().getTileByCoordinate(targetCoordinate), 0)) {
                                l.debug("Player {}'s robot wanted to traverse an impassable tile [from {} to {}]. " +
                                        "Ignoring.", player.getPlayerRobot().determineRobotOwner().getController().
                                        getPlayerID(), oldCoordinate.toString(), targetCoordinate.toString());
                                return;
                            }

                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(null);
                            course.updateRobotPosition(player.getPlayerRobot(), targetCoordinate);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(player.getPlayerRobot());

                            this.getSession().broadcastPositionUpdate(player.getController().getPlayerID(), targetCoordinate.getX(), targetCoordinate.getY());
                        } else {
                            l.debug("Push Panel is not activated.");
                        }
                    }
                }
            }
        }
    }

    /**
     * The following method handles the activation of gears and sends the corresponding JSON messages.
     * The robot is rotated 90 degrees into the gear's rotational direction.
     */
    private void activateGears() {
        this.getSession().broadcastAnimation(EAnimation.GEAR);

        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof Gear gear) {
                    String rotationalDirection = gear.getRotationalDirection();
                    String robotDirection = player.getPlayerRobot().getDirection();
                    String newDirection = robotDirection;

                    if(Objects.equals(rotationalDirection, "counterclockwise")) {
                        switch (robotDirection) {
                            case "top" -> newDirection = "left";
                            case "right" -> newDirection = "top";
                            case "bottom" -> newDirection = "right";
                            case "left" -> newDirection = "bottom";
                        }
                    } else if(Objects.equals(rotationalDirection, "clockwise")) {
                        switch (robotDirection) {
                            case "top" -> newDirection = "right";
                            case "right" -> newDirection = "bottom";
                            case "bottom" -> newDirection = "left";
                            case "left" -> newDirection = "top";
                        }
                    }

                    player.getPlayerRobot().setDirection(newDirection);

                    this.getSession().broadcastRotationUpdate(player.getController().getPlayerID(), rotationalDirection);
                }
            }
        }
    }

    /**
     * The following method checks the course for lasers and passes the respective tile including its field types to
     * the handleLaserByDirection method.
     */
    private void findLasers() {
        addDelay(2000);
        this.getSession().broadcastAnimation(EAnimation.WALL_SHOOTING);

        for (ArrayList<Tile> row : course.getCourse()) {
            for (Tile tile : row) {
                for (FieldType fieldType : tile.getFieldTypes()) {
                    if (fieldType instanceof Laser) {
                        handleLaserByDirection((Laser) fieldType, tile);
                    }
                }
            }
        }
    }

    /**
     * The following method determines all parameters needed to shoot the robot lasers during the activation phase
     * and calls the handleLaserShooting method depending on the direction the robot is facing to.
     */
    private void shootRobotLasers() {
        addDelay(2000);
        this.getSession().broadcastAnimation(EAnimation.PLAYER_SHOOTING);

        for(Player player : players) {
            Robot playerRobot = player.getPlayerRobot();
            Tile robotTile = playerRobot.getCurrentTile();

            int robotTileXCoordinate = robotTile.getCoordinate().getX();
            int robotTileYCoordinate = robotTile.getCoordinate().getY();
            String robotDirection = playerRobot.getDirection();

            switch(robotDirection) {
                case "top" -> handleLaserShooting("top", 1, robotTileXCoordinate, robotTileYCoordinate -1 , 0, -1);
                case "right" -> handleLaserShooting("right", 1, robotTileXCoordinate +1  , robotTileYCoordinate, 1, 0);
                case "bottom" -> handleLaserShooting("bottom", 1, robotTileXCoordinate, robotTileYCoordinate + 1  , 0, 1);
                case "left" -> handleLaserShooting("left", 1, robotTileXCoordinate - 1 , robotTileYCoordinate, -1, 0);
            }

            if (playerRobot.getCanShootBackward()) {
                l.debug("Player {}'s robot can shoot backwards. Checking hits.", player.getController().getPlayerID());
                switch (robotDirection) {
                    case "top" -> handleLaserShooting("bottom", 1, robotTileXCoordinate, robotTileYCoordinate - 1, 0, -1);
                    case "right" -> handleLaserShooting("left", 1, robotTileXCoordinate + 1, robotTileYCoordinate, 1, 0);
                    case "bottom" -> handleLaserShooting("top", 1, robotTileXCoordinate, robotTileYCoordinate + 1, 0, 1);
                    case "left" -> handleLaserShooting("right", 1, robotTileXCoordinate - 1, robotTileYCoordinate, -1, 0);
                }
            }
        }
    }

    /**
     * The following method determines the laser orientation. Depending on the orientation it passes different values to
     * the handleLaserShooting method.
     * @param laser laser object holding laser orientation and laser strength
     * @param tile tile of current laser being handled
     */
    private void handleLaserByDirection(Laser laser, Tile tile) {
        int laserXCoordinate = tile.getCoordinate().getX();
        int laserYCoordinate = tile.getCoordinate().getY();
        String laserOrientation = laser.getOrientation();
        int laserCount = Laser.getLaserCount();


        switch (laserOrientation) {
            case "top" -> handleLaserShooting("top", laserCount, laserXCoordinate, laserYCoordinate, 0, -1);
            case "right" -> handleLaserShooting("right", laserCount, laserXCoordinate, laserYCoordinate, 1, 0);
            case "bottom" -> handleLaserShooting("bottom", laserCount, laserXCoordinate, laserYCoordinate, 0, 1);
            case "left" -> handleLaserShooting("left", laserCount, laserXCoordinate, laserYCoordinate, -1, 0);
        }
    }

    /**
     * The following method shoots the laser and checks for obstacles in its way which stop the laser.
     * If the obstacle is a robot, they draw 1-3 spam cards, depending on the laser's strength.
     * @param laserOrientation direction the laser is shooting into
     * @param laserCount intensity of the laser, can vary between 1-3
     * @param x x-coordinate of laser tile
     * @param y y-coordinate of laser tile
     * @param xIncrement differs depending on laser orientation
     * @param yIncrement differs depending on laser orientation
     */
    private void handleLaserShooting(String laserOrientation, int laserCount, int x, int y, int xIncrement, int yIncrement) {
        boolean laserGoing = true;

        while (course.areCoordinatesWithinBounds(x, y) && laserGoing) {
            Tile tile = course.getTileByNumbers(x, y);

            if (tile.isOccupied()) {
                Robot occupyingRobot = tile.getRobot();

                for (Player player : players) {
                    if (player.getPlayerRobot() == occupyingRobot) {
                        l.debug(player.getController().getName() + " got hit by a laser at " + tile.getCoordinate());

                        if(this.spamCardDeck.size() >= laserCount) {
                            for(int i=0; i<laserCount; i++) {
                                player.getDiscardPile().add(this.spamCardDeck.remove(0));
                            }
                            if (player.getController() instanceof PlayerController pc) {
                                String[] spamArray = new String[laserCount];
                                Arrays.fill(spamArray, "Spam");
                                new DrawDamageModel(pc.getClientInstance(), player.getController().getPlayerID(), spamArray).send();
                            } else {
                                l.error("Agent draw damage not implemented yet.");
                            }
                        }

                        laserGoing = false;
                        break;
                    }
                }
            }

            for (FieldType fieldType : tile.getFieldTypes()) {
                if (fieldType instanceof Wall wall) {
                    String[] orientations = wall.getOrientations();

                    for (String wallOrientation : orientations) {


                        if ((laserOrientation.equals("top") && wallOrientation.equals("top")) ||
                                (laserOrientation.equals("bottom") && wallOrientation.equals("bottom")) ||
                                (laserOrientation.equals("right") && wallOrientation.equals("right")) ||
                                (laserOrientation.equals("left") && wallOrientation.equals("left"))) {

                            laserGoing = false;
                            break;
                        }
                    }
                }

                if (fieldType instanceof Antenna) {
                    laserGoing = false;
                    break;
                }
            }

            x += xIncrement;
            y += yIncrement;
        }

    }


    /**
     * The following method checks if any robot ended their register on an energy space, if
     * they receive an energy cube, and sends the corresponding JSON messages.
     */
    private void checkEnergySpaces() {
        for (Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes())
            {
                if (fieldType instanceof final EnergySpace energySpace)
                {
                    if (energySpace.getAvailableEnergy() > 0)
                    {
                        l.debug("Player {} is on energy space {} with {} available energy and receives an energy cube.", player.getController().getPlayerID(), energySpace.getAvailableEnergy(), currentTile.getCoordinate());

                        energySpace.setAvailableEnergy(energySpace.getAvailableEnergy() - 1);
                        player.setEnergyCollected(player.getEnergyCollected() + 1);

                        this.getSession().broadcastEnergyUpdate(player.getController().getPlayerID(), player.getEnergyCollected(), "EnergySpace");

                        this.getSession().broadcastAnimation(EAnimation.ENERGY_SPACE);

                        continue;
                    }

                    // If the player lands on an empty energy space at the end of the 5. register.
                    // They pick up an energy cube from the bank.
                    if (this.currentRegisterIndex == 4)
                    {
                        l.debug("Player {} is on an empty energy space at the end of the 5. register. They receive an energy cube.", player.getController().getPlayerID());

                        player.setEnergyCollected(player.getEnergyCollected() + 1);

                        this.getSession().broadcastEnergyUpdate(player.getController().getPlayerID(), player.getEnergyCollected(), "EnergySpace");

                        continue;
                    }

                    continue;
                }
            }
        }
    }

    /**
     * The following method checks if any robot has reached a checkpoint. If yes, the method
     * checks if it is the correct checkpoint according to numerical order. If it is the last
     * checkpoint it ends the game. The method also sends the corresponding JSON message.
     */
    private void checkCheckpoints() {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof CheckPoint checkPoint) {
                    int checkpointNumber = checkPoint.getCheckpointNumber();
                    if(player.getCheckpointsCollected() == checkpointNumber-1) {
                        player.setCheckpointsCollected(player.getCheckpointsCollected()+1);
                    }

                    this.getSession().broadcastCheckPointReached(player.getController().getPlayerID(), player.getCheckpointsCollected());

                    if(player.getCheckpointsCollected() == availableCheckPoints) {
                        l.debug("Collected checkpoints: " + player.getCheckpointsCollected() +
                                ", Checkpoints needed: " + availableCheckPoints);
                        l.debug("Player has collected last checkpoint.");
                        endGame(player);
                        this.getSession().broadcastGameFinish(player.getController().getPlayerID());
                    }
                }
            }
        }
    }

    public void endGame(Player winner) {
        getSession().handleGameFinished(winner.getController().getPlayerID());
        if (this.activationPhaseThread != null)
        {
            this.activationPhaseThread.interrupt();
            this.activationPhaseThread = null;
        }
    }

    /**
     * The following method is called whenever the activation phase is ended. It empties the registers
     * and calls a method that refills the player deck.
     */
    public void endRound() {
        for(int i = 0; i<5; i++) {
            for(Player player : players) {
                player.getDiscardPile().add(player.getRegisters()[i]);
                player.getRegisters()[i] = null;
                l.debug("Resetting register {} for player {}.", i + 1, player.getController().getName());
            }
        }

        for(Player player : players) {
            player.shuffleAndRefillDeck();
            l.debug("Shuffling and refilling deck for player {}.", player.getController().getName());
        }

        for (Player player : players) {
            if (player.getHasAdminPrivilegeUpgrade()) {
                player.setChosenRegisterAdminPrivilegeUpgrade(null);
                l.debug("Player {} reset chosenRegisterAdminPrivilegeUpgrade to null.", player.getController().getName());
            }
        }
    }

    // endregion Activation Phase Helpers

    /**
     * One run of a register in the activation phase. This method must
     * be called five times in a row to complete the activation phase.
     * @return True if the activation phase should continue. False otherwise.
     */
    private boolean runActivationPhase() throws InterruptedException
    {
        l.debug("Starting register phase {}.", this.currentRegisterIndex + 1);

        this.determinePriorities();
        this.sortPlayersByPriorityInDesc();
        this.getSession().broadcastCurrentCards(this.currentRegisterIndex);

        for (Player p : this.players) {
            if (p.getRegisters()[this.currentRegisterIndex] != null) {
                l.info("Player {} is playing card {}.", p.getController().getPlayerID(), p.getRegisters()[this.currentRegisterIndex].getCardType());
                p.getRegisters()[this.currentRegisterIndex].playCard(p, this.currentRegisterIndex);
                Thread.sleep(Types.EDelay.CARD_PLAY.i);
                continue;
            }

            l.warn("Player {} does not have a card in register {}. If this was after a reboot, this can be ignored.", p.getController().getPlayerID(), this.currentRegisterIndex + 1);
        }

        addDelay(2000);
        this.activateConveyorBelts();
        this.activatePushPanels();
        this.activateGears();
        this.findLasers();
        this.shootRobotLasers();
        this.checkEnergySpaces();
        this.checkCheckpoints();
        this.moveCheckpoints();
        this.currentRegisterIndex++;

        return this.currentRegisterIndex < GameMode.REGISTER_PHASE_COUNT;
    }

    /**
     * The following method introduces a timeout. The length depends on the amount of milliseconds passed to the method.
     * @param milliseconds length of timeout
     *
     * @deprecated DO NOT USE. This method can easily crash the server. Marked as deprecated to prevent further usage.
     */
    public void addDelay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void triggerActivationPhase()
    {
        for (Player p : players) {
            l.debug("Player {} has these cards in their registers: {}.",
                    p.getController().getPlayerID(),
                    p.getRegistersAsStringArray());
        }

        if (this.activationPhaseThread != null && this.activationPhaseThread.isAlive())
        {
            l.warn("Activation Phase is already running. Skipping . . .");
            return;
        }

        this.activationPhaseThread = this.createActivationThread();
        this.activationPhaseThread.start();

    }

    /**
     * Interface for a new phase in the game. All phase updates should be called through this method.
     *
     * @param phase New phase to be set
     */
    public void handleNewPhase(EGamePhase phase) {
        l.info("Session [{}] is entering a new phase. From {} to {}.", this.getSession().getSessionID(), this.gamePhase, phase);

        this.getSession().broadcastNewGamePhase(phase);

        switch (phase) {
            case REGISTRATION -> {
                this.gamePhase = EGamePhase.REGISTRATION;
                this.triggerRegistrationPhase();
            }

            case UPGRADE -> {
                this.gamePhase = EGamePhase.UPGRADE;
                this.triggerUpgradePhase();
            }

            case PROGRAMMING -> {
                this.gamePhase = EGamePhase.PROGRAMMING;
                this.triggerProgrammingPhase();
            }

            case ACTIVATION -> {
                this.gamePhase = EGamePhase.ACTIVATION;
                this.triggerActivationPhase();
            }

            default -> this.gamePhase = EGamePhase.INVALID;
        }
    }

    // endregion Game Phases

    /**
     * Called in the method addCardToRegister in the Class Player when the players register is full
     */
    public void startProgrammingTimerService()
    {
        this.getSession().broadcastProgrammingTimerStart();

        if (this.programmingPhaseTimerService != null && this.programmingPhaseTimerService.isAlive())
        {
            l.error("Programming Phase Timer Service is already running.");
            EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
            return;
        }

        this.programmingPhaseTimerService = new Thread(() ->
        {
            final long startTime = System.currentTimeMillis();
            try
            {
                Thread.sleep(30_000);
            }
            catch (final InterruptedException e)
            {
                if (this.gameState.isClosed())
                {
                    l.debug("Programming Phase Timer Service was interrupted successfully. Not executing post programming phase behavior. Cause: Session Closed.");
                    return;
                }

                l.info("All Players have set their register cards in time. Time left [{}s].Starting next phase . . ." , ( (double) 30_000 - (System.currentTimeMillis() - startTime) ) / 1_000);
            }

            l.info("Programming Phase Timer Service was interrupted or finished waiting successfully.");

            this.executePostProgrammingPhaseTimerServiceBehavior();

        });

        this.programmingPhaseTimerService.setName(String.format("ProgrammingPhaseTimerService-%s", this.getSession().getSessionID()));
        this.programmingPhaseTimerService.start();

    }

    public void executePostProgrammingPhaseTimerServiceBehavior()
    {
        if (this.getUnfinishedPlayersDuringProgrammingPhase().length > 0)
        {
            l.info("The following players did not finish their programming phase in time: {}.", Arrays.toString(this.getUnfinishedPlayersDuringProgrammingPhase()));
            this.getSession().broadcastProgrammingTimerFinish(this.getUnfinishedPlayersDuringProgrammingPhase());
            this.discardAndDrawBlind(Arrays.stream(this.getUnfinishedPlayersDuringProgrammingPhase()).mapToObj(i -> Objects.requireNonNull(this.getSession().getOwnershipableByID(i)).getPlayer()).collect(Collectors.toCollection(ArrayList::new)));
        }

        this.programmingPhaseTimerService = null;

        this.handleNewPhase(EGamePhase.ACTIVATION);

    }

    private int[] getUnfinishedPlayersDuringProgrammingPhase()
    {
        ArrayList<Integer> unfinishedPlayers = new ArrayList<Integer>();
        for (final Player p : this.players)
        {
            if (!p.hasPlayerFinishedProgramming())
            {
                unfinishedPlayers.add(p.getController().getPlayerID());
                continue;
            }

            continue;
        }

        return unfinishedPlayers.stream().mapToInt(i -> i).toArray();
    }

    public void discardAndDrawBlind(final ArrayList<Player> players)
    {
        for (final Player p : players)
        {
            p.executeIncompleteProgrammingBehavior();
            continue;
        }

    }

    /**
     * The following method is called whenever a card in a register needs to be replaced by
     * another card (by default, top card of player deck).
     * @param player player who needs to replace their cards
     * @param card card that will be added instead
     */
    public void replaceCardInRegister(Player player, IPlayableCard card) {
        player.getDiscardPile().add(player.getCardByRegisterIndex(currentRegisterIndex));
        player.getRegisters()[currentRegisterIndex] = null;

        IPlayableCard topCardFromDiscardPile = player.getPlayerDeck().get(0);
        String newCard = ((Card) topCardFromDiscardPile).getCardType();
        player.setCardInRegister(currentRegisterIndex, topCardFromDiscardPile);

        this.getSession().broadcastReplacedCard(player.getController().getPlayerID(), currentRegisterIndex, newCard);
    }

    /* TODO Remove player after connection loss */
    public void removePlayer(final int playerID)
    {
        l.error("Removing player with ID {} from game. Not implemented yet.", playerID);
    }

    public void onClose() throws InterruptedException
    {
        if (this.activationPhaseThread != null)
        {
            this.activationPhaseThread.interrupt();
            this.activationPhaseThread.join();
            this.activationPhaseThread = null;
        }

        if (this.programmingPhaseTimerService != null)
        {
            this.programmingPhaseTimerService.interrupt();
            this.programmingPhaseTimerService.join();
            this.programmingPhaseTimerService = null;
        }

        l.debug("Game Mode of Session [{}] closed successfully.", this.getSession().getSessionID());

    }

    // region Getters and Setters

    public ArrayList<Player> getPlayers()
    {
        return this.players;
    }

    public ArrayList<SpamDamage> getSpamDeck()
    {
        return spamCardDeck;
    }

    public ArrayList<TrojanHorseDamage> getTrojanDeck()
    {
        return trojanCardDeck;
    }

    public ArrayList<VirusDamage> getVirusDeck()
    {
        return virusCardDeck;
    }

    public ArrayList<WormDamage> getWormDeck()
    {
        return wormDamageDeck;
    }

    public int getAvailableCheckpoints(final String courseName) {
        return switch (courseName) {
            case "Dizzy Highway", "DizzyHighway" -> 1;
            case "Extra Crispy", "ExtraCrispy", "Lost Bearings", "LostBearings" -> 4;
            case "Death Trap", "DeathTrap" -> 5;
            default -> 0;
        };
    }

    public PlayerController[] getRemotePlayers()
    {
       return this.gameState.getSession().getRemotePlayers().toArray(new PlayerController[0]);
    }

    public IOwnershipable[] getControllers()
    {
        return this.gameState.getControllers();
    }

    public Session getSession()
    {
        return this.gameState.getSession();
    }

    public int getEnergyBank() {
        return energyBank;
    }

    public void setEnergyBank(int energyBank) {
        this.energyBank = energyBank;
    }

    private Thread createActivationThread()
    {

        final Thread t = new Thread(
        () ->
        {
            l.debug("Activation Phase started.");

            this.currentRegisterIndex = 0;

            try
            {
                while (this.runActivationPhase())
                {
                    l.debug("Register {} in Activation Phase ended. Waiting 5s for the next register iteration . . .", this.currentRegisterIndex);

                    //noinspection BusyWait
                    Thread.sleep(Types.EDelay.REGISTER_PHASE_ITERATION.i);

                    continue;
                }

                this.endRound();

                l.debug("Activation Phase ended successfully. Waiting 2s for the next phase . . .");

                Thread.sleep(Types.EDelay.PHASE_CHANGE.i);
            }
            catch (final InterruptedException e)
            {
                l.warn("Activation Phase was interrupted. If this was during session close, this can be ignored.");
                l.warn(e.getMessage());
                return;
            }

            this.handleNewPhase(EGamePhase.UPGRADE);
            this.activationPhaseThread = null;

        });

        t.setName(String.format("PhaseIIIService-%s", this.getSession().getSessionID()));

        return t;
    }

    private void evaluateUpgradePhasePriorities()
    {
        l.debug("Determining priority for all players during this upgrade phase.");
        this.upgradePhasePlayersSortedByPriority.clear();

        final Coordinate antennaLocation = this.course.getPriorityAntennaCoordinate();

        final int[] distances = new int[this.players.size()];
        for (int i = 0; i < this.players.size(); ++i)
        {
            final Coordinate    robotCoordinate     = this.players.get(i).getPlayerRobot().getCurrentTile().getCoordinate();
                                distances[i]        = Math.abs(antennaLocation.getX() - robotCoordinate.getX()) + Math.abs(antennaLocation.getY() - robotCoordinate.getY());
            continue;
        }

        int currentPriority = this.players.size();
        for (int i = 0; i < this.players.size(); ++i)
        {
            int     minDistance     = Integer.MAX_VALUE;
            int     minIndex        = -1;

            for (int j = 0; j < distances.length; ++j)
            {
                if (distances[j] < minDistance)
                {
                    minDistance     = distances[j];
                    minIndex        = j;
                }

                continue;
            }

            if (minIndex == -1)
            {
                l.fatal("Could not determine priority for upgrade phase.");
                EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
                return;
            }

            final Player currentPlayer = this.players.get(minIndex);

            this.upgradePhasePlayersSortedByPriority.add(currentPlayer);
            currentPriority--;
            distances[minIndex] = Integer.MAX_VALUE;

            continue;
        }

        l.debug("Upgrade Phase priorities determined: {}.", this.upgradePhasePlayersSortedByPriority);

    }

    private boolean isUpgradeShopRightSize()
    {
        /* The shop must always have the size of all controllers connected. */
        return this.players.size() == this.upgradeShop.size();
    }

    private boolean doesUpgradeShopContains(final String card)
    {
        for (final AUpgradeCard upgradeCard : this.upgradeShop)
        {
            if(upgradeCard != null) {
                if (upgradeCard.getCardType().equals(card)) {
                    return true;
                }
            }

            continue;
        }

        return false;
    }

    private int getUpgradeCardCost(final String card)
    {
        for (final AUpgradeCard upgradeCard : this.upgradeShop)
        {
            if(upgradeCard != null) {
                if (upgradeCard.getCardType().equals(card)) {
                    return upgradeCard.getCost();
                }
            }

            continue;
        }

        l.error("Could not find cost for upgrade card {}.", card);

        return -1;
    }

    public boolean isProgrammingTimerServiceRunning()
    {
        return this.programmingPhaseTimerService != null && this.programmingPhaseTimerService.isAlive();
    }

    public Thread getProgrammingTimerService()
    {
        return this.programmingPhaseTimerService;
    }

    // endregion Getters and Setters

}
