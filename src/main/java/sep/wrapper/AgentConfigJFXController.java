package sep.wrapper;

import sep.server.viewmodel.        EServerInstance;
import sep.view.clientcontroller.   GameInstance;
import sep.                         Types;
import sep.                         EArgs;
import sep.server.model.game.       GameState;

import javafx.scene.text.       TextAlignment;
import javafx.fxml.             FXML;
import javafx.event.            ActionEvent;
import javafx.scene.control.    TextField;
import javafx.scene.control.    CheckBox;
import javafx.scene.control.    Label;
import javafx.scene.control.    Button;
import javafx.geometry.         Pos;
import javafx.scene.            Node;
import javafx.scene.layout.     Priority;
import javafx.scene.layout.     HBox;
import javafx.scene.layout.     VBox;
import javafx.scene.layout.     AnchorPane;
import javafx.application.      Platform;

public final class AgentConfigJFXController
{
    @FXML private CheckBox      bNoCloseTerminal;
    @FXML private TextField     sessionIDTextField;
    @FXML private Button        addAgentBtn;
    @FXML private VBox          addedAgentsContainer;
    @FXML private TextField     serverPortTextField;
    @FXML private TextField     serverIPTextField;
    @FXML private TextField     minHumanPlayersTextField;
    @FXML private CheckBox      bAllowServerStart;
    @FXML private CheckBox      bAllowClientStart;

    @FXML
    private void onCancelBtn(final ActionEvent actionEvent)
    {
        Wrapper.loadWrapperMenu();
        return;
    }

    private AnchorPane getRoot()
    {
        return (AnchorPane) this.sessionIDTextField.getScene().getRoot();
    }

    private void showAlert(final String s)
    {

        final Label l = new Label(s);
        l.getStyleClass().add("text-xl-error");
        l.setWrapText(true);
        l.setTextAlignment(TextAlignment.CENTER);

        final Button b = new Button("OK");
        b.getStyleClass().add("secondary-btn");

        final VBox v = new VBox(l, b);
        v.getStyleClass().add("alert");

        final HBox h = new HBox(v);
        h.setStyle("-fx-alignment: center;");
        final AnchorPane ap = new AnchorPane(h);

        AnchorPane.setTopAnchor(        h,  0.0     );
        AnchorPane.setBottomAnchor(     h,  0.0     );
        AnchorPane.setLeftAnchor(       h,  0.0     );
        AnchorPane.setRightAnchor(      h,  0.0     );

        AnchorPane.setTopAnchor(        ap, 0.0     );
        AnchorPane.setBottomAnchor(     ap, 0.0     );
        AnchorPane.setLeftAnchor(       ap, 0.0     );
        AnchorPane.setRightAnchor(      ap, 0.0     );

        b.setOnAction(e ->
        {
            this.getRoot().getChildren().remove(ap);
            return;
        });

        this.getRoot().getChildren().add(ap);

        return;
    }

    private void showAlertLater(final String s)
    {
        Platform.runLater(() ->
        {
            this.showAlert(s);
            return;
        });

        return;
    }

    @FXML
    private void onConfirmBtn(ActionEvent actionEvent)
    {

        if (this.bAllowServerStart.isSelected() && !(this.serverIPTextField.getText().isEmpty() || this.serverIPTextField.getText().isBlank()))
        {
            this.showAlertLater("Cannot start server with a custom IP address.");
            return;
        }

        EArgs.getAgentNames().clear();
        for (final Node n : this.addedAgentsContainer.getChildren())
        {
            final String s = ( (TextField) ( (HBox) n ).getChildren().get(0) ).getText();

            if (s.isEmpty() || s.isBlank())
            {
                this.showAlertLater("An Agent name cannot be empty.");
                return;
            }

            if (s.length() > GameInstance.MAX_PLAYER_NAME_LENGTH)
            {
                this.showAlertLater(String.format("An Agent name cannot be longer than %d characters.", GameInstance.MAX_PLAYER_NAME_LENGTH));
                return;
            }

            EArgs.getAgentNames().add(s);

            continue;
        }

        final String    sessionID               = this.sessionIDTextField.getText();
        final String    port                    = this.serverPortTextField.getText();
        final String    ip                      = this.serverIPTextField.getText();
        final String    sid                     = this.sessionIDTextField.getText();
        final String    minHumanPlayers         = this.minHumanPlayersTextField.getText();
        final boolean   bAllowServerStart       = this.bAllowServerStart.isSelected();
        final boolean   bAllowClientStart       = this.bAllowClientStart.isSelected();
        final boolean   bNoClose                = this.bNoCloseTerminal.isSelected();

        if (!(sessionID.isEmpty() || sessionID.isBlank()))
        {
            EArgs.setCustomSessionID(sessionID);
        }

        if (!(port.isEmpty() || port.isBlank()))
        {
            if (Integer.parseInt(port) > Types.EPort.MAX.i || Integer.parseInt(port) < Types.EPort.MIN.i)
            {
                this.showAlert("The port number is out of bounds.");
                return;
            }

            EArgs.setCustomServerPort(Integer.parseInt(port));
        }

        if (!(ip.isEmpty() || ip.isBlank()))
        {
            EArgs.setCustomServerIP(ip);
        }

        if (!(sid.isEmpty() || sid.isBlank()))
        {
            EArgs.setCustomSessionID(sid);
        }

        if (!(minHumanPlayers.isEmpty() || minHumanPlayers.isBlank()))
        {
            if (Integer.parseInt(minHumanPlayers) < 0 || Integer.parseInt(minHumanPlayers) > GameState.MAX_CONTROLLERS_ALLOWED)
            {
                this.showAlert("Invalid minimum human players number.");
                return;
            }

            EArgs.setCustomMinHumanPlayers(Integer.parseInt(minHumanPlayers));
        }

        EArgs.setAllowServerStart(bAllowServerStart);
        EArgs.setAllowClientStart(bAllowClientStart);
        EArgs.setNoClose(bNoClose);

        EArgs.setMode(EArgs.EMode.AGENT);
        Wrapper.exitWrapper();

        return;
    }

    @FXML
    private void initialize()
    {
        this.sessionIDTextField         .setPromptText(Types.EProps.DESCRIPTION.toString());
        this.serverIPTextField          .setPromptText(EArgs.PREF_SERVER_IP);
        this.serverPortTextField        .setPromptText(String.valueOf(EArgs.PREF_SERVER_PORT.i));
        this.minHumanPlayersTextField   .setPromptText(String.valueOf(GameState.DEFAULT_MIN_HUMAN_PLAYER_COUNT_TO_START));
        this.bAllowServerStart          .setSelected(true);
        this.bAllowClientStart          .setSelected(true);
        this.bNoCloseTerminal           .setSelected(false);

        return;
    }

    private HBox createAgentBox()
    {
        final TextField tf = new TextField();
        tf.getStyleClass().add("text-field-agent-name");
        tf.setPromptText("Agent Name");
        tf.setText(EServerInstance.createRandomAgentName());

        HBox.setHgrow(tf, Priority.ALWAYS);

        final Button b = new Button("X");
        b.getStyleClass().add("danger-btn-mini");

        final HBox agentBox = new HBox(tf, b);
        agentBox.setSpacing(10);
        agentBox.setAlignment(Pos.CENTER);

        b.setOnAction(actionEvent ->
        {
            this.addedAgentsContainer.getChildren().remove(agentBox);
            this.addAgentBtn.setDisable(false);
            return;
        });

        return agentBox;
    }

    public void onAddAgentBtn(ActionEvent actionEvent)
    {
        this.addedAgentsContainer.getChildren().add(this.createAgentBox());

        if (this.addedAgentsContainer.getChildren().size() >= EArgs.MAX_AGENT_COUNT)
        {
            this.addAgentBtn.setDisable(true);
        }

        return;
    }
}
