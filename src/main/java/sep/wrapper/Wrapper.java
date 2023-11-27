package sep.wrapper;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.stage.WindowEvent;

public final class Wrapper extends Application
{
    private static final String path = "wrapper.fxml";
    private static final int PREF_WIDTH = 1_280;
    private static final int PREF_HEIGHT = 720;
    private static final String TITLE = "WRAPPER";

    public Wrapper()
    {
        super();
        return;
    }

    @Override
    public void start(final Stage s) throws IOException
    {
        final FXMLLoader ldr = new FXMLLoader(Wrapper.class.getResource(Wrapper.path));
        final Pane p = ldr.load();
        s.setScene(new Scene(new Parent() {}, Wrapper.PREF_WIDTH, Wrapper.PREF_HEIGHT));
        s.setTitle(String.format("%s v-", Wrapper.TITLE));
        s.getScene().setRoot(p);

        s.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e ->
        {
            sep.EArgs.setMode(sep.EArgs.EXIT);
            WrapperJFXController.exitWrapper();
        });

        s.show();

        return;
    }

    public static void run()
    {
        Application.launch();
        return;
    }

}
