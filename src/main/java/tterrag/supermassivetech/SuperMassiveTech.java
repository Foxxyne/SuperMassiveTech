package tterrag.supermassivetech;

import net.minecraft.creativetab.CreativeTabs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tterrag.supermassivetech.block.waypoint.BreakWaypointHandler;
import tterrag.supermassivetech.config.ConfigHandler;
import tterrag.supermassivetech.item.TooltipHandler;
import tterrag.supermassivetech.item.armor.ClientKeyHandler;
import tterrag.supermassivetech.item.armor.GravityArmorHandler;
import tterrag.supermassivetech.item.armor.HelmetOverlayHandler;
import tterrag.supermassivetech.lib.Reference;
import tterrag.supermassivetech.network.GuiHandler;
import tterrag.supermassivetech.network.PacketHandler;
import tterrag.supermassivetech.proxy.CommonProxy;
import tterrag.supermassivetech.registry.ModBlocks;
import tterrag.supermassivetech.registry.ModEnchants;
import tterrag.supermassivetech.registry.ModItems;
import tterrag.supermassivetech.registry.Stars;
import tterrag.supermassivetech.util.Constants;
import tterrag.supermassivetech.util.Utils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

/**
 * @author Garrett Spicer-Davis
 */
@Mod(modid = Reference.MODID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class SuperMassiveTech
{
    @Instance
    public static SuperMassiveTech instance;

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    public static Logger logger = LogManager.getLogger("SuperMassiveTech");

    public static ModItems itemRegistry = ModItems.instance;
    public static ModEnchants enchantRegistry = ModEnchants.instance;
    public static ModBlocks blockRegistry = ModBlocks.instance;
    public static Stars starRegistry = Stars.instance;

    public static CreativeTabs tabSMT = Utils.tab;

    public static int renderIDStorage, renderIDHopper, renderIDStarHarvester, renderIDWaypoint;
    
    @EventHandler
    public static void preInit(FMLPreInitializationEvent event)
    {
        ConfigHandler.init(event.getSuggestedConfigurationFile());

        Constants.init();
        Utils.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        proxy.registerRenderers();
        proxy.registerKeybinds();

        itemRegistry.register();
        blockRegistry.register();

        enchantRegistry.init();

        starRegistry.registerDefaultStars();
    }

    @EventHandler
    public static void init(FMLInitializationEvent event)
    {
        proxy.init();
        
        PacketHandler.init();
        
        itemRegistry.addRecipes();
        blockRegistry.addRecipes();

        Utils.registerEventHandlers(false, GravityArmorHandler.class);
        Utils.registerEventHandlers(true, TooltipHandler.class, BreakWaypointHandler.class);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            Utils.registerEventHandlers(false, ClientKeyHandler.class);
            Utils.registerEventHandlers(true, HelmetOverlayHandler.class);
        }
    }
}
