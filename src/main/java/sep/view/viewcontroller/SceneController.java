package sep.view.viewcontroller;

import sep.view.clientcontroller.   GameInstance    ;
import sep.view.lib.                Types           ;

import javafx.scene.                Scene           ;
import java.util.                   Objects         ;
import java.util.                   ArrayList       ;
import javafx.scene.layout.         Pane            ;
import javafx.scene.layout.         HBox            ;
import javafx.scene.layout.         AnchorPane      ;
import javafx.scene.layout.         BorderPane      ;
import javafx.fxml.                 FXMLLoader      ;
import javafx.scene.control.        Button          ;
import javafx.scene.control.        Label           ;
import java.io.                     IOException     ;
import org.apache.logging.log4j.    LogManager      ;
import org.apache.logging.log4j.    Logger          ;

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

    public static final String  WIN_TITLE       = "CLIENT";
    public static final int     PREF_WIDTH      = 1_280;
    public static final int     PREF_HEIGHT     = 720;

    public static final String  MAIN_MENU_ID    = "main-menu";
    public static final String  LOBBY_ID        = "lobby";
    public static final String  GAME_ID         = "game";
    public static final String  END_SCENE_ID    = "end-scene";

    public static final String  PATH_TO_MAIN_MENU   = "main-menu.fxml";
    /** @deprecated */
    public static final String  PATH_TO_LOBBY       = "lobby.fxml";
    public static final String  PATH_TO_LOBBY_V2    = "lobby_v2.fxml";
    public static final String  PATH_TO_GAME        = "game.fxml";
    public static final String  PATH_TO_END_SCENE   = "end-scene.fxml";

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

    private void activateScreen(final String ID)
    {
        for (final RGameScene<?> s : this.screens)
        {
            if (!s.ID().equals(ID))
            {
                continue;
            }

            final RGameScene<?> oldScreen = this.screens.size() > 1 ? this.getCurrentScreen() : null;

            this.currentScreen = ID;
            this.masterScene.setRoot(s.screen());

            if (oldScreen != null && oldScreen.hasFallback())
            {
                this.screens.remove(oldScreen);
            }

            return;
        }

        l.fatal("Failed to activate screen with ID {}.", ID);
        GameInstance.kill();

        return;
    }

    private void createPopUp(final Pane target, final Types.RPopUpMask mask)
    {
        final AnchorPane container = new AnchorPane();
        container.setId("pop-up-container");
        container.setMouseTransparent(false);

        final Label headerLabel = new Label(mask.header());
        headerLabel.getStyleClass().add("text-2xl-error");
        final HBox header = new HBox(headerLabel);
        header.setId("pop-up-header");

        final Label msgLabel = new Label(mask.msg());
        msgLabel.getStyleClass().add("text-xl");
        final HBox body = new HBox(msgLabel);
        body.setId("pop-up-body");
        body.setMouseTransparent(true);

        final HBox form = new HBox();
        form.setId("pop-up-form");
        if (Objects.requireNonNull(mask.type()) == Types.EPopUp.ERROR)
        {
            final Button b = new Button("OK");
            b.getStyleClass().add("secondary-btn");
            b.setOnAction(e ->
            {
                target.getChildren().remove(container);
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

        AnchorPane.setLeftAnchor(   container,  0.0 );
        AnchorPane.setRightAnchor(  container,  0.0 );
        AnchorPane.setTopAnchor(    container,  0.0 );
        AnchorPane.setBottomAnchor( container,  0.0 );

        target.getChildren().add(container);

        l.debug("Successfully created pop up of type {}.", mask.type().toString());

        return;
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
            GameInstance.kill();
            return;
        }

        final T ctrl = ldr.getController();

        final RGameScene<T> rgs = new RGameScene<T>(ID, p, ctrl, bAutoKillAfterUse ? this.currentScreen : "");

        this.addScreen(rgs);
        this.activateScreen(ID);

        return;
    }

    public void renderPopUp(final Types.RPopUpMask mask)
    {
        final Pane target;

        if (this.masterScene.getRoot() instanceof final BorderPane bp)
        {
            if (bp.getCenter() instanceof final AnchorPane ap)
            {
                this.createPopUp(ap, mask);
                return;
            }

            l.fatal("Failed to create pop up. Center of BorderPane is not an AnchorPane.");
            GameInstance.kill();

            return;
        }

        if (this.masterScene.getRoot() instanceof final AnchorPane ap)
        {
            this.createPopUp(ap, mask);
            return;
        }

        l.fatal("Failed to create pop up. Root of master scene is not valid.");
        GameInstance.kill();

        return;
    }

    public void renderExistingScreen(final String ID)
    {
        this.activateScreen(ID);
        return;
    }

    public void killCurrentScreen()
    {
        final RGameScene<?> currentScreen = this.getCurrentScreen();
        if (!currentScreen.hasFallback())
        {
            l.fatal("No fallback screen found.");
            GameInstance.kill();
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

    public RGameScene<?> getScreenByID(final String ID)
    {
        if (this.screens.isEmpty())
        {
            l.fatal("No screens are loaded.");
            GameInstance.kill();
            return null;
        }

        for (final RGameScene<?> s : this.screens)
        {
            if (s.ID().equals(ID))
            {
                return s;
            }
        }

        l.fatal("No screen with ID {} found.", ID);
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

}
