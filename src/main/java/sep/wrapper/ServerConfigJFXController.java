package sep.wrapper;

import sep.EPort;
import sep.EArgs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ServerConfigJFXController
{
    @FXML private Label alertLabel;
    @FXML private TextField serverIPTextField;
    @FXML private TextField serverPortTextField;

    @FXML
    protected void onStartServerBtn(ActionEvent actionEvent)
    {
        this.alertLabel.setText("");

        final String ip = serverIPTextField.getText();
        final String port = serverPortTextField.getText();

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

        EArgs.setMode(EArgs.SERVER);
        Wrapper.exitWrapper();
        return;
    }

    @FXML
    protected void onCancelBtn(ActionEvent actionEvent)
    {
        Wrapper.loadWrapperMenu();
        return;
    }

    @FXML
    private void initialize()
    {
        this.serverIPTextField.setPromptText(EArgs.PREF_SERVER_IP);
        this.serverPortTextField.setPromptText(String.valueOf(EArgs.PREF_SERVER_PORT.i));

        this.serverPortTextField.textProperty().addListener((observable, o, t1) ->
        {
            if (!t1.matches("\\d*"))
            {
                this.serverPortTextField.setText(t1.replaceAll("[^\\d]", ""));
            }

            return;
        });

        return;
    }

    public void showAlert(String message)
    {
        this.alertLabel.setText(message);
        return;
    }

}
