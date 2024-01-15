package sep.view.viewcontroller;

import javafx.scene.layout.Pane;

/**
 * Holds useful information about a rendered screen.
 *
 * @param id            The ID of the screen.
 * @param screen        The actual scene (pane).
 * @param controller    The controller of the scene.
 * @param fallback      The fallback scene to go to when this screen is destroyed.
 * @param <T>           The type of the controller.
 */
public record RGameScene<T>(String id, Pane screen, T controller, String fallback)
{
    public RGameScene(final String id, final Pane screen, final T controller, String fallback)
    {
        this.id             = id;
        this.screen         = screen;
        this.controller     = controller;
        this.fallback       = fallback;

        return;
    }

    /**
     * If a screen is destroyed the {@link SceneController} will try its best to get back to
     * the screen rendered before. If this fails, the game will force quit within an instant.
     */
    public boolean hasFallback()
    {
        return this.fallback != null && !this.fallback.isEmpty();
    }

}
