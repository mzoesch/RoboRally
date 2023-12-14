package sep.wrapper;

import sep.EPort;
import sep.EArgs;
import sep.server.model.game.GameState;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public final class ServerConfigJFXController
{
    @FXML private Label alertLabel;
    @FXML private TextField serverIPTextField;
    @FXML private TextField serverPortTextField;
    @FXML private TextField minRemotePlayersTextField;

    @FXML
    private void onStartServerBtn(final ActionEvent actionEvent)
    {
        this.alertLabel.setText("");

        final String ip = this.serverIPTextField.getText();
        final String port = this.serverPortTextField.getText();
        final String minRemotePlayers = this.minRemotePlayersTextField.getText();

        if (!(port.isEmpty() || port.isBlank()))
        {
            if (Integer.parseInt(port) < EPort.MIN.i || Integer.parseInt(port) > EPort.MAX.i)
            {
                this.showAlert("Invalid port number.");
                return;
            }

            EArgs.setCustomServerPort(Integer.parseInt(port));
        }

        if (!(ip.isEmpty() || ip.isBlank()))
        {
            EArgs.setCustomServerIP(ip);
        }

        if (!(minRemotePlayers.isEmpty() || minRemotePlayers.isBlank()))
        {
            if (Integer.parseInt(minRemotePlayers) < GameState.MIN_CONTROLLERS_ALLOWED || Integer.parseInt(minRemotePlayers) > GameState.MAX_CONTROLLERS_ALLOWED)
            {
                this.showAlert("Invalid minimum remote players number.");
                return;
            }

            EArgs.setCustomMinRemotePlayers(Integer.parseInt(minRemotePlayers));
        }

        EArgs.setMode(EArgs.SERVER);
        Wrapper.exitWrapper();
        return;
    }

    @FXML
    private void onCancelBtn(final ActionEvent actionEvent)
    {
        Wrapper.loadWrapperMenu();
        return;
    }

    @FXML
    private void initialize()
    {
        this.serverIPTextField.setPromptText(EArgs.PREF_SERVER_IP);
        this.serverPortTextField.setPromptText(String.valueOf(EArgs.PREF_SERVER_PORT.i));
        this.minRemotePlayersTextField.setPromptText(String.valueOf(GameState.DEFAULT_MIN_REMOTE_PLAYER_COUNT_TO_START));

        this.serverPortTextField.textProperty().addListener((observable, o, t1) ->
        {
            if (!t1.matches("\\d*"))
            {
                this.serverPortTextField.setText(t1.replaceAll("[^\\d]", ""));
            }

            return;
        });

        this.minRemotePlayersTextField.textProperty().addListener((observable, o, t1) ->
        {
            if (!t1.matches("\\d*"))
            {
                this.minRemotePlayersTextField.setText(t1.replaceAll("[^\\d]", ""));
            }

            if (Integer.parseInt(t1) < GameState.MIN_CONTROLLERS_ALLOWED || Integer.parseInt(t1) > GameState.MAX_CONTROLLERS_ALLOWED)
            {
                this.minRemotePlayersTextField.setText(o);
            }

            return;
        });

        return;
    }

    public void showAlert(final String message)
    {
        this.alertLabel.setText(message);
        return;
    }

}
