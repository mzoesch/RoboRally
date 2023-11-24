package sep.view.scenecontrollers;

import javafx.event.ActionEvent;
import sep.view.json.DefaultServerRequestParser;
import sep.view.json.lobby.PlayerValuesModel;
import sep.view.viewcontroller.ViewLauncher;
import sep.view.clientcontroller.GameInstance;
import sep.view.clientcontroller.EGameState;
import sep.view.json.ChatMsgModel;
import sep.view.clientcontroller.RemotePlayer;
import sep.view.clientcontroller.EClientInformation;
import sep.view.json.lobby.ReadyPlayerModel;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.beans.binding.Bindings;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

public final class LobbyJFXController_v2
{
    private static final Logger l = LogManager.getLogger(LobbyJFXController_v2.class);

    private boolean bReadyBtnClicked;

    public LobbyJFXController_v2()
    {
        super();
        this.bReadyBtnClicked = false;
        return;
    }

    @FXML private Button readyButton;
    @FXML private VBox playerListContainer;
    @FXML private VBox readyLabelContainerWrapper;
    @FXML private ScrollPane formScrollPane;
    @FXML private VBox formArea;
    @FXML private Label sessionIDLabel;
    @FXML private TextField lobbyMsgInputTextField;
    @FXML private ScrollPane lobbyMsgScrollPane;
    private VBox lobbyMsgContainer;
    @FXML private VBox playerNameContainer;
    @FXML private TextField playerNameField;
    @FXML private Label formErrorLabel;
    @FXML private HBox playerRobotsSelectorContainer;
    @FXML private VBox playerRobotSelectorArea;
    @FXML private HBox readyLabelContainer;

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
        HBox.setHgrow(this.readyLabelContainer, Priority.ALWAYS);
        HBox.setHgrow(this.readyLabelContainerWrapper, Priority.ALWAYS);

        this.readyLabelContainerWrapper.getChildren().add(1, this.createVSpacer());
        this.readyLabelContainer.getChildren().add(1, this.createHSpacer());

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

        this.updateView();

        this.lobbyMsgContainer = new VBox();
        this.lobbyMsgContainer.setId("lobby-msg-scroll-pane-inner");
        this.lobbyMsgScrollPane.setContent(this.lobbyMsgContainer);

        this.formArea.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
            this.formScrollPane.getViewportBounds().getWidth(), this.formScrollPane.viewportBoundsProperty()
        ));

        boolean bSuccess = false;
        try
        {
            bSuccess = GameInstance.connectToSessionPostLogin();
        }
        catch (IOException e)
        {
            ViewLauncher.getSceneController().killCurrentScreen();
            return;
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

                l.debug("Whispering to {}.", targetPlayer);

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

    /** Always call from another thread. */
    public void updatePlayerSelection()
    {
        Platform.runLater(() -> {
            this.updateView();
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

    /** Will update the player status from another thread. */
    public void handlePlayerStatusUpdate()
    {
        Platform.runLater(() -> {
            this.updateReadyBtn();
            return;
        });
        return;
    }

    private Node createHSpacer()
    {
        final Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        return s;
    }

    private Node createVSpacer()
    {
        final Region s = new Region();
        VBox.setVgrow(s, Priority.ALWAYS);
        return s;
    }

    private Node createRobotSelectorBox(int idx)
    {
        Label l = new Label(EGameState.INSTANCE.isPlayerRobotUnavailable(idx) ? EGameState.INSTANCE.getRemotePlayerByFigureID(idx) == null ? "Available" : Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByFigureID(idx)).getPlayerName() : "Available");
        l.getStyleClass().add("text-base");

        Button b = new Button(String.format(EGameState.FIGURE_NAMES[idx]));
        if (EGameState.INSTANCE.hasClientSelectedARobot())
        {
            if (EGameState.INSTANCE.getClientSelectedRobotID() == idx)
            {
                b.getStyleClass().add("primary-btn-mini");
            }
            else
            {
                b.getStyleClass().add("secondary-btn-mini");
            }
        }
        else
        {
            if (EGameState.INSTANCE.isPlayerRobotUnavailable(idx))
            {
                b.getStyleClass().add("secondary-btn-mini");
            }
            else
            {
                b.getStyleClass().add("primary-btn-mini");
            }
        }
        b.setOnAction(actionEvent ->
        {
            this.formErrorLabel.setText("");

            if (this.isPlayerNameValid())
            {
                if (EGameState.INSTANCE.getClientSelectedRobotID() == idx)
                {
                    LobbyJFXController_v2.l.debug("Player selected robot ({}) {}, but he already selected this robot. Ignoring.", idx, EGameState.FIGURE_NAMES[idx]);
                    return;
                }

                LobbyJFXController_v2.l.debug("Player selected robot ({}) {}.", idx, EGameState.FIGURE_NAMES[idx]);
                new PlayerValuesModel(this.getPlayerName(), idx).send();
                return;
            }

            this.formErrorLabel.setText("Invalid player name.");
            LobbyJFXController_v2.l.debug("Player selected robot ({}) {}, but player name is invalid.", idx, EGameState.FIGURE_NAMES[idx]);

            return;
        });

        VBox v = new VBox(l, b);
        v.getStyleClass().add("player-robot-selector-vbox");

        return v;
    }

    private void updateView()
    {
        this.addPlayerRobotSelector_v2();
        this.updatePlayersInSession();
        this.updateReadyBtn();

        return;
    }

    private void updatePlayersInSession()
    {
        this.playerListContainer.getChildren().clear();

        for (RemotePlayer rp : EGameState.INSTANCE.getRemotePlayers())
        {
            Label l = new Label(rp.getPlayerName());
            l.getStyleClass().add("player-in-session-label");
            this.playerListContainer.getChildren().add(l);
            continue;
        }

        return;
    }

    private void updateReadyBtn()
    {
        this.readyButton.setDisable(!EGameState.INSTANCE.hasClientSelectedARobot());
        if (EGameState.INSTANCE.getClientRemotePlayer() == null)
        {
            this.readyButton.setText("Not Ready");
            this.readyButton.getStyleClass().clear();
            this.readyButton.getStyleClass().add("secondary-btn-mini");
        }
        else
        {
            this.readyButton.setText(Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady() ? "Ready" : "Not Ready");
            this.readyButton.getStyleClass().clear();
            this.readyButton.getStyleClass().add(Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady() ? "confirm-btn-mini" : "secondary-btn-mini");
        }
        this.bReadyBtnClicked = false;

        return;
    }

    /** Will create the btn to select the different robots. */
    private void addPlayerRobotSelector_v2()
    {
        double scrollPos = this.formScrollPane.getVvalue();

        this.playerRobotSelectorArea.getChildren().clear();

        int half = EGameState.FIGURE_NAMES.length / 2;
        HBox hTop = new HBox();
        hTop.getStyleClass().add("player-robot-selector-hbox");
        for (int i = 0; i < half; i++)
        {
            hTop.getChildren().add(this.createRobotSelectorBox(i));
            if (i + 1 < half)
            {
                hTop.getChildren().add(this.createHSpacer());
            }

            continue;
        }

        HBox hBot = new HBox();
        hBot.getStyleClass().add("player-robot-selector-hbox");
        for (int i = half; i < EGameState.FIGURE_NAMES.length; i++)
        {
            hBot.getChildren().add(this.createRobotSelectorBox(i));
            if (i + 1 < EGameState.FIGURE_NAMES.length)
            {
                hBot.getChildren().add(this.createHSpacer());
            }

            continue;
        }

        this.playerRobotSelectorArea.getChildren().add(hTop);
        this.playerRobotSelectorArea.getChildren().add(hBot);

        // TODO This is highly experimental. But Platform.runLater() does not work (and ofc setting it
        //      directly does not work either). Duration has to be increased on slower hardware.
        PauseTransition p = new PauseTransition(Duration.millis(15));
        p.setOnFinished(f -> this.formScrollPane.setVvalue(scrollPos));
        p.play();

        return;
    }

    @FXML
    public void onReadyBtn(ActionEvent actionEvent)
    {
        if (this.bReadyBtnClicked)
        {
            return;
        }
        this.bReadyBtnClicked = true;

        // TODO Some input validation needed.
        new ReadyPlayerModel(!Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady()).send();
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
