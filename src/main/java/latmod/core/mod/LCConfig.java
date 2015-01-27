package latmod.core.mod;

import latmod.core.*;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class LCConfig extends LMConfig
{
	public static LCConfig instance;
	
	public LCConfig(FMLPreInitializationEvent e)
	{
		super(e, "/LatMod/LatCoreMC.cfg");
		instance = this;
		load();
	}
	
	public void load()
	{
		General.load(get("general"));
		Client.load(get("client"));
		Recipes.load(get("recipes"));
		save();
	}
	
	public static class General
	{
		public static boolean checkUpdates;
		public static boolean disableLatCoreCommand;
		
		public static void load(Category c)
		{
			checkUpdates = c.getBool("checkUpdates", true);
			disableLatCoreCommand = c.getBool("disableLatCoreCommand", false);
		}
	}
	
	public static class Client
	{
		public static boolean enablePlayerDecorators;
		public static boolean rotateBlocks;
		public static boolean renderHighlights;
		public static boolean addAllColorItems;
		
		public static boolean onlyAdvanced;
		public static boolean addOreNames;
		public static boolean addRegistryNames;
		public static boolean addFluidContainerNames;
		
		public static void load(Category c)
		{
			enablePlayerDecorators = c.getBool("enablePlayerDecorators", true);
			rotateBlocks = c.getBool("rotateBlocks", true);
			renderHighlights = c.getBool("renderHighlights", true);
			addAllColorItems = c.getBool("addAllColorItems", true);
			
			onlyAdvanced = c.getBool("onlyAdvanced", false);
			addOreNames = c.getBool("addOreNames", false);
			addRegistryNames = c.getBool("addRegistryNames", false);
			addFluidContainerNames = c.getBool("addFluidContainerNames", false);
		}
	}
	
	public static class Recipes
	{
		public static boolean smeltFleshToLeather;
		public static boolean craftWoolWithDye;
		
		public static void load(Category c)
		{
			smeltFleshToLeather = c.getBool("smeltFleshToLeather", true);
			craftWoolWithDye = c.getBool("craftWoolWithDye", true);
		}
		
		public static void loadRecipes()
		{
			if(smeltFleshToLeather)
				LC.mod.recipes.addSmelting(new ItemStack(Items.rotten_flesh), new ItemStack(Items.leather));
			
			if(craftWoolWithDye)
			{
				for(int i = 0; i < 16; i++)
					LC.mod.recipes.addRecipe(new ItemStack(Blocks.wool, 8, i), "WWW", "WDW", "WWW",
							'W', new ItemStack(Blocks.wool, 1, ODItems.ANY),
							'D', EnumDyeColor.VALUES[i].dyeName);
			}
		}
	}
}