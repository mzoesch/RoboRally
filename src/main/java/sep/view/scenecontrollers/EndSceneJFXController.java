package sep.view.scenecontrollers;

import sep.view.clientcontroller.EGameState;
import sep.view.clientcontroller.GameInstance;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

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
            try{
                this.winnerNameField.setText(EGameState.INSTANCE.getWinner().getPlayerName() + " has won the Game!!!");
            } catch (NullPointerException e){
                this.winnerNameField.setText("EndGame Error");
            }
            return;
        }
}
