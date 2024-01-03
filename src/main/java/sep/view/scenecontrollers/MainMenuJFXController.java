package sep.view.scenecontrollers;

import sep.EArgs;
import sep.view.clientcontroller.GameInstance;
import sep.view.viewcontroller.ViewSupervisor;
import sep.view.viewcontroller.SceneController;
import sep.view.clientcontroller.EGameState;
import sep.view.clientcontroller.EClientInformation;
import sep.view.lib.RPopUpMask;
import sep.view.lib.EPopUp;

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
        if (this.isServerAddressInvalid())
        {
            return;
        }

        EGameState.reset();

        // TODO We need to call this on a separate thread. If the connections establishment is not immediate, the
        //      JFX Window will freeze and is unresponsive. On a longer period of time (for example during a network
        //      timeout), the JFX Window will crash. We need to implement a callback to this class then.
        if (GameInstance.connectToServer())
        {
            ViewSupervisor.getSceneController().renderNewScreen(SceneController.LOBBY_ID, SceneController.PATH_TO_LOBBY_V2, true);
            return;
        }

        ViewSupervisor.createPopUp(new RPopUpMask(EPopUp.ERROR, "Server Connection Failed", EClientInformation.INSTANCE.getStdServerErrPipeline().toString()));

        return;
    }

    @FXML
    protected void onJoinBtn(ActionEvent actionEvent) throws IOException
    {
        if (this.isServerAddressInvalid())
        {
            return;
        }

        if (this.sessionIDField.getText().isEmpty() || this.sessionIDField.getText().isBlank())
        {
            this.sessionJoinErrorField.setText("Session ID is invalid.");
            return;
        }

        EGameState.reset();

        // TODO We need to call this on a separate thread. If the connections establishment is not immediate, the
        //      JFX Window will freeze and is unresponsive. On a longer period of time (for example during a network
        //      timeout), the JFX Window will crash. We need to implement a callback to this class then.
        if (GameInstance.connectToServer())
        {
            EClientInformation.INSTANCE.setPreferredSessionID(this.sessionIDField.getText());
            ViewSupervisor.getSceneController().renderNewScreen(SceneController.LOBBY_ID, SceneController.PATH_TO_LOBBY_V2, true);
            return;
        }

        ViewSupervisor.createPopUp(new RPopUpMask(EPopUp.ERROR, "Server Connection Failed", EClientInformation.INSTANCE.getStdServerErrPipeline().toString()));

        return;
    }

    @FXML
    public void onPopUpTestBtn(final ActionEvent actionEvent)
    {
        ViewSupervisor.createPopUp(new RPopUpMask(EPopUp.ERROR, "Test", "This is a test pop up."));
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

    private boolean isServerAddressInvalid()
    {
        if (!(this.serverAddressField.getText().isEmpty() || this.serverAddressField.getText().isBlank()))
        {
            String[] tokens = this.serverAddressField.getText().split(":");
            if (tokens.length != 2)
            {
                this.sessionJoinErrorField.setText("Server address is invalid.");
                return true;
            }

            if (tokens[0].isEmpty() || tokens[0].isBlank())
            {
                this.sessionJoinErrorField.setText("Server address is invalid.");
                return true;
            }

            if (tokens[1].isEmpty() || tokens[1].isBlank())
            {
                this.sessionJoinErrorField.setText("Server port is invalid.");
                return true;
            }

            EClientInformation.INSTANCE.setServerIP(tokens[0]);
            try
            {
                if (Integer.parseInt(tokens[1]) < sep.Types.EPort.MIN.i || Integer.parseInt(tokens[1]) > sep.Types.EPort.MAX.i)
                {
                    this.sessionJoinErrorField.setText("Server port is invalid.");
                    return true;
                }
                EClientInformation.INSTANCE.setServerPort(Integer.parseInt(tokens[1]));
            }
            catch (NumberFormatException e)
            {
                this.sessionJoinErrorField.setText("Server port is invalid.");
                return true;
            }
        }
        else
        {
            EClientInformation.INSTANCE.setServerIP(EArgs.PREF_SERVER_IP);
            EClientInformation.INSTANCE.setServerPort(EArgs.PREF_SERVER_PORT.i);
        }

        return false;
    }

}
