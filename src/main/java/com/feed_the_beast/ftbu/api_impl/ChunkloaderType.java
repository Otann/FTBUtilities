package com.feed_the_beast.ftbu.api_impl;

import com.feed_the_beast.ftbl.lib.EnumNameMap;

public enum ChunkloaderType
{
    DISABLED,
    OFFLINE,
    ONLINE;

    public static final EnumNameMap<ChunkloaderType> NAME_MAP = new EnumNameMap<>(values(), false);
}