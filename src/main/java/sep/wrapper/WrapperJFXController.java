package sep.wrapper;

import javafx.application.Platform;
import javafx.event.ActionEvent;

public class WrapperJFXController
{
    public void onStartClientBtn(final ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.CLIENT);
        WrapperJFXController.exitWrapper();
        return;
    }

    public void onStartServerBtn(final ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.SERVER);
        WrapperJFXController.exitWrapper();
        return;
    }

    public void onExitBtn(final ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.EXIT);
        WrapperJFXController.exitWrapper();
        return;
    }

    public static void exitWrapper()
    {
        Platform.exit();
        return;
    }

}
