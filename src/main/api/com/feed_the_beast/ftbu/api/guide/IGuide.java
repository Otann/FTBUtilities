package com.feed_the_beast.ftbu.api.guide;

import com.feed_the_beast.ftbl.api.info.IGuiInfoPage;

import java.util.List;

/**
 * Created by LatvianModder on 02.10.2016.
 */
public interface IGuide
{
    IGuiInfoPage getPage();

    GuideType getType();

    GuideFormat getFormat();

    List<String> getAuthors();

    List<String> getGuideAuthors();

    int getPriority();
}