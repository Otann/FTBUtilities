package com.feed_the_beast.ftbu.client;

import com.feed_the_beast.ftbl.lib.io.LMConnection;
import com.feed_the_beast.ftbl.lib.io.RequestMethod;
import com.feed_the_beast.ftbu.badges.Badge;
import com.feed_the_beast.ftbu.badges.BadgeStorage;
import com.feed_the_beast.ftbu.net.MessageRequestBadge;

import java.util.UUID;

/**
 * Created by LatvianModder on 30.08.2016.
 */
public class CachedClientData
{
    public static final BadgeStorage GLOBAL_BADGES = new BadgeStorage();
    public static final BadgeStorage LOCAL_BADGES = new BadgeStorage();

    public static void clear()
    {
        LOCAL_BADGES.clear();
    }

    public static void reloadGlobalBadges()
    {
        GLOBAL_BADGES.clear();

        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    LMConnection connection = new LMConnection(RequestMethod.GET, "http://pastebin.com/raw/Mu8McdDR");
                    GLOBAL_BADGES.loadBadges(connection.connect().asJson());
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        };

        thread.setDaemon(true);
        thread.start();
    }

    public static Badge getClientBadge(UUID playerID)
    {
        if(LOCAL_BADGES.badgePlayerMap.containsKey(playerID))
        {
            return LOCAL_BADGES.badgePlayerMap.get(playerID);
        }

        LOCAL_BADGES.badgePlayerMap.put(playerID, null);
        new MessageRequestBadge(playerID).sendToServer();
        return null;
    }
}