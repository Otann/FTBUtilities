package com.feed_the_beast.ftbu.client;

import com.feed_the_beast.ftbl.api.config.ConfigValue;
import com.feed_the_beast.ftbl.lib.config.PropertyBool;
import com.feed_the_beast.ftbu.FTBUFinals;

/**
 * Created by LatvianModder on 13.09.2016.
 */
public class FTBUClientConfig
{
    @ConfigValue(id = "render_badges", file = FTBUFinals.MOD_ID, client = true)
    public static final PropertyBool RENDER_BADGES = new PropertyBool(true);

    @ConfigValue(id = "light_value_texture_x", file = FTBUFinals.MOD_ID, client = true)
    public static final PropertyBool LIGHT_VALUE_TEXTURE_X = new PropertyBool(false);

    @ConfigValue(id = "enable_chunk_selector_depth", file = FTBUFinals.MOD_ID, client = true, isHidden = true)
    public static final PropertyBool ENABLE_CHUNK_SELECTOR_DEPTH = new PropertyBool(false);

    @ConfigValue(id = "journeymap_overlay", file = FTBUFinals.MOD_ID, client = true)
    public static final PropertyBool JOURNEYMAP_OVERLAY = new PropertyBool(true);
}