module sep
{
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.apache.logging.log4j;
    requires javafx.                swing;
    requires java.                  desktop;

    exports sep;
    opens sep to javafx.fxml;

    opens sep.view to javafx.fxml;
    exports sep.view;

    opens sep.view.scenecontrollers to javafx.fxml;
    exports sep.view.scenecontrollers;

    opens sep.view.clientcontroller to javafx.fxml;
    exports sep.view.clientcontroller;

    opens sep.view.viewcontroller to javafx.fxml;
    exports sep.view.viewcontroller;

    opens sep.wrapper to javafx.fxml;
    exports sep.wrapper;

    exports sep.view.json;
    exports sep.view.lib;
}
