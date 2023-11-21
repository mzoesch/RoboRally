package sep.view.scenecontrollers;

import sep.view.clientcontroller.GameInstance;
import sep.view.viewcontroller.ViewLauncher;
import sep.view.viewcontroller.SceneController;
import sep.view.clientcontroller.EClientInformation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.io.IOException;

/** JavaFX controller for the main menu screen. Handles session creation and joining. */
public class MainMenuJFXController
{
    @FXML private TextField sessionIDField;
    @FXML private Label sessionJoinErrorField;

    @FXML
    protected void onHostBtn(ActionEvent actionEvent) throws IOException
    {
        this.sessionJoinErrorField.setText("");

        if (GameInstance.connectToServer())
        {
            ViewLauncher.getSceneController().renderNewScreen(SceneController.LOBBY_ID, SceneController.PATH_TO_LOBBY_V2, true);
            return;
        }

        // TODO Get err pipeline from socket
        this.sessionJoinErrorField.setText("Failed to connect to session.");

        return;
    }

    @FXML
    protected void onJoinBtn(ActionEvent actionEvent) throws IOException
    {
        this.sessionJoinErrorField.setText("");

        if (this.sessionIDField.getText().isEmpty() || this.sessionIDField.getText().isBlank())
        {
            this.sessionJoinErrorField.setText("Session ID is invalid.");
            return;
        }

        if (GameInstance.connectToServer())
        {
            EClientInformation.INSTANCE.setPreferredSessionID(this.sessionIDField.getText());
            ViewLauncher.getSceneController().renderNewScreen(SceneController.LOBBY_ID, SceneController.PATH_TO_LOBBY_V2, true);
            return;
        }

        // TODO Get err pipeline from socket
        this.sessionJoinErrorField.setText("Failed to connect to session.");

        return;
    }

    @FXML
    protected void onExitBtn(ActionEvent actionEvent)
    {
        GameInstance.kill();
        return;
    }

    @FXML
    private void initialize()
    {
        // TODO Un focus session ID field. It's annoying. See this fxml file for more information.
        return;
    }

}
