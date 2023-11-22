package sep;

public class Launcher
{
    private Launcher() throws RuntimeException
    {
        super();
        throw new RuntimeException("This class cannot be instantiated.");
    }

    /**
     * When wrapping.
     *
     * @param args Are always passed down.
     */
    public static void main(String[] args)
    {
        double t0 = System.currentTimeMillis();

        System.out.printf("[WRAPPER] Wrapping main methods.%n");

        String[] targs = new String[args.length + 1];
        targs[0] = "wrap";
        System.arraycopy(args, 0, targs, 1, args.length);

        sep.view.Launcher.main(targs);

        System.out.printf("[WRAPPER] The wrapper took %.2f seconds to run.%n", (System.currentTimeMillis() - t0) / 1000);

        if (EArgs.getMode() == EArgs.DEFAULT)
        {
            System.err.println("[WRAPPER] The wrapper did not receive a return code from the GUI. Shutting down.");
            System.exit(EArgs.ERROR);
            return;
        }

        if (EArgs.getMode() == EArgs.CLIENT)
        {
            System.out.printf("[WRAPPER] Client closed. Shutting down.%n");
            System.exit(EArgs.OK);
            return;
        }

        if (EArgs.getMode() == EArgs.SERVER)
        {
            System.out.printf("[WRAPPER] Launching server.%n");
            sep.server.Launcher.main(args);
            System.exit(EArgs.OK);
            return;
        }

        if (EArgs.getMode() == EArgs.EXIT)
        {
            System.out.printf("[WRAPPER] Shutting down.%n");
            System.exit(EArgs.OK);
            return;
        }

        System.out.printf("[WRAPPER] The wrapper received an invalid return code from the GUI: %d.%n", EArgs.getMode());
        System.exit(EArgs.ERROR);

        return;
    }

}
