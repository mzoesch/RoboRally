package sep.view.viewcontroller;

import sep.view.clientcontroller.   GameInstance;
import sep.view.lib.                RPopUpMask;
import sep.view.lib.                EPopUp;

import javafx.scene.                Scene;
import javafx.scene.                Node;
import java.util.                   Objects;
import java.util.                   ArrayList;
import javafx.scene.layout.         Pane;
import javafx.scene.layout.         HBox;
import javafx.scene.layout.         AnchorPane;
import javafx.scene.layout.         BorderPane;
import javafx.scene.layout.         Region;
import javafx.scene.layout.         VBox;
import javafx.scene.layout.         Priority;
import javafx.fxml.                 FXMLLoader;
import javafx.scene.control.        Button;
import javafx.scene.control.        Label;
import java.io.                     IOException;
import javafx.scene.effect.         Effect;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import javafx.scene.text.           TextAlignment;

/**
 * Singleton object that implements high-level methods relevant for the Graphical User Interface and handles the
 * overall flow it. It is spawned by the {@link ViewSupervisor} at JFX startup and not destroyed until the Platform
 * has been exited.
 *
 * <p> It manages the loading of FXML files and the corresponding controllers, the activation of them and destruction.
 */
public final class SceneController
{
    private static final Logger l = LogManager.getLogger(SceneController.class);

    /** On slower end hardware this timeout may be increased. */
    public static final int     MAIN_MENU_REROUTING_DELAY   = 200;

    public static final String  WIN_TITLE                   = "CLIENT";
    public static final int     PREF_WIDTH                  = 1_280;
    public static final int     PREF_HEIGHT                 = 720;

    public static final String  MAIN_MENU_ID                = "main-menu";
    public static final String  LOBBY_ID                    = "lobby";
    public static final String  GAME_ID                     = "game";
    public static final String  END_SCENE_ID                = "end-scene";

    public static final String  PATH_TO_MAIN_MENU           = "main-menu.fxml";
    /** @deprecated */
    public static final String  PATH_TO_LOBBY               = "lobby.fxml";
    public static final String  PATH_TO_LOBBY_V2            = "lobby_v2.fxml";
    public static final String  PATH_TO_GAME                = "game.fxml";
    public static final String  PATH_TO_END_SCENE           = "end-scene.fxml";

    /** The scene where we apply different screens (panes in our case) to. */
    private final Scene                     masterScene;
    private String                          currentScreen;
    private final ArrayList<RGameScene<?>>  screens;

    public SceneController(final Scene masterScene)
    {
        super();

        this.masterScene    = masterScene;
        this.currentScreen  = "";
        this.screens        = new ArrayList<RGameScene<?>>();

        return;
    }

    private <T> void addScreen(final RGameScene<T> RGameScene)
    {
        this.screens.add(RGameScene);
        return;
    }

    private void activateScreen(final String id)
    {
        for (final RGameScene<?> s : this.screens)
        {
            if (!s.id().equals(id))
            {
                continue;
            }

            final RGameScene<?> oldScreen = this.screens.size() > 1 ? this.getCurrentScreen() : null;

            this.currentScreen = id;
            this.masterScene.setRoot(s.screen());

            if (oldScreen != null && oldScreen.hasFallback())
            {
                this.screens.remove(oldScreen);
            }

            return;
        }

        l.fatal("Failed to activate screen with ID {}.", id);
        GameInstance.kill();

        return;
    }

    private Pane createPopUp(final Pane target, final RPopUpMask mask)
    {
        final AnchorPane container = new AnchorPane();
        container.setId("pop-up-container");
        container.setMouseTransparent(false);

        final HBox outerContainer = new HBox(container);
        outerContainer.setStyle("-fx-alignment: center;");

        final Label headerLabel = new Label(mask.header());
        if (mask.type() == EPopUp.ERROR)
        {
            headerLabel.getStyleClass().add("text-2xl-error");
        }
        else if (mask.type() == EPopUp.WARNING)
        {
            headerLabel.getStyleClass().add("text-2xl-warning");
        }
        else
        {
            headerLabel.getStyleClass().add("text-2xl");
        }
        headerLabel.setWrapText(true);
        headerLabel.setTextAlignment(TextAlignment.CENTER);
        final HBox header = new HBox(headerLabel);
        header.setId("pop-up-header");

        final Label msgLabel = new Label(mask.msg());
        msgLabel.getStyleClass().add("text-xl");
        msgLabel.setWrapText(true);
        msgLabel.setTextAlignment(TextAlignment.CENTER);
        final HBox body = new HBox(msgLabel);
        body.setId("pop-up-body");
        body.setMouseTransparent(true);

        final HBox form = new HBox();
        form.setId("pop-up-form");
        if (Objects.requireNonNull(mask.type()) == EPopUp.ERROR)
        {
            final Button b = new Button("OK");
            b.getStyleClass().add("secondary-btn");
            b.setOnAction(e ->
            {
                target.getChildren().remove(outerContainer);
                return;
            });

            form.getChildren().add(b);
        }
        else if (Objects.requireNonNull(mask.type()) == EPopUp.WARNING)
        {
            final Button b = new Button("OK");
            b.getStyleClass().add("secondary-btn");
            b.setOnAction(e ->
            {
                target.getChildren().remove(outerContainer);
                return;
            });

            form.getChildren().add(b);
        }

        container.getChildren().add(    body    );
        container.getChildren().add(    header  );
        container.getChildren().add(    form    );

        AnchorPane.setLeftAnchor(   header, 0.0 );
        AnchorPane.setRightAnchor(  header, 0.0 );
        AnchorPane.setTopAnchor(    header, 0.0 );

        AnchorPane.setLeftAnchor(   body,   0.0 );
        AnchorPane.setRightAnchor(  body,   0.0 );
        AnchorPane.setTopAnchor(    body,   0.0 );
        AnchorPane.setBottomAnchor( body,   0.0 );

        AnchorPane.setLeftAnchor(   form,  0.0 );
        AnchorPane.setRightAnchor(  form,  0.0 );
        AnchorPane.setBottomAnchor( form,  0.0 );

        AnchorPane.setLeftAnchor(   outerContainer,  0.0 );
        AnchorPane.setRightAnchor(  outerContainer,  0.0 );
        AnchorPane.setTopAnchor(    outerContainer,  0.0 );
        AnchorPane.setBottomAnchor( outerContainer,  0.0 );

        return outerContainer;
    }

    public <T> void renderNewScreen(final String ID, final String path, final boolean bAutoKillAfterUse)
    {
        final FXMLLoader ldr = new FXMLLoader(SceneController.class.getResource(path));
        final Pane p;
        try
        {
            p = ldr.load();
        }
        catch (final IOException e)
        {
            l.fatal("Failed to load FXML file at {}.", path);
            l.fatal(e.getMessage());
            for (final StackTraceElement ste : e.getStackTrace())
            {
                l.fatal(ste.toString());
                continue;
            }
            GameInstance.kill(GameInstance.EXIT_FATAL);
            return;
        }

        final T ctrl = ldr.getController();

        final RGameScene<T> rgs = new RGameScene<T>(ID, p, ctrl, bAutoKillAfterUse ? this.currentScreen : "");

        this.addScreen(rgs);
        this.activateScreen(ID);

        return;
    }

    private Pane getPopUpTarget()
    {
        if (this.masterScene.getRoot() instanceof final BorderPane bp)
        {
            if (bp.getCenter() instanceof final AnchorPane ap)
            {
                return ap;
            }

            return null;
        }

        if (this.masterScene.getRoot() instanceof final AnchorPane ap)
        {
            return ap;
        }

        return null;
    }

    public void renderPopUp(final RPopUpMask mask)
    {
        this.renderPopUp(this.createPopUp(this.getPopUpTarget(), mask));

        l.debug("Successfully created pop up of type {}.", mask.type().toString());

        return;
    }

    public void renderPopUp(final Pane p)
    {
        final Pane target = this.getPopUpTarget();

        if (target == null)
        {
            l.fatal("Failed to create pop up. Root of master scene is not valid for holding pop ups.");
            GameInstance.kill();
            return;
        }

        target.getChildren().add(p);

        l.debug("Successfully rendered pop up.");

        return;
    }

    public void destroyPopUp(final Pane target, final boolean bSoft)
    {
        if (target == null)
        {
            l.fatal("Failed to destroy pop up. Target is null.");
            GameInstance.kill();
            return;
        }

        if (bSoft && !Objects.requireNonNull(this.getPopUpTarget()).getChildren().contains(target))
        {
            l.warn("Pop up is already destroyed or was never rendered. Ignoring.");
            return;
        }

        if (!Objects.requireNonNull(this.getPopUpTarget()).getChildren().contains(target))
        {
            l.fatal("Failed to destroy pop up. Target is not a child of the pop up master container.");
            GameInstance.kill();
            return;
        }

        Objects.requireNonNull(this.getPopUpTarget()).getChildren().remove(target);

        l.debug("Successfully destroyed pop up.");

        return;
    }

    public void renderExistingScreen(final String id)
    {
        this.activateScreen(id);
        return;
    }

    public void killCurrentScreen()
    {
        final RGameScene<?> currentScreen = this.getCurrentScreen();
        if (!currentScreen.hasFallback())
        {
            l.error("No fallback screen found. Rerouting to main menu.");

            for (final RGameScene<?> s : this.screens)
            {
                if (s.id().equals(SceneController.MAIN_MENU_ID))
                {
                    continue;
                }

                this.screens.remove(s);
                continue;
            }

            new Thread(() ->
            {
                try
                {
                    Thread.sleep(SceneController.MAIN_MENU_REROUTING_DELAY);
                }
                catch (final InterruptedException e)
                {
                    l.error("Interrupted while rerouting to main menu.");
                    l.error(e.getMessage());
                    GameInstance.kill();
                    return;
                }

                this.activateScreen(SceneController.MAIN_MENU_ID);
                l.debug("Successfully rerouted to main menu.");

                return;
            })
            .start();

            return;
        }

        this.screens.remove(currentScreen);
        this.activateScreen(currentScreen.fallback());

        return;
    }

    public Scene getMasterScene()
    {
        return this.masterScene;
    }

    public RGameScene<?> getScreenByID(final String id)
    {
        if (this.screens.isEmpty())
        {
            l.fatal("No screens are loaded.");
            GameInstance.kill(GameInstance.EXIT_FATAL);
            return null;
        }

        for (final RGameScene<?> s : this.screens)
        {
            if (s.id().equals(id))
            {
                return s;
            }
        }

        l.fatal("No screen with ID {} found.", id);
        GameInstance.kill();

        return null;
    }

    public RGameScene<?> getCurrentScreen()
    {
        return this.getScreenByID(this.currentScreen);
    }

    public <T> T getCurrentController()
    {
        //noinspection unchecked
        return (T) this.getCurrentScreen().controller();
    }

    private Node createHSpacer()
    {
        final Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        return s;
    }

    private Node createVSpacer()
    {
        final Region s = new Region();
        VBox.setVgrow(s, Priority.ALWAYS);
        return s;
    }

    public Pane getRenderTarget()
    {
        return this.getPopUpTarget();
    }

    public void clearSceneEffect()
    {
        this.getRenderTarget().setEffect(null);
        return;
    }

    public void applySceneEffect(final Effect e)
    {
        this.getRenderTarget().setEffect(e);
        return;
    }

}
