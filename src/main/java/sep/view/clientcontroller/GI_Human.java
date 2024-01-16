package sep.view.clientcontroller;

import sep.view.viewcontroller.     ViewSupervisor;

import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

public final class GI_Human extends GameInstance
{
    private static final Logger l = LogManager.getLogger(GI_Human.class);

    public GI_Human()
    {
        super();
        l.info("Creating Game Instance for main thread (JavaFX). Constructing window.");
        return;
    }

    @Override
    public void run()
    {
        ViewSupervisor.run();
        return;
    }

}
