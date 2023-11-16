package sep.view.scenecontrollers;

import sep.view.viewcontroller.ViewLauncher;
import sep.view.json.ChatMessageModel;
import sep.view.clientcontroller.GameInstance;
import sep.view.clientcontroller.EClientInformation;
import sep.view.clientcontroller.EGameState;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

/** JavaFX controller for the lobby screen. Handles game start. */
public class LobbyJFXController
{
    @FXML private Label sessionIDLabel;
    @FXML private Button startGameBtn;

    @FXML private VBox currentPlayersList;

    @FXML private ScrollPane messageScrollPane;
    private VBox messageVBox;
    @FXML private TextField messageField;

    @FXML
    protected void onStartGameBtn(ActionEvent actionEvent)
    {
        System.out.printf("[CLIENT] Start Game Button pressed.%n");
        return;
    }

    @FXML
    protected void onLeaveLobbyBtn(ActionEvent actionEvent)
    {
        GameInstance.handleServerDisconnect();
        ViewLauncher.getSceneController().killCurrentScreen();
        return;
    }

    private static boolean isMessageValid(String message)
    {
        return !message.isEmpty() && message.length() <= ChatMessageModel.MAX_MESSAGE_LENGTH;
    }

    /** Will add a new message to the active scroll pane from another thread. */
    public void addMessage(String caller, String message)
    {
        Platform.runLater(() ->
        {
            if (caller.equals("SERVER"))
            {
                Label callerLabel = new Label(String.format("[%s]", caller));
                callerLabel.setId("lobby-msg-server");
                Label messageLabel = new Label(message);
                messageLabel.setId("lobby-msg-server");

                HBox hBox = new HBox(callerLabel, messageLabel);
                hBox.setId("lobby-msg-hbox");

                this.messageVBox.getChildren().add(hBox);
                return;
            }

            Label callerLabel = new Label(String.format("<%s>", caller));
            callerLabel.setId("lobby-msg-caller");
            Label messageLabel = new Label(message);
            messageLabel.setId("lobby-msg");

            HBox hBox = new HBox(callerLabel, messageLabel);
            hBox.setId("lobby-msg-hbox");

            this.messageVBox.getChildren().add(hBox);
            return;
        });
    }

    /** Will update the player list from another thread. */
    public void updatePlayerNames()
    {
        Platform.runLater(() ->
        {
            this.currentPlayersList.getChildren().clear();

            String[] playerNames = EGameState.INSTANCE.getPlayerNames();
            String hostPlayerName = EGameState.INSTANCE.getHostPlayerName();

            for (String playerName : playerNames)
            {
                Label playerLabel = new Label(playerName);
                playerLabel.getStyleClass().add("lobby-player");
                HBox.setHgrow(playerLabel, Priority.ALWAYS);

                HBox hbox = new HBox(playerLabel);
                hbox.getStyleClass().add("lobby-player-container");

                if (playerName.equals(hostPlayerName))
                {
                    Label hostLabel = new Label("HOST");
                    hostLabel.getStyleClass().add("lobby-player-host");
                    hbox.getChildren().add(hostLabel);
                }

                this.currentPlayersList.getChildren().add(hbox);
                continue;
            }

            return;
        });

        return;
    }

    @FXML
    protected void onSubmitMessageBtn()
    {
        if (!LobbyJFXController.isMessageValid(this.messageField.getText()))
        {
            return;
        }

        new ChatMessageModel(EClientInformation.INSTANCE.getPlayerName(), this.messageField.getText()).send();
        this.messageField.clear();

        return;
    }

    /** Will send the Post Login Confirmation to the server. */
    @FXML
    private void initialize()
    {
        VBox.setVgrow(this.messageScrollPane, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(this.messageField, javafx.scene.layout.Priority.ALWAYS);

        this.messageVBox = new VBox();
        this.messageScrollPane.setContent(this.messageVBox);

        this.messageField.lengthProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1)
            {
                if (t1.intValue() > ChatMessageModel.MAX_MESSAGE_LENGTH)
                {
                    messageField.setText(messageField.getText().substring(0, ChatMessageModel.MAX_MESSAGE_LENGTH));
                }

                return;
            }
        });
        this.messageField.setOnKeyPressed((event) ->
        {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER)
            {
                this.onSubmitMessageBtn();
            }

            return;
        });

        this.sessionIDLabel.setText(String.format("Session ID: %s", EClientInformation.INSTANCE.getConnectedLobbyID()));

        try
        {
            GameInstance.connectToSessionPostLogin();
        }
        catch (IOException e)
        {
            ViewLauncher.getSceneController().killCurrentScreen();
        }

        return;
    }

}
