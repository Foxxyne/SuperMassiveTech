package tterrag.supermassivetech.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import tterrag.supermassivetech.SuperMassiveTech;
import tterrag.supermassivetech.registry.Achievements;
import tterrag.supermassivetech.util.BlockCoord;
import tterrag.supermassivetech.util.Utils;
import codechicken.lib.math.MathHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class AchievementHandler
{
    @SubscribeEvent
    public void onCrafted(ItemCraftedEvent event)
    {
        if (!event.player.worldObj.isRemote)
        {
            Achievements.unlock(Achievements.getValidItemStack(event.crafting), (EntityPlayerMP) event.player);

            if (event.crafting.getItem() == SuperMassiveTech.itemRegistry.heartOfStar)
            {
                IInventory inv = event.craftMatrix;

                for (int i = 0; i < inv.getSizeInventory(); i++)
                {
                    ItemStack curStack = inv.getStackInSlot(i);
                    if (curStack != null && curStack.getItem() == Items.nether_star && curStack.hasTagCompound()
                            && curStack.getTagCompound().getBoolean("wasRejuvenated"))
                    {
                        Achievements.unlock(Achievements.getValidItemStack(new ItemStack(SuperMassiveTech.itemRegistry.heartOfStar, 1, 1)),
                                (EntityPlayerMP) event.player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        EntityPlayer player = event.player;
        int fireworksLeft = player.getEntityData().getInteger("fireworksLeft");
        if (event.phase == Phase.END && fireworksLeft > 0
                && (!player.getEntityData().getBoolean("fireworkDelay") || player.worldObj.getTotalWorldTime() % 20 == 0))
        {
            Utils.spawnFireworkAround(getBlockCoord(player), player.worldObj.provider.dimensionId);
            player.getEntityData().setInteger("fireworksLeft", fireworksLeft - 1);
            player.getEntityData().setBoolean("fireworkDelay", true);

            if (fireworksLeft == 1)
            {
                for (int i = 0; i < 5; i++)
                {
                    Utils.spawnFireworkAround(getBlockCoord(player), player.worldObj.provider.dimensionId);
                }
            }
        }
    }

    private BlockCoord getBlockCoord(EntityPlayer player)
    {
        return new BlockCoord(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY),
                MathHelper.floor_double(player.posZ));
    }
}
