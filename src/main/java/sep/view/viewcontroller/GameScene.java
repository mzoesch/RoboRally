package sep.view.viewcontroller;

import javafx.scene.layout.Pane;

public record GameScene <T>(String ID, Pane screen, T controller, String fallback)
{
    public GameScene(final String ID, final Pane screen, final T controller, String fallback)
    {
        this.ID = ID;
        this.screen = screen;
        this.controller = controller;
        this.fallback = fallback;

        return;
    }

    public boolean hasFallback()
    {
        return this.fallback != null && !this.fallback.isEmpty();
    }

}
