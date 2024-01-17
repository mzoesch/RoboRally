package sep.view.scenecontrollers;

import sep.view.json.lobby.         PlayerValuesModel;
import sep.view.json.lobby.         SetStatusModel;
import sep.view.json.lobby.         CourseSelectedModel;
import sep.view.lib.                EFigure;
import sep.view.lib.                OutErr;
import sep.view.lib.                EPopUp;
import sep.view.clientcontroller.   GameInstance;
import sep.view.clientcontroller.   EGameState;
import sep.view.clientcontroller.   RemotePlayer;
import sep.view.clientcontroller.   EClientInformation;
import sep.view.json.               ChatMsgModel;
import sep.view.lib.RPopUpMask;
import sep.view.viewcontroller.     ViewSupervisor;
import sep.view.viewcontroller.     SceneController;

import javafx.application.          Platform;
import javafx.fxml.                 FXML;
import javafx.scene.control.        ScrollPane;
import javafx.scene.control.        Button;
import javafx.scene.control.        TextField;
import javafx.scene.control.        Label;
import javafx.scene.                Node;
import javafx.scene.layout.         Priority;
import javafx.scene.layout.         HBox;
import javafx.scene.layout.         VBox;
import javafx.scene.layout.         Region;
import javafx.scene.layout.         Pane;
import java.io.                     IOException;
import javafx.scene.input.          KeyCode;
import javafx.beans.binding.        Bindings;
import java.util.                   Objects;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import javafx.beans.value.          ChangeListener;
import javafx.beans.value.          ObservableValue;
import javafx.util.                 Duration;
import javafx.animation.            PauseTransition;
import org.json.                    JSONException;
import javafx.event.                ActionEvent;

public final class LobbyJFXController_v2
{
    private static final Logger l = LogManager.getLogger(LobbyJFXController_v2.class);

    private static final int SCROLL_TO_END_DELAY = 15;

    private boolean     bReadyBtnClicked;
    private boolean     bSelectBtnClicked;

    public LobbyJFXController_v2()
    {
        super();

        this.bReadyBtnClicked   = false;
        this.bSelectBtnClicked  = false;

        return;
    }

    @FXML private Button        addBotBtn;
    @FXML private VBox          playersInSessionLabelContainer;
    @FXML private Label         serverCourseLabel;
    @FXML private VBox          serverCourseSelectorArea;
    @FXML private Button        readyButton;
    @FXML private VBox          playerListContainer;
    @FXML private VBox          readyLabelContainerWrapper;
    @FXML private ScrollPane    formScrollPane;
    @FXML private VBox          formArea;
    @FXML private Label         sessionIDLabel;
    @FXML private TextField     lobbyMsgInputTextField;
    @FXML private ScrollPane    lobbyMsgScrollPane;
    @FXML private VBox          playerNameContainer;
    @FXML private TextField     playerNameField;
    @FXML private Label         formErrorLabel;
    @FXML private HBox          playerRobotsSelectorContainer;
    @FXML private VBox          playerRobotSelectorArea;
    @FXML private HBox          readyLabelContainer;

    private VBox lobbyMsgContainer;

    @FXML
    private void initialize()
    {
        HBox.setHgrow(  this.playersInSessionLabelContainer,    Priority.ALWAYS );
        HBox.setHgrow(  this.playerNameContainer,               Priority.ALWAYS );
        VBox.setVgrow(  this.lobbyMsgScrollPane,                Priority.ALWAYS );
        HBox.setHgrow(  this.readyLabelContainer,               Priority.ALWAYS );
        HBox.setHgrow(  this.readyLabelContainerWrapper,        Priority.ALWAYS );

        this.readyLabelContainerWrapper .getChildren().add(1, this.createVSpacer());
        this.readyLabelContainer        .getChildren().add(1, this.createHSpacer());

        this.lobbyMsgInputTextField.lengthProperty().addListener(
        new ChangeListener<Number>()
        {
            @Override
            public void changed(final ObservableValue<? extends Number> observableValue, final Number number, final Number t1)
            {
                if (t1.intValue() > EGameState.MAX_CHAT_MESSAGE_LENGTH)
                {
                    lobbyMsgInputTextField.setText(lobbyMsgInputTextField.getText().substring(0, EGameState.MAX_CHAT_MESSAGE_LENGTH));
                }

                return;
            }
        });

        this.playerNameField.lengthProperty().addListener(
        new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1)
            {
                if (t1.intValue() > GameInstance.MAX_PLAYER_NAME_LENGTH)
                {
                    playerNameField.setText(playerNameField.getText().substring(0, GameInstance.MAX_PLAYER_NAME_LENGTH));
                }

                return;
            }
        });

        this.lobbyMsgInputTextField.setOnKeyPressed((
        keyEvent ->
        {
            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.ENTER)
            {
                this.onSubmitLobbyMsg();
            }

            return;
        }
        ));

        this.updateView();

        this.lobbyMsgContainer = new VBox();
        this.lobbyMsgContainer.setId("lobby-msg-scroll-pane-inner");
        this.lobbyMsgScrollPane.setContent(this.lobbyMsgContainer);

        this.formArea.minWidthProperty().bind(Bindings.createDoubleBinding(
        () ->
            this.formScrollPane.getViewportBounds().getWidth(), this.formScrollPane.viewportBoundsProperty()
        ));

        this.onAvailableCourseUpdate();

        final OutErr outErr = new OutErr();
        final boolean bSuccess;
        try
        {
            bSuccess = GameInstance.connectToSessionPostLogin(outErr);
        }
        catch (final IOException e)
        {
            ViewSupervisor.getSceneController().killCurrentScreen();
            return;
        }
        catch (final JSONException e)
        {
            l.error("Client did not understand the server's JSON.");
            l.error(e.getMessage());
            ViewSupervisor.getSceneController().killCurrentScreen();
            return;
        }

        if (!bSuccess)
        {
            EClientInformation.INSTANCE.resetServerConnectionAfterDisconnect();

            ViewSupervisor.getSceneController().killCurrentScreen();

            if (outErr.isSet())
            {
                new Thread(() ->
                {
                    try
                    {
                        Thread.sleep(SceneController.MAIN_MENU_REROUTING_DELAY + 100);
                    }
                    catch (final InterruptedException e)
                    {
                        l.fatal("Interrupted while waiting to show error message.");
                        l.fatal(e.getMessage());
                        GameInstance.kill();
                        return;
                    }

                    ViewSupervisor.createPopUpLater(new RPopUpMask(EPopUp.ERROR, outErr.get()));

                    return;
                })
                .start();

                return;
            }

            return;
        }

        if (!EClientInformation.INSTANCE.getAllowLegacyAgents())
        {
            ( (Pane) this.addBotBtn.getParent() ).getChildren().remove(this.addBotBtn);
        }

        this.sessionIDLabel.setText(String.format("Session ID: %s", EClientInformation.INSTANCE.getPreferredSessionID()));
        this.updateSessionCourseLabel();

        l.debug("Successfully connected to session [{}].", EClientInformation.INSTANCE.getPreferredSessionID());

        return;
    }

    // region Rendering Methods

    private Node createRobotSelectorBox(final EFigure f)
    {
        if (f == null)
        {
            l.error("Could not create robot selector box. Figure is null.");
            return null;
        }

        final Label     l   = new Label(f.toString());
        final Button    b   = new Button(EGameState.INSTANCE.isPlayerRobotUnavailable(f) ? EGameState.INSTANCE.getRemotePlayerByFigureID(f) == null ? "Available" : Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByFigureID(f)).getPlayerName() : "Available");

        l.getStyleClass().add("text-base");

        b.getStyleClass().add(
              EGameState.INSTANCE.hasClientSelectedARobot()
            ? EGameState.INSTANCE.getClientSelectedFigure() == f
            ? "primary-btn-mini"
            : "secondary-btn-mini"
            : EGameState.INSTANCE.isPlayerRobotUnavailable(f)
            ? "secondary-btn-mini"
            : "primary-btn-mini"
        );
        b.setStyle("-fx-min-width: 170px;");

        if (EGameState.INSTANCE.getRemotePlayerByFigureID(f) != null)
        {
            b.setDisable(EGameState.INSTANCE.getClientSelectedFigure() != f);
        }
        b.setOnAction(
        actionEvent ->
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

        final VBox v = new VBox(l, b);
        v.getStyleClass().add("player-robot-selector-vbox");

        return v;
    }

    private void addChatMsgToScrollPane(final int caller, final String msg, final boolean bIsPrivate)
    {
        if (caller == ChatMsgModel.SERVER_ID)
        {
            final Label l = new Label(String.format("[%s] %s", ChatMsgModel.SERVER_NAME, msg));
            l.getStyleClass().add("lobby-msg-server");
            l.setWrapText(true);
            this.lobbyMsgContainer.getChildren().add(l);

            /* Kinda sketchy. But is there a better way? */
            final PauseTransition p = new PauseTransition(Duration.millis(LobbyJFXController_v2.SCROLL_TO_END_DELAY));
            p.setOnFinished(f -> this.lobbyMsgScrollPane.setVvalue(1.0));
            p.play();

            return;
        }

        if (caller == ChatMsgModel.CLIENT_ID)
        {
            final Label l = new Label(String.format("[%s] %s", ChatMsgModel.CLIENT_NAME, msg));
            l.getStyleClass().add("lobby-msg-client");
            l.setWrapText(true);
            this.lobbyMsgContainer.getChildren().add(l);

            /* Kinda sketchy. But is there a better way? */
            final PauseTransition p = new PauseTransition(Duration.millis(LobbyJFXController_v2.SCROLL_TO_END_DELAY));
            p.setOnFinished(f -> this.lobbyMsgScrollPane.setVvalue(1.0));
            p.play();

            return;
        }

        final Label l = new Label(String.format("<%s>%s %s", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(caller)).getPlayerName(), bIsPrivate ? " whispers: " : "", msg));
        l.getStyleClass().add(bIsPrivate ? "lobby-msg-whisper" : "lobby-msg");
        l.setWrapText(true);
        this.lobbyMsgContainer.getChildren().add(l);

        /* Kinda sketchy. But is there a better way? */
        final PauseTransition p = new PauseTransition(Duration.millis(LobbyJFXController_v2.SCROLL_TO_END_DELAY));
        p.setOnFinished(f -> this.lobbyMsgScrollPane.setVvalue(1.0));
        p.play();

        return;
    }

    private void addPlayerRobotSelector_v2()
    {
        final double scrollPos = this.formScrollPane.getVvalue();

        this.playerRobotSelectorArea.getChildren().clear();

        for (int i = 0; i < EFigure.NUM.i; i++)
        {
            if (i % 2 == 0)
            {
                this.playerRobotSelectorArea.getChildren().add(new HBox() {{ this.getStyleClass().add("player-robot-selector-hbox"); }});
            }

            ( (HBox) this.playerRobotSelectorArea.getChildren().get(this.playerRobotSelectorArea.getChildren().size() - 1) ).getChildren().add(this.createRobotSelectorBox(EFigure.fromInt(i)));

            continue;
        }

        // TODO This is highly experimental. But Platform.runLater() does not work (and ofc setting it
        //      directly does not work either). Duration has to be increased on slower end devices.
        final PauseTransition p = new PauseTransition(Duration.millis(LobbyJFXController_v2.SCROLL_TO_END_DELAY));
        p.setOnFinished(f -> this.formScrollPane.setVvalue(scrollPos));
        p.play();

        return;
    }

    private void updatePlayersInSession()
    {
        this.playerListContainer.getChildren().clear();

        for (final RemotePlayer rp : EGameState.INSTANCE.getRemotePlayers())
        {
            if (rp.getPlayerName().startsWith(EClientInformation.AGENT_PREFIX))
            {
                final Label l = new Label(rp.getPlayerName());
                l.getStyleClass().clear();
                l.getStyleClass().add("player-in-session-label-ready-agent");
                l.setPrefWidth(1_000);

                final Button b = new Button("Remove");
                b.setStyle("-fx-min-width: 55px;");
                b.getStyleClass().add("danger-btn-tiny");

                b.setOnAction(actionEvent ->
                {
                    LobbyJFXController_v2.l.debug("User wants to remove the agent [{}].", rp.getPlayerID());
                    EClientInformation.INSTANCE.sendRemoveAgentRequest(rp.getPlayerID());
                    return;
                });

                final HBox h = new HBox(l, b);
                h.getStyleClass().add("player-in-session-hbox-agent");
                this.playerListContainer.getChildren().add(h);
                continue;
            }

            final Label l = new Label(rp.getPlayerName());
            l.getStyleClass().clear();
            l.getStyleClass().add(rp.isReady()      ? "player-in-session-label-ready"           : "player-in-session-label-not-ready"           );
            l.getStyleClass().add(rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID() ? "player-in-session-label-client"      : ""    );
            l.setPrefWidth(1_000); /* We want the label to take the whole space and center itself. */
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
                this.onAvailableCourseUpdate();
            }
        }

        this.bReadyBtnClicked = false;

        return;
    }

    private void updateSessionCourseLabel()
    {
        this.serverCourseLabel.setText(String.format("Course: %s", EGameState.INSTANCE.getCurrentServerCourse().isBlank() || EGameState.INSTANCE.getCurrentServerCourse().isEmpty() ? "None" : EGameState.INSTANCE.getCurrentServerCourse()));
        return;
    }

    private void updateView()
    {
        this.addPlayerRobotSelector_v2();
        this.updatePlayersInSession();
        this.updateReadyBtn();
        this.updateSessionCourseLabel();

        return;
    }

    // endregion Rendering Methods

    // region Update Methods

    public void onCourseSelected()
    {
        Platform.runLater(() ->
        {
            this.onAvailableCourseUpdate();
            return;
        });

        return;
    }

    public void onPlayerStatusUpdate()
    {
        Platform.runLater(() ->
        {
            this.updateView();
            return;
        });

        return;
    }

    public void onPlayerSelectionUpdate()
    {
        Platform.runLater(() ->
        {
            this.updateView();
            return;
        });
    }

    public void onChatMsg(final int sourceID, final String msg, final boolean bIsPrivate)
    {
        Platform.runLater(() ->
        {
            this.addChatMsgToScrollPane(sourceID, msg, bIsPrivate);
            return;
        });

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

    public void onAvailableCourseUpdate()
    {
        this.onAvailableCourseUpdate(false);
        return;
    }

    public void onAvailableCourseUpdate(final boolean bScrollToEnd)
    {
        this.bSelectBtnClicked = false;

        Platform.runLater(() ->
        {
            final double scrollPos = this.formScrollPane.getVvalue();

            this.updateSessionCourseLabel();
            this.serverCourseSelectorArea.getChildren().clear();

            if (EGameState.INSTANCE.getServerCourses().length == 0)
            {
                final Label l = new Label("Your are currently not allowed to select a course.");
                l.getStyleClass().add("text-base");
                this.serverCourseSelectorArea.getChildren().add(l);
                return;
            }

            for (int i = 0; i < EGameState.INSTANCE.getServerCourses().length; i++)
            {
                if (i % 2 == 0)
                {
                    this.serverCourseSelectorArea.getChildren().add(new HBox() {{ this.getStyleClass().add("server-course-selector-hbox"); }});
                }

                final Button b = new Button(EGameState.INSTANCE.getServerCourses()[i]);
                b.getStyleClass().add(
                      EGameState.INSTANCE.getCurrentServerCourse().isEmpty()
                    ? "primary-btn-mini"
                    : Objects.equals(EGameState.INSTANCE.getCurrentServerCourse(), EGameState.INSTANCE.getServerCourses()[i])
                    ? "primary-btn-mini"
                    : "secondary-btn-mini"
                );
                b.setStyle("-fx-min-width: 250px;");

                final int finalI = i;
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

                ( (HBox) this.serverCourseSelectorArea.getChildren().get(this.serverCourseSelectorArea.getChildren().size() - 1) ).getChildren().add(b);

                continue;
            }

            if (bScrollToEnd)
            {
                // TODO
                //      This is super ludicrous and should be changed :D. Works for now
                //      but could cause unpredictable behavior in the future. The delay
                //      must be higher than the {@link #addPlayerRobotSelector_v2} delay.
                final PauseTransition p = new PauseTransition(Duration.millis(LobbyJFXController_v2.SCROLL_TO_END_DELAY * 2));
                p.setOnFinished(f -> this.formScrollPane.setVvalue(1.0));
                p.play();
                return;
            }

            final PauseTransition p = new PauseTransition(Duration.millis(LobbyJFXController_v2.SCROLL_TO_END_DELAY));
            p.setOnFinished(f -> this.formScrollPane.setVvalue(scrollPos));
            p.play();

            return;
        });

        return;
    }

    // endregion Update Methods

    // region Game Events

    private void onSubmitLobbyMsg()
    {
        final String token = this.getChatMsg();

        this.lobbyMsgInputTextField.clear();

        if (this.isChatMsgACommand(token))
        {
            if (this.getCommand(token).isEmpty() || this.getCommand(token).isBlank())
            {
                this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Type /h for help on commands.", false);
                return;
            }

            if (this.getCommand(token).equals("w"))
            {
                if (!token.contains("\""))
                {
                    this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }

                final int idxBSBegin = token.indexOf("\"");
                final String sub = token.substring(idxBSBegin + 1);
                if (!sub.contains("\""))
                {
                    this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }

                final int idxBSEnd = sub.indexOf("\"");
                final String targetPlayer = token.substring(idxBSBegin + 1, idxBSBegin + idxBSEnd + 1);
                if (targetPlayer.isEmpty() || targetPlayer.isBlank())
                {
                    this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }

                final String msgToWhisper;
                try
                {
                    msgToWhisper = token.substring(idxBSBegin + idxBSEnd + 3);
                }
                catch (final IndexOutOfBoundsException e)
                {
                    this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Invalid message.", false);
                    return;
                }
                if (msgToWhisper.isEmpty() || msgToWhisper.isBlank())
                {
                    return;
                }

                final RemotePlayer target = EGameState.INSTANCE.getRemotePlayerByPlayerName(targetPlayer);
                if (target == null)
                {
                    this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, String.format("Player %s not found.", targetPlayer), false);
                    return;
                }

                new ChatMsgModel(msgToWhisper, target.getPlayerID()).send();
                if (EClientInformation.INSTANCE.getPlayerID() != target.getPlayerID())
                {
                    this.addChatMsgToScrollPane(EClientInformation.INSTANCE.getPlayerID(), msgToWhisper, true);
                }

                l.debug("Whispering to {}.", targetPlayer);

                return;
            }

            if (this.getCommand(token).equals("h"))
            {
                this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "Commands:", false);
                this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "/h - Show this help.", false);
                this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, "/w [\"player name\"] [msg] - Whisper to a player.", false);
                return;
            }

            this.addChatMsgToScrollPane(ChatMsgModel.CLIENT_ID, String.format("Unknown command: %s", this.getCommand(token)), false);

            return;
        }

        if (!this.isChatMsgValid(token))
        {
            return;
        }

        new ChatMsgModel(token, ChatMsgModel.CHAT_MSG_BROADCAST).send();

        return;
    }

    @FXML
    private void onLeaveBtn(final ActionEvent actionEvent)
    {
        l.debug("Player clicked leave button.");
        EClientInformation.INSTANCE.setDisconnectHandled(true);
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

        /* TODO Some input validation needed. */
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

    // endregion Game Events

    // region Getters and Setters

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
