package sep.view.viewcontroller;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewLauncher extends Application
{
    /** This instance is only valid on the JFX thread. */
    private static ViewLauncher INSTANCE;
    private SceneController sceneController;

    public ViewLauncher()
    {
        super();
        System.out.printf("[CLIENT] Creating View instance.%n");
        ViewLauncher.INSTANCE = this;
        return;
    }

    @Override
    public void start(Stage stage)
    {
        this.sceneController = new SceneController(new Scene(new Parent(){}, SceneController.PREF_WIDTH, SceneController.PREF_HEIGHT));
        stage.setScene(this.sceneController.getMasterScene());
        stage.setTitle(SceneController.WIN_TITLE);
        this.sceneController.renderNewScreen(SceneController.MAIN_MENU_ID, SceneController.PATH_TO_MAIN_MENU, false);

        stage.show();

        return;
    }

    public static void run()
    {
        Application.launch();
        return;
    }

    public static SceneController getSceneController()
    {
        return ViewLauncher.INSTANCE.sceneController;
    }

}
