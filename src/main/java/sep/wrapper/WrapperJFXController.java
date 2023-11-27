package sep.wrapper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class WrapperJFXController
{
    @FXML
    protected void onStartClientBtn(final ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.CLIENT);
        Wrapper.exitWrapper();
        return;
    }

    @FXML
    protected void onStartServerBtn(final ActionEvent actionEvent)
    {
        Wrapper.loadServerConfig();
        return;
    }

    @FXML
    protected void onExitBtn(final ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.EXIT);
        Wrapper.exitWrapper();
        return;
    }

}
