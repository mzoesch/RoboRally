package sep.view.viewcontroller;

import javafx.scene.layout.Pane;

/**
 * Holds useful information about a rendered screen.
 *
 * @param ID            The ID of the screen.
 * @param screen        The actual scene (pane).
 * @param controller    The controller of the scene.
 * @param fallback      The fallback scene to go to when this screen is destroyed.
 * @param <T>           The type of the controller.
 */
public record RGameScene<T>(String ID, Pane screen, T controller, String fallback)
{
    public RGameScene(final String ID, final Pane screen, final T controller, String fallback)
    {
        this.ID = ID;
        this.screen = screen;
        this.controller = controller;
        this.fallback = fallback;

        return;
    }

    /**
     * If a screen is destroyed the {@link SceneController} will try its best to get to back to
     * the screen rendered before. If this fails, the game will force quit within an instant.
     */
    public boolean hasFallback()
    {
        return this.fallback != null && !this.fallback.isEmpty();
    }

}
