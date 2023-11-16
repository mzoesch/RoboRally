package sep.view.scenecontrollers;

import sep.view.clientcontroller.GameInstance;
import sep.view.viewcontroller.ViewLauncher;
import sep.view.viewcontroller.SceneController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.io.IOException;

public class MainMenuJFXController
{
    @FXML private TextField playerNameField;
    @FXML private TextField sessionIDField;
    @FXML private Label formErrorField;
    @FXML private Label sessionJoinErrorField;

    @FXML
    protected void onHostBtn(ActionEvent actionEvent) throws IOException
    {
        this.formErrorField.setText("");
        this.sessionJoinErrorField.setText("");

        if (this.isPlayerNameInvalid(this.getPlayerName()))
        {
            this.formErrorField.setText(String.format("Invalid player name (max %d characters).", GameInstance.MAX_PLAYER_NAME_LENGTH));
            return;
        }

        if (GameInstance.connectToNewSession(this.getPlayerName()))
        {
            ViewLauncher.getSceneController().renderNewScreen(SceneController.LOBBY_ID, SceneController.PATH_TO_LOBBY, true);
            return;
        }

        // TODO Get err pipeline from socket
        this.sessionJoinErrorField.setText("Failed to connect to session.");

        return;
    }

    @FXML
    protected void onJoinBtn(ActionEvent actionEvent) throws IOException
    {
        this.formErrorField.setText("");
        this.sessionJoinErrorField.setText("");

        if (this.isPlayerNameInvalid(this.getPlayerName()))
        {
            this.formErrorField.setText(String.format("Invalid player name (max %d characters).", GameInstance.MAX_PLAYER_NAME_LENGTH));
            return;
        }

        if (!this.isLobbyIDValid(this.getLobbyID()))
        {
            this.formErrorField.setText("Invalid lobby ID.");
            return;
        }

        if (GameInstance.connectToExistingSession(this.getPlayerName(), this.getLobbyID()))
        {
            ViewLauncher.getSceneController().renderNewScreen(SceneController.LOBBY_ID, SceneController.PATH_TO_LOBBY, true);
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
        return;
    }

    private boolean isPlayerNameInvalid(String name)
    {
        return name.isEmpty() || name.length() > GameInstance.MAX_PLAYER_NAME_LENGTH;
    }

    private boolean isLobbyIDValid(String id)
    {
        return id.length() == GameInstance.SESSION_ID_LENGTH;
    }

    private String getPlayerName()
    {
        return this.playerNameField.getText();
    }

    private String getLobbyID()
    {
        return this.sessionIDField.getText();
    }

}
