package sep.view.lib;

import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

public enum EUpgradeCard
{
    ADMIN_PRIVILEGE(    "Admin Privilege",  3,  true    ),
    REAR_LASER(         "Rear Laser",       2,  true   ),
    MEMORY_SWAP(        "Memory Swap",      1,  false   ),
    SPAM_BLOCKER(       "Spam Blocker",     3,  false   ),
    ;

    private static final Logger l = LogManager.getLogger(EUpgradeCard.class);

    private final String    name;
    private final int       energy;
    private final boolean   bIsPermanent;

    private EUpgradeCard(final String name, final int energy, final boolean bIsPermanent)
    {
        this.name           = name;
        this.energy         = energy;
        this.bIsPermanent   = bIsPermanent;

        return;
    }

    public String getName()
    {
        return this.name;
    }

    public int getEnergy()
    {
        return this.energy;
    }

    public boolean isPermanent()
    {
        return this.bIsPermanent;
    }

    public static EUpgradeCard fromString(final String name)
    {
        final String check = name.replaceAll("\\s+", "").toLowerCase();

        for (final EUpgradeCard card : EUpgradeCard.values())
        {
            final String cardFormatted = card.getName().replaceAll("\\s+", "").toLowerCase();
            if (cardFormatted.equals(check))
            {
                return card;
            }
        }

        l.warn("Could not find upgrade card with name: {}", name);

        return null;
    }

}
