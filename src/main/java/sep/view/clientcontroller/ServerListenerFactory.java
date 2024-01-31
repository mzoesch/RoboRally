package sep.view.clientcontroller;

import java.util.concurrent.ThreadFactory;

public final class ServerListenerFactory implements ThreadFactory
{
    private static int instantiations = 0;

    private final String prefix;

    public ServerListenerFactory(final String prefix)
    {
        this.prefix = prefix;
        return;
    }

    private String getName()
    {
        return String.format("%s-thread-%d", this.prefix, ServerListenerFactory.instantiations++);
    }

    @Override
    public Thread newThread(final Runnable r)
    {
        final Thread t = new Thread(r);
        t.setName(this.getName());
        return t;
    }

}
