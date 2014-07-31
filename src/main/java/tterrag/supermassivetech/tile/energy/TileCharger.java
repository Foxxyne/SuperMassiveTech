package tterrag.supermassivetech.tile.energy;

import java.util.List;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tterrag.supermassivetech.lib.Reference;
import tterrag.supermassivetech.network.PacketHandler;
import tterrag.supermassivetech.network.message.MessageChargerUpdate;
import tterrag.supermassivetech.util.Utils;
import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class TileCharger extends TileSMTEnergy implements ISidedInventory
{
    private boolean hadItem = false;

    public TileCharger()
    {
        super(10000);

        inventory = new ItemStack[1];

        setOutputSpeed(1000);
        setInputSpeed(1000);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (!worldObj.isRemote)
        {
            if (inventory[0] != null && inventory[0].getItem() instanceof IEnergyContainerItem)
            {
                IEnergyContainerItem item = (IEnergyContainerItem) inventory[0].getItem();
                int canTake = item.receiveEnergy(inventory[0], getOutputSpeed(), true);
                int canGive = storage.extractEnergy(getOutputSpeed(), true);

                if (canTake >= canGive)
                {
                    item.receiveEnergy(inventory[0], canGive, false);
                    storage.extractEnergy(canGive, false);
                }
                else
                {
                    item.receiveEnergy(inventory[0], canTake, false);
                    storage.extractEnergy(canTake, false);
                }

                if (canTake > 0 && canGive > 0)
                {
                    markDirty();
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                }
            }

            if (inventory[0] != null != hadItem && !worldObj.isRemote)
            {
                markDirty();
                sendPacket();
                hadItem = inventory[0] != null;
            }
        }
    }

    public boolean isCharging()
    {
        if (inventory[0] == null || !(inventory[0].getItem() instanceof IEnergyContainerItem))
        {
            return false;
        }
        
        IEnergyContainerItem item = (IEnergyContainerItem) inventory[0].getItem();
        
        return getEnergyStored() > 0 && item.getEnergyStored(inventory[0]) < item.getMaxEnergyStored(inventory[0]);
    }

    private void sendPacket()
    {
        PacketHandler.INSTANCE.sendToAllAround(new MessageChargerUpdate(xCoord, yCoord, zCoord, getEnergyStored(), inventory[0]), getPacketRange());
    }

    private TargetPoint getPacketRange()
    {
        return new TargetPoint(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 5);
    }

    @Override
    public String getInventoryName()
    {
        return Reference.LOCALIZING + ".tile.charger";
    }

    @Override
    public boolean isGravityWell()
    {
        return false;
    }

    @Override
    public boolean showParticles()
    {
        return isGravityWell();
    }

    @Override
    public ForgeDirection[] getValidOutputs()
    {
        return new ForgeDirection[] {};
    }

    @Override
    public ForgeDirection[] getValidInputs()
    {
        return ForgeDirection.VALID_DIRECTIONS;
    }

    /* ISidedInventory */

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return new int[] { 0 };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side)
    {
        return stack != null && slot == 0;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side)
    {
        return canInsertItem(slot, stack, side);
    }
    
    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    /* IWailaAdditionalInfo */

    @Override
    public void getWailaInfo(List<String> tooltip, int x, int y, int z, World world)
    {
        super.getWailaInfo(tooltip, x, y, z, world);

        String str = EnumChatFormatting.WHITE + Utils.localize("tooltip.itemStorage", true) + ": ";

        if (inventory[0] != null && inventory[0].getItem() instanceof IEnergyContainerItem)
        {
            IEnergyContainerItem item = (IEnergyContainerItem) inventory[0].getItem();
            int power = item.getEnergyStored(inventory[0]);
            str += String.format("%s / %s", Utils.formatString(Utils.getColorForPowerLeft(power, item.getMaxEnergyStored(inventory[0])).toString(), " RF", power, true, true),
                    Utils.formatString("", " RF", item.getMaxEnergyStored(inventory[0]), true, true));
        }
        else
        {
            str += EnumChatFormatting.GRAY + Utils.localize("tooltip.na", true);
        }

        tooltip.add(str);
        
        tooltip.add(EnumChatFormatting.WHITE + Utils.localize("tooltip.redstone.mode", true) + ": " + (getBlockMetadata() == 0 ? EnumChatFormatting.AQUA + Utils.localize("tooltip.redstone.normal", true) : EnumChatFormatting.YELLOW + Utils.localize("tooltip.redstone.inverted", true)));
    }
}
