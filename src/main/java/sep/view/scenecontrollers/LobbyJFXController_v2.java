package sep.view.scenecontrollers;

import sep.view.json.DefaultServerRequestParser;
import sep.view.json.lobby.PlayerValuesModel;
import sep.view.viewcontroller.ViewLauncher;
import sep.view.clientcontroller.GameInstance;
import sep.view.clientcontroller.EGameState;
import sep.view.json.ChatMsgModel;
import sep.view.clientcontroller.RemotePlayer;
import sep.view.clientcontroller.EClientInformation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ScrollPane;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.Objects;
import javafx.scene.input.KeyCode;

public final class LobbyJFXController_v2
{
    @FXML private Label sessionIDLabel;
    @FXML private TextField lobbyMsgInputTextField;
    @FXML private ScrollPane lobbyMsgScrollPane;
    private VBox lobbyMsgContainer;
    @FXML private VBox playerNameContainer;
    @FXML private TextField playerNameField;
    @FXML private Label formErrorLabel;
    @FXML private HBox playerRobotsSelectorContainer;

    /** Just for debugging purposes. Can be removed at any given time. */
    private void testChat()
    {
        VBox v = new VBox();
        v.setStyle("-fx-max-width: 380px");

        this.lobbyMsgScrollPane.setContent(v);

        for (int i = 0; i < 2; i++)
        {
            Label l = new Label("kjasdhfjhasjkdfhkljsadhfjipkahsdpuijüfhapiüjhwdtjiopüahndsjkpmgnapiejhtjipdanfpjkgnadpjkngpjiüaenrptjignadpfjignpijadnfg");
            l.setWrapText(true);
            v.getChildren().add(l);
            this.lobbyMsgScrollPane.setVvalue(1.0);
            continue;
        }

        return;
    }

    @FXML
    private void initialize()
    {
        HBox.setHgrow(this.playerNameContainer, Priority.ALWAYS);
        VBox.setVgrow(this.lobbyMsgScrollPane, Priority.ALWAYS);

        this.lobbyMsgInputTextField.lengthProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1)
            {
                if (t1.intValue() > EGameState.MAX_CHAT_MESSAGE_LENGTH)
                {
                    lobbyMsgInputTextField.setText(lobbyMsgInputTextField.getText().substring(0, EGameState.MAX_CHAT_MESSAGE_LENGTH));
                }

                return;
            }
        });

        this.lobbyMsgInputTextField.setOnKeyPressed(
        (keyEvent ->
        {
            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.ENTER)
            {
                this.onSubmitLobbyMsg();
            }

            return;
        })
        );

        this.addPlayerRobotSelector();

        this.lobbyMsgContainer = new VBox();
        this.lobbyMsgContainer.setId("lobby-msg-scroll-pane-inner");
        this.lobbyMsgScrollPane.setContent(this.lobbyMsgContainer);

        boolean bSuccess = false;
        try
        {
            bSuccess = GameInstance.connectToSessionPostLogin();
        }
        catch (IOException e)
        {
            ViewLauncher.getSceneController().killCurrentScreen();
        }

        if (!bSuccess)
        {
            ViewLauncher.getSceneController().killCurrentScreen();
        }

        this.sessionIDLabel.setText(String.format("Session ID: %s", EClientInformation.INSTANCE.getPreferredSessionID()));

        return;
    }

    private void onSubmitLobbyMsg()
    {
        String token = this.getChatMsg();
        this.lobbyMsgInputTextField.clear();

        if (this.isChatMsgACommand(token))
        {
            if (this.getCommand(token).isEmpty() || this.getCommand(token).isBlank())
            {
                this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Type /h for help on commands.", false);
                return;
            }

            if (this.getCommand(token).equals("w"))
            {
                if (!token.contains("\""))
                {
                    this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }
                int idxBSBegin = token.indexOf("\"");
                String sub = token.substring(idxBSBegin + 1);
                if (!sub.contains("\""))
                {
                    this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }
                int idxBSEnd = sub.indexOf("\"");

                String targetPlayer = token.substring(idxBSBegin + 1, idxBSBegin + idxBSEnd + 1);
                if (targetPlayer.isEmpty() || targetPlayer.isBlank())
                {
                    this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }

                String msgToWhisper;
                try
                {
                    msgToWhisper = token.substring(idxBSBegin + idxBSEnd + 3);
                }
                catch (IndexOutOfBoundsException e)
                {
                    this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Invalid message.", false);
                    return;
                }
                if (msgToWhisper.isEmpty() || msgToWhisper.isBlank())
                {
                    return;
                }

                RemotePlayer target = EGameState.INSTANCE.getRemotePlayerByPlayerName(targetPlayer);
                if (target == null)
                {
                    this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, String.format("Player %s not found.", targetPlayer), false);
                    return;
                }

                new ChatMsgModel(msgToWhisper, target.getPlayerID()).send();
                if (EClientInformation.INSTANCE.getPlayerID() != target.getPlayerID())
                {
                    this.addToChatMsgToScrollPane(EClientInformation.INSTANCE.getPlayerID(), msgToWhisper, true);
                }

                System.out.printf("[CLIENT] Whispering to %s.%n", targetPlayer);

                return;
            }

            if (this.getCommand(token).equals("h"))
            {
                this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Commands:", false);
                this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "/h - Show this help.", false);
                this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "/w [\"player name\"] [msg] - Whisper to a player.", false);
                return;
            }

            this.addToChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, String.format("Unknown command: %s", this.getCommand(token)), false);
            return;
        }

        if (!this.isChatMsgValid(token))
        {
            return;
        }
        new ChatMsgModel(token, ChatMsgModel.CHAT_MSG_BROADCAST).send();
        return;
    }

    public void updatePlayerSelection()
    {
        Platform.runLater(() -> {
            this.addPlayerRobotSelector();
            return;
        });
    }

    private void addToChatMsgToScrollPane(int caller, String msg, boolean bIsPrivate)
    {
        if (caller == ChatMsgModel.SERVER_ID)
        {
            Label l = new Label(String.format("[%s] %s", ChatMsgModel.SERVER_NAME, msg));
            l.getStyleClass().add("lobby-msg-server");
            l.setWrapText(true);
            this.lobbyMsgContainer.getChildren().add(l);
            this.lobbyMsgScrollPane.setVvalue(1.0);
            return;
        }

        if (caller == ChatMsgModel.CLIENT_ID)
        {
            Label l = new Label(String.format("[%s] %s", ChatMsgModel.CLIENT_NAME, msg));
            l.getStyleClass().add("lobby-msg-client");
            l.setWrapText(true);
            this.lobbyMsgContainer.getChildren().add(l);
            this.lobbyMsgScrollPane.setVvalue(1.0);
            return;
        }

        Label l = new Label(String.format("<%s>%s %s", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(caller)).getPlayerName(), bIsPrivate ? " whispers: " : "", msg));
        if (bIsPrivate)
        {
            l.getStyleClass().add("lobby-msg-whisper");
        }
        else
        {
            l.getStyleClass().add("lobby-msg");
        }
        l.setWrapText(true);
        this.lobbyMsgContainer.getChildren().add(l);
        this.lobbyMsgScrollPane.setVvalue(1.0); // TODO Kurwa does not work. We have to call this with a delay.
        return;
    }

    /** Will add a new chat msg from another thread. */
    public void handleChatMessage(DefaultServerRequestParser dsrp)
    {
        Platform.runLater(() -> {
            this.addToChatMsgToScrollPane(dsrp.getChatMsgSourceID(), dsrp.getChatMsg(), dsrp.isChatMsgPrivate());
            return;
        });

        return;
    }

    /** Will create the btn to select the different robots. */
    private void addPlayerRobotSelector()
    {
        this.playerRobotsSelectorContainer.getChildren().clear();

        for (int i = 0; i < EGameState.FIGURE_NAMES.length; i++)
        {
            Button btn = new Button(String.format(EGameState.FIGURE_NAMES[i]));
            if (EGameState.INSTANCE.hasClientSelectedARobot())
            {
                btn.getStyleClass().add("secondary-btn-mini");
            }
            else
            {
                btn.getStyleClass().add("primary-btn-mini");
            }
            btn.setDisable(EGameState.INSTANCE.isPlayerRobotUnavailable(i));

            int finalI = i;
            btn.setOnAction(actionEvent -> {
                this.formErrorLabel.setText("");

                if (this.isPlayerNameValid())
                {
                    System.out.printf("[CLIENT] Robot %d selected.%n", finalI);
                    new PlayerValuesModel(this.getPlayerName(), finalI).send();
                    return;
                }

                this.formErrorLabel.setText("Invalid player name.");
                System.out.printf("[CLIENT] Robot %d selected, but player name is invalid.%n", finalI);

                return;
            });

            Label possessedBy = new Label();
            if (EGameState.INSTANCE.isPlayerRobotUnavailable(i))
            {
                possessedBy.setText(Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByFigureID(i)).getPlayerName());
            }
            possessedBy.getStyleClass().add("text-base");
            HBox hbox = new HBox(possessedBy);
            hbox.setId("player-robot-player-name-wrapper");
            HBox.setHgrow(possessedBy, Priority.ALWAYS);

            VBox v = new VBox(btn, hbox);
            v.setId("player-robot-selector-wrapper");

            this.playerRobotsSelectorContainer.getChildren().add(v);

            continue;
        }

        return;
    }

    // region Getters and Setters

    private String getCommand(String token)
    {
        if (!token.contains(" "))
        {
            return token.substring(1);
        }

        return token.substring(1, token.indexOf(" "));
    }

    private boolean isChatMsgACommand(String token)
    {
        return token.startsWith(ChatMsgModel.COMMAND_PREFIX);
    }

    private boolean isChatMsgValid(String token)
    {
        return !token.isEmpty() && token.length() <= EGameState.MAX_CHAT_MESSAGE_LENGTH;
    }

    private boolean isPlayerNameValid()
    {
        return !this.getPlayerName().isEmpty() && this.getPlayerName().length() <= GameInstance.MAX_PLAYER_NAME_LENGTH;
    }

    private String getPlayerName()
    {
        return this.playerNameField.getText();
    }

    private String getChatMsg()
    {
        return this.lobbyMsgInputTextField.getText();
    }

    // endregion Getters and Setters

}
