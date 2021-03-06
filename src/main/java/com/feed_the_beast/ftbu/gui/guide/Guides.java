package com.feed_the_beast.ftbu.gui.guide;

import com.feed_the_beast.ftbl.api.info.IGuiInfoPage;
import com.feed_the_beast.ftbl.api.info.IPageIconRenderer;
import com.feed_the_beast.ftbl.gui.GuiInfo;
import com.feed_the_beast.ftbl.gui.GuiLoading;
import com.feed_the_beast.ftbl.lib.info.InfoPageHelper;
import com.feed_the_beast.ftbl.lib.info.ItemPageIconRenderer;
import com.feed_the_beast.ftbl.lib.info.TexturePageIconRenderer;
import com.feed_the_beast.ftbl.lib.info.WrappedImageProvider;
import com.feed_the_beast.ftbl.lib.util.LMJsonUtils;
import com.feed_the_beast.ftbl.lib.util.LMStringUtils;
import com.feed_the_beast.ftbl.lib.util.LMUtils;
import com.feed_the_beast.ftbu.api.guide.ClientGuideEvent;
import com.feed_the_beast.ftbu.api.guide.GuideFormat;
import com.feed_the_beast.ftbu.api.guide.IGuide;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LatvianModder on 02.10.2016.
 */
public class Guides
{
    public static final Comparator<IGuide> COMPARATOR = (o1, o2) ->
    {
        int i = Integer.compare(o2.getPriority(), o1.getPriority());

        if(i == 0)
        {
            i = o1.getPage().getDisplayName().getFormattedText().compareToIgnoreCase(o2.getPage().getDisplayName().getFormattedText());
        }

        return i;
    };

    private static final InfoPageGuides INFO_PAGE = new InfoPageGuides();
    private static boolean isReloading = false;
    private static Thread reloadingThread = null;
    private static GuiInfo cachedGui = null;

    public static void setShouldReload()
    {
        cachedGui = null;
    }

    public static void openGui()
    {
        if(cachedGui == null)
        {
            if(!isReloading)
            {
                isReloading = true;

                new GuiLoading()
                {
                    @Override
                    public void startLoading()
                    {
                        reloadingThread = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                reloadGuides();
                                isReloading = false;
                            }
                        };
                        reloadingThread.start();
                    }

                    @Override
                    public boolean isLoading()
                    {
                        return isReloading;
                    }

                    @Override
                    public void finishLoading()
                    {
                        reloadingThread = null;
                        cachedGui = new GuiInfo(INFO_PAGE);
                        cachedGui.openGui();
                    }
                }.openGui();
            }
        }
        else if(!isReloading)
        {
            cachedGui.openGui();
        }
    }

    private static void reloadGuides()
    {
        LMUtils.DEV_LOGGER.info("Reloading guides...");
        INFO_PAGE.clear();
        INFO_PAGE.setTitle(new TextComponentString("Guides")); //TODO: Lang

        List<IGuide> guides = new ArrayList<>();

        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();

        for(String domain : resourceManager.getResourceDomains())
        {
            try
            {
                IResource resource = resourceManager.getResource(new ResourceLocation(domain, "guide.json"));
                JsonElement infoFile = LMJsonUtils.fromJson(new InputStreamReader(resource.getInputStream()));

                if(infoFile.isJsonObject())
                {
                    InfoPageGuide guide = new InfoPageGuide(domain, infoFile.getAsJsonObject());
                    GuideFormat format = guide.getFormat();

                    if(format == GuideFormat.UNSUPPORTED)
                    {
                        guide.getPage().println("Unsupported format!"); //TODO: Lang
                        guide.getPage().println("Please update FTBUtilities or contact mod author!"); //TODO: Lang
                    }
                    else
                    {
                        loadTree(resourceManager, domain, guide.getPage(), format, "guide");
                    }

                    guides.add(guide);
                }

                LMUtils.DEV_LOGGER.info("Guide found in domain '" + domain + "'");
            }
            catch(Exception ex)
            {
                LMUtils.DEV_LOGGER.info("Error while loading guide from domain '" + domain + "'");

                if(!(ex instanceof FileNotFoundException))
                {
                    ex.printStackTrace();
                }
            }
        }

        Map<String, IGuide> eventMap = new HashMap<>();
        MinecraftForge.EVENT_BUS.post(new ClientGuideEvent(eventMap));
        guides.addAll(eventMap.values());

        Collections.sort(guides, COMPARATOR);

        for(IGuide guide : guides)
        {
            INFO_PAGE.addSub(guide.getPage());
        }

        INFO_PAGE.cleanup();
        INFO_PAGE.sortAll();
    }

    private static void loadTree(IResourceManager resourceManager, String domain, IGuiInfoPage page, GuideFormat format, String parentDir) throws Exception
    {
        try
        {
            switch(format)
            {
                case JSON:
                    for(JsonElement e : LMJsonUtils.fromJson(new InputStreamReader(resourceManager.getResource(new ResourceLocation(domain, parentDir + "/index.json")).getInputStream())).getAsJsonArray())
                    {
                        page.println(InfoPageHelper.createLine(page, e));
                    }
                    break;
                case MD:
                    for(String s : LMStringUtils.readStringList(resourceManager.getResource(new ResourceLocation(domain, parentDir + "/README.md")).getInputStream()))
                    {
                        //FIXME: Support more than just text
                        page.println(s);
                    }
                    break;
            }
        }
        catch(Exception ex)
        {
            if(LMUtils.DEV_ENV && !(ex instanceof FileNotFoundException))
            {
                ex.printStackTrace();
            }
        }

        try
        {
            for(JsonElement e : LMJsonUtils.fromJson(new InputStreamReader(resourceManager.getResource(new ResourceLocation(domain, parentDir + "/pages.json")).getInputStream())).getAsJsonArray())
            {
                IGuiInfoPage page1;

                if(e.isJsonObject())
                {
                    JsonObject o = e.getAsJsonObject();

                    IPageIconRenderer pageIcon = null;

                    if(o.has("icon"))
                    {
                        pageIcon = new TexturePageIconRenderer(new WrappedImageProvider(new ResourceLocation(o.get("icon").getAsString())));
                    }
                    else if(o.has("icon_item"))
                    {
                        pageIcon = new ItemPageIconRenderer(o.get("icon_item").getAsString());
                    }

                    page1 = new InfoPageGuide.Page(o.get("id").getAsString(), pageIcon);

                    if(o.has("lang"))
                    {
                        page1.setTitle(new TextComponentTranslation(o.get("lang").getAsString()));
                    }
                    else
                    {
                        page1.setTitle(new TextComponentTranslation(domain + '.' + parentDir.replace('/', '.') + "." + page1.getName()));
                    }

                    page.addSub(page1);
                }
                else
                {
                    page1 = page.getSub(e.getAsString());
                    page1.setTitle(new TextComponentTranslation(domain + '.' + parentDir.replace('/', '.') + "." + page1.getName()));
                }

                loadTree(resourceManager, domain, page1, format, parentDir + "/" + page1.getName());
            }
        }
        catch(Exception ex)
        {
            if(LMUtils.DEV_ENV && !(ex instanceof FileNotFoundException))
            {
                ex.printStackTrace();
            }
        }
    }
}