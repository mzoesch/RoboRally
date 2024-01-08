package sep.wrapper;

import javafx.event.    ActionEvent;
import javafx.fxml.     FXML;

public final class WrapperJFXController
{
    @FXML
    private void onStartClientBtn(final ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.CLIENT);
        Wrapper.exitWrapper();
        return;
    }

    @FXML
    private void onStartServerBtn(final ActionEvent actionEvent)
    {
        Wrapper.loadServerConfig();
        return;
    }

    @FXML
    private void onExitBtn(final ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.EXIT);
        Wrapper.exitWrapper();
        return;
    }

}
