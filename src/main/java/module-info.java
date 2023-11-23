module sep
{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.apache.logging.log4j;

    opens sep.view to javafx.fxml;
    exports sep.view;

    opens sep.view.scenecontrollers to javafx.fxml;
    exports sep.view.scenecontrollers;

    opens sep.view.clientcontroller to javafx.fxml;
    exports sep.view.clientcontroller;

    opens sep.view.viewcontroller to javafx.fxml;
    exports sep.view.viewcontroller;

    exports sep.view.json;
}
