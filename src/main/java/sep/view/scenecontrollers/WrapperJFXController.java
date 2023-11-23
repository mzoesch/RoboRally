package sep.view.scenecontrollers;

import sep.view.clientcontroller.GameInstance;

import javafx.event.ActionEvent;

public class WrapperJFXController
{
    public void onStartClientBtn(ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.CLIENT);
        GameInstance.loadInnerClient();
        return;
    }

    public void onStartServerBtn(ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.SERVER);
        GameInstance.kill();
        return;
    }

    public void onExitBtn(ActionEvent actionEvent)
    {
        sep.EArgs.setMode(sep.EArgs.EXIT);
        GameInstance.kill();
        return;
    }

}
