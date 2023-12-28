package sep.view.scenecontrollers;

import sep.view.json.lobby.PlayerValuesModel;
import sep.view.lib.Types.EFigure;
import sep.view.viewcontroller.ViewSupervisor;
import sep.view.clientcontroller.GameInstance;
import sep.view.clientcontroller.EGameState;
import sep.view.json.ChatMsgModel;
import sep.view.clientcontroller.RemotePlayer;
import sep.view.clientcontroller.EClientInformation;
import sep.view.json.lobby.SetStatusModel;
import sep.view.json.lobby.CourseSelectedModel;
import sep.view.viewcontroller.SceneController;

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
import javafx.event.ActionEvent;
import org.json.JSONException;

public final class LobbyJFXController_v2
{
    private static final Logger l = LogManager.getLogger(LobbyJFXController_v2.class);

    private boolean bReadyBtnClicked;
    private boolean bSelectBtnClicked;

    public LobbyJFXController_v2()
    {
        super();
        this.bReadyBtnClicked = false;
        this.bSelectBtnClicked = false;
        return;
    }

    @FXML private VBox playersInSessionLabelContainer;
    @FXML private Label serverCourseLabel;
    @FXML private VBox serverCourseSelectorArea;
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

    @FXML
    private void initialize()
    {
        HBox.setHgrow(this.playersInSessionLabelContainer, Priority.ALWAYS);
        HBox.setHgrow(this.playerNameContainer, Priority.ALWAYS);
        VBox.setVgrow(this.lobbyMsgScrollPane, Priority.ALWAYS);
        HBox.setHgrow(this.readyLabelContainer, Priority.ALWAYS);
        HBox.setHgrow(this.readyLabelContainerWrapper, Priority.ALWAYS);

        this.readyLabelContainerWrapper.getChildren().add(1, this.createVSpacer());
        this.readyLabelContainer.getChildren().add(1, this.createHSpacer());

        this.lobbyMsgInputTextField.lengthProperty().addListener(
        new ChangeListener<Number>()
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

        this.formArea.minWidthProperty().bind(Bindings.createDoubleBinding(
        () ->
            this.formScrollPane.getViewportBounds().getWidth(), this.formScrollPane.viewportBoundsProperty()
        ));

        this.updateAvailableCourses();

        boolean bSuccess = false;
        try
        {
            bSuccess = GameInstance.connectToSessionPostLogin();
        }
        catch (IOException e)
        {
            ViewSupervisor.getSceneController().killCurrentScreen();
            return;
        }
        catch (JSONException e)
        {
            l.error("Client did not understand the server's JSON.");
            l.error(e.getMessage());
            ViewSupervisor.getSceneController().killCurrentScreen();
            return;
        }

        if (!bSuccess)
        {
            ViewSupervisor.getSceneController().killCurrentScreen();
            return;
        }

        this.sessionIDLabel.setText(String.format("Session ID: %s", EClientInformation.INSTANCE.getPreferredSessionID()));
        this.updateSessionCourseLabel();

        l.debug("Successfully connected to session [{}].", EClientInformation.INSTANCE.getPreferredSessionID());

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

            /* Kinda sketchy. But is there a better way? */
            PauseTransition p = new PauseTransition(Duration.millis(15));
            p.setOnFinished(f -> this.lobbyMsgScrollPane.setVvalue(1.0));
            p.play();

            return;
        }

        if (caller == ChatMsgModel.CLIENT_ID)
        {
            Label l = new Label(String.format("[%s] %s", ChatMsgModel.CLIENT_NAME, msg));
            l.getStyleClass().add("lobby-msg-client");
            l.setWrapText(true);
            this.lobbyMsgContainer.getChildren().add(l);

            /* Kinda sketchy. But is there a better way? */
            PauseTransition p = new PauseTransition(Duration.millis(15));
            p.setOnFinished(f -> this.lobbyMsgScrollPane.setVvalue(1.0));
            p.play();

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

        /* Kinda sketchy. But is there a better way? */
        PauseTransition p = new PauseTransition(Duration.millis(15));
        p.setOnFinished(f -> this.lobbyMsgScrollPane.setVvalue(1.0));
        p.play();

        return;
    }

    /** Will add a new chat msg from another thread. */
    public void handleChatMessage(int sourceID, String msg, boolean bIsPrivate)
    {
        Platform.runLater(() -> {
            this.addToChatMsgToScrollPane(sourceID, msg, bIsPrivate);
            return;
        });

        return;
    }

    /** Will update the player status from another thread. */
    public void handlePlayerStatusUpdate()
    {
        Platform.runLater(() -> {
            this.updateView();
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

    private Node createRobotSelectorBox(final EFigure f)
    {
        Label l = new Label(EGameState.INSTANCE.isPlayerRobotUnavailable(f) ? EGameState.INSTANCE.getRemotePlayerByFigureID(f) == null ? "Available" : Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByFigureID(f)).getPlayerName() : "Available");
        l.getStyleClass().add("text-base");

        Button b = new Button(f.toString());
        if (EGameState.INSTANCE.hasClientSelectedARobot())
        {
            if (EGameState.INSTANCE.getClientSelectedFigure() == f)
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
            if (EGameState.INSTANCE.isPlayerRobotUnavailable(f))
            {
                b.getStyleClass().add("secondary-btn-mini");
            }
            else
            {
                b.getStyleClass().add("primary-btn-mini");
            }
        }
        if (EGameState.INSTANCE.getRemotePlayerByFigureID(f) != null)
        {
            b.setDisable(EGameState.INSTANCE.getClientSelectedFigure() != f);
        }
        b.setOnAction(actionEvent ->
        {
            this.formErrorLabel.setText("");

            if (this.isPlayerNameValid())
            {
                if (EGameState.INSTANCE.getClientSelectedFigure() == f)
                {
                    if (!Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getPlayerName().equals(this.getPlayerName()))
                    {
                        LobbyJFXController_v2.l.debug("Player selected robot ({}) {}.", f.i, f.toString());
                        new PlayerValuesModel(this.getPlayerName(), f).send();
                        return;
                    }

                    LobbyJFXController_v2.l.debug("Player selected robot ({}) {}, but he already selected this robot and the name did not change. Ignoring.", f, f.toString());
                    return;
                }

                if (EGameState.INSTANCE.isPlayerRobotUnavailable(f))
                {
                    LobbyJFXController_v2.l.debug("Player selected robot ({}) {}, but this robot is already taken. Ignoring.", f, f.toString());
                    return;
                }

                LobbyJFXController_v2.l.debug("Player selected robot ({}) {}.", f, f.toString());
                new PlayerValuesModel(this.getPlayerName(), f).send();
                return;
            }

            this.formErrorLabel.setText("Invalid player name.");
            LobbyJFXController_v2.l.debug("Player selected robot ({}) {}, but player name is invalid.", f, f.toString());

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
        this.updateSessionCourseLabel();

        return;
    }

    private void updatePlayersInSession()
    {
        this.playerListContainer.getChildren().clear();

        for (RemotePlayer rp : EGameState.INSTANCE.getRemotePlayers())
        {
            if (rp.getPlayerName().startsWith(EClientInformation.AGENT_PREFIX))
            {
                Label l = new Label(rp.getPlayerName());
                l.getStyleClass().clear();
                l.getStyleClass().add("player-in-session-label-ready-agent");
                l.setPrefWidth(1_000);

                Button b = new Button("Remove");
                b.setStyle("-fx-min-width: 55px;");
                b.getStyleClass().add("danger-btn-tiny");

                b.setOnAction(actionEvent ->
                {
                    LobbyJFXController_v2.l.debug("User wants to remove the agent [{}].", rp.getPlayerID());
                    EClientInformation.INSTANCE.sendRemoveAgentRequest(rp.getPlayerID());
                    return;
                });

                HBox h = new HBox(l, b);
                h.getStyleClass().add("player-in-session-hbox-agent");
                this.playerListContainer.getChildren().add(h);
                continue;
            }

            Label l = new Label(rp.getPlayerName());
            l.getStyleClass().clear();
            l.getStyleClass().add(rp.isReady() ? "player-in-session-label-ready" : "player-in-session-label-not-ready");
            l.getStyleClass().add(rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID() ? "player-in-session-label-client" : "");
            l.setPrefWidth(1_000);
            this.playerListContainer.getChildren().add(l);
            continue;
        }

        return;
    }

    private void updateReadyBtn()
    {
        this.readyButton.setDisable(!EGameState.INSTANCE.hasClientSelectedARobot());
        this.readyButton.getStyleClass().clear();
        if (EGameState.INSTANCE.getClientRemotePlayer() == null)
        {
            this.readyButton.setText("Not Ready");
            this.readyButton.getStyleClass().add("secondary-btn-mini");
        }
        else
        {
            this.readyButton.setText(Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady() ? "Ready" : "Not Ready");
            this.readyButton.getStyleClass().add(Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady() ? "confirm-btn-mini" : "secondary-btn-mini");
            if (!EGameState.INSTANCE.getClientRemotePlayer().isReady())
            {
                EGameState.INSTANCE.setServerCourses(new String[0]);
                this.updateAvailableCourses();
            }
        }
        this.bReadyBtnClicked = false;

        return;
    }

    public void updateAvailableCourses()
    {
        this.updateAvailableCourses(false);
        return;
    }

    public void updateAvailableCourses(boolean bScrollToEnd)
    {
        this.bSelectBtnClicked = false;

        Platform.runLater(() ->
        {
            double scrollPos = this.formScrollPane.getVvalue();
            this.updateSessionCourseLabel();
            this.serverCourseSelectorArea.getChildren().clear();

            if (EGameState.INSTANCE.getServerCourses().length == 0)
            {
                Label l = new Label("Your are currently not allowed to select a course.");
                l.getStyleClass().add("text-base");
                this.serverCourseSelectorArea.getChildren().add(l);
                return;
            }

            for (int i = 0; i < EGameState.INSTANCE.getServerCourses().length; i++)
            {
                if (i % 2 == 0)
                {
                    this.serverCourseSelectorArea.getChildren().add(new HBox() {{ getStyleClass().add("server-course-selector-hbox"); }});
                }

                Button b = new Button(EGameState.INSTANCE.getServerCourses()[i]);
                if (EGameState.INSTANCE.getCurrentServerCourse().isEmpty())
                {
                    b.getStyleClass().add("primary-btn-mini");
                    b.setStyle("-fx-min-width: 250px;");
                }
                else
                {
                    if (Objects.equals(EGameState.INSTANCE.getCurrentServerCourse(), EGameState.INSTANCE.getServerCourses()[i]))
                    {
                        b.getStyleClass().add("primary-btn-mini");
                        b.setStyle("-fx-min-width: 250px;");
                    }
                    else
                    {
                        b.getStyleClass().add("secondary-btn-mini");
                        b.setStyle("-fx-min-width: 250px;");
                    }
                }
                int finalI = i;
                b.setOnAction(actionEvent ->
                {
                    l.debug("Player clicked course selection button.");
                    if (this.bSelectBtnClicked)
                    {
                        return;
                    }
                    this.bSelectBtnClicked = true;

                    if (Objects.equals(EGameState.INSTANCE.getCurrentServerCourse(), EGameState.INSTANCE.getServerCourses()[finalI]))
                    {
                        LobbyJFXController_v2.l.debug("Player selected course {}, but this course is already the server course. Ignoring.", EGameState.INSTANCE.getServerCourses()[finalI]);
                        this.bSelectBtnClicked = false;
                        return;
                    }
                    l.debug("Player selected course {}.", EGameState.INSTANCE.getServerCourses()[finalI]);

                    new CourseSelectedModel(EGameState.INSTANCE.getServerCourses()[finalI]).send();

                    return;
                });

                ( (HBox) this.serverCourseSelectorArea.getChildren().get(this.serverCourseSelectorArea.getChildren().size() - 1)).getChildren().add(b);

                continue;
            }

            if (bScrollToEnd)
            {
                // This is super ludicrous and should be changed :D. Works for now
                // but could cause unpredictable behavior in the future.
                /* Must be higher than addPlayerRobotSelector_v2 delay. */
                PauseTransition p = new PauseTransition(Duration.millis(30));
                p.setOnFinished(f -> this.formScrollPane.setVvalue(1.0));
                p.play();
                return;
            }

            PauseTransition p = new PauseTransition(Duration.millis(15));
            p.setOnFinished(f -> this.formScrollPane.setVvalue(scrollPos));
            p.play();

            return;
        });

        return;
    }

    public void updateCourseSelected()
    {
        Platform.runLater(() ->
        {
            this.updateAvailableCourses();
            return;
        });

        return;
    }

    private void updateSessionCourseLabel()
    {
        this.serverCourseLabel.setText(String.format("Course: %s", EGameState.INSTANCE.getCurrentServerCourse().isBlank() || EGameState.INSTANCE.getCurrentServerCourse().isEmpty() ? "None" : EGameState.INSTANCE.getCurrentServerCourse()));
        return;
    }

    /** Will create the btn to select the different robots. */
    private void addPlayerRobotSelector_v2()
    {
        double scrollPos = this.formScrollPane.getVvalue();

        this.playerRobotSelectorArea.getChildren().clear();

        int half = EFigure.NUM.i / 2;
        HBox hTop = new HBox();
        hTop.getStyleClass().add("player-robot-selector-hbox");
        for (int i = 0; i < half; i++)
        {
            hTop.getChildren().add(this.createRobotSelectorBox(EFigure.fromInt(i)));
            if (i + 1 < half)
            {
                hTop.getChildren().add(this.createHSpacer());
            }

            continue;
        }

        HBox hBot = new HBox();
        hBot.getStyleClass().add("player-robot-selector-hbox");
        for (int i = half; i < EFigure.NUM.i; i++)
        {
            hBot.getChildren().add(this.createRobotSelectorBox(EFigure.fromInt(i)));
            if (i + 1 < EFigure.NUM.i)
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

    public void onPlayerRemoved()
    {
        Platform.runLater(() ->
        {
            this.updateView();
            return;
        });

        return;
    }

    @FXML
    private void onLeaveBtn(final ActionEvent actionEvent)
    {
        l.debug("Player clicked leave button.");
        GameInstance.handleServerDisconnect();
        ViewSupervisor.getSceneController().renderExistingScreen(SceneController.MAIN_MENU_ID);

        return;
    }

    @FXML
    private void onReadyBtn(final ActionEvent actionEvent)
    {
        l.debug("Player clicked ready button.");
        if (this.bReadyBtnClicked)
        {
            return;
        }
        this.bReadyBtnClicked = true;

        // TODO Some input validation needed.
        l.debug("Player wants to be {}.", Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady() ? "not ready" : "ready");
        new SetStatusModel(!Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady()).send();
        return;
    }

    @FXML
    private void onAddAgentBtn(final ActionEvent actionEvent)
    {
        l.debug("Client wants to add a bot to this session.");
        EClientInformation.INSTANCE.sendAddAgentRequest();
        return;
    }

    // region Getters and Setters

    private String getCommand(final String token)
    {
        if (!token.contains(" "))
        {
            return token.substring(1);
        }

        return token.substring(1, token.indexOf(" "));
    }

    private boolean isChatMsgACommand(final String token)
    {
        return token.startsWith(ChatMsgModel.COMMAND_PREFIX);
    }

    private boolean isChatMsgValid(final String token)
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
