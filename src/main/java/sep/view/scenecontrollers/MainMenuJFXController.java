package sep.view.scenecontrollers;

import sep.EArgs;
import sep.view.clientcontroller.GameInstance;
import sep.view.viewcontroller.ViewSupervisor;
import sep.view.viewcontroller.SceneController;
import sep.EPort;
import sep.view.clientcontroller.EClientInformation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.io.IOException;

// TODO Some sort of loading screen while connecting to server. The server connection is blocking the JFX Thread.
/** JavaFX controller for the main menu screen. Handles session creation and joining. */
public class MainMenuJFXController
{
    @FXML private TextField serverAddressField;
    @FXML private TextField sessionIDField;
    @FXML private Label sessionJoinErrorField;

    @FXML
    protected void onHostBtn(ActionEvent actionEvent) throws IOException
    {
        this.sessionJoinErrorField.setText("");

        if (!this.parseServerAddress())
        {
            return;
        }

        if (GameInstance.connectToServer())
        {
            ViewSupervisor.getSceneController().renderNewScreen(SceneController.LOBBY_ID, SceneController.PATH_TO_LOBBY_V2, true);
            return;
        }

        this.sessionJoinErrorField.setText(EClientInformation.INSTANCE.getStdServerErrPipeline().toString());

        return;
    }

    @FXML
    protected void onJoinBtn(ActionEvent actionEvent) throws IOException
    {
        this.sessionJoinErrorField.setText("");

        if (!this.parseServerAddress())
        {
            return;
        }

        if (this.sessionIDField.getText().isEmpty() || this.sessionIDField.getText().isBlank())
        {
            this.sessionJoinErrorField.setText("Session ID is invalid.");
            return;
        }

        if (GameInstance.connectToServer())
        {
            EClientInformation.INSTANCE.setPreferredSessionID(this.sessionIDField.getText());
            ViewSupervisor.getSceneController().renderNewScreen(SceneController.LOBBY_ID, SceneController.PATH_TO_LOBBY_V2, true);
            return;
        }

        this.sessionJoinErrorField.setText(EClientInformation.INSTANCE.getStdServerErrPipeline().toString());

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

    private boolean parseServerAddress()
    {
        if (!(this.serverAddressField.getText().isEmpty() || this.serverAddressField.getText().isBlank()))
        {
            String[] tokens = this.serverAddressField.getText().split(":");
            if (tokens.length != 2)
            {
                this.sessionJoinErrorField.setText("Server address is invalid.");
                return false;
            }

            if (tokens[0].isEmpty() || tokens[0].isBlank())
            {
                this.sessionJoinErrorField.setText("Server address is invalid.");
                return false;
            }

            if (tokens[1].isEmpty() || tokens[1].isBlank())
            {
                this.sessionJoinErrorField.setText("Server port is invalid.");
                return false;
            }

            EClientInformation.INSTANCE.setServerIP(tokens[0]);
            try
            {
                if (Integer.parseInt(tokens[1]) < EPort.MIN.i || Integer.parseInt(tokens[1]) > EPort.MAX.i)
                {
                    this.sessionJoinErrorField.setText("Server port is invalid.");
                    return false;
                }
                EClientInformation.INSTANCE.setServerPort(Integer.parseInt(tokens[1]));
            }
            catch (NumberFormatException e)
            {
                this.sessionJoinErrorField.setText("Server port is invalid.");
                return false;
            }
        }
        else
        {
            EClientInformation.INSTANCE.setServerIP(EArgs.PREF_SERVER_IP);
            EClientInformation.INSTANCE.setServerPort(EArgs.PREF_SERVER_PORT.i);
        }

        return true;
    }

}
