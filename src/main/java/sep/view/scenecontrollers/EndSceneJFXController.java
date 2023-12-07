package sep.view.scenecontrollers;

import sep.EArgs;
import sep.view.clientcontroller.EGameState;
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

/** JavaFX controller for the main menu screen. Handles winner announcement and return to MainMenu/ Exit. */
public class EndSceneJFXController{

        @FXML private Label winnerNameField;


        @FXML
        protected void onExitBtn(ActionEvent actionEvent)
        {
            GameInstance.kill();
            return;
        }

        @FXML
        private void initialize()
        {
            if(EGameState.INSTANCE.getWinningPlayer() == ""){
                this.winnerNameField.setText("EndGame Error");
            } else {
                this.winnerNameField.setText(EGameState.INSTANCE.getWinningPlayer());
            }

            // TODO Here Winner will be set
            return;
        }
}
