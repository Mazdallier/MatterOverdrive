package com.MO.MatterOverdrive.tile;

import cofh.lib.util.helpers.MathHelper;
import com.MO.MatterOverdrive.Reference;
import com.MO.MatterOverdrive.api.inventory.UpgradeTypes;
import com.MO.MatterOverdrive.api.matter.IRecyclable;
import com.MO.MatterOverdrive.data.Inventory;
import com.MO.MatterOverdrive.data.inventory.RemoveOnlySlot;
import com.MO.MatterOverdrive.data.inventory.SlotRecycler;
import com.MO.MatterOverdrive.util.MatterHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by Simeon on 5/15/2015.
 */
public class TileEntityMachineMatterRecycler extends MOTileEntityMachineEnergy {

    public static final int ENERGY_STORAGE = 512000;
    public int OUTPUT_SLOT_ID;
    public int INPUT_SLOT_ID;
    public int recycleProgress;
    public int recycleTime;

    public static final int RECYCLE_SPEED_PER_MATTER = 80;
    public static final int RECYCLE_ENERGY_PER_MATTER = 1000;

    public TileEntityMachineMatterRecycler()
    {
        super(4);
        this.energyStorage.setCapacity(ENERGY_STORAGE);
        this.energyStorage.setMaxExtract(ENERGY_STORAGE);
        this.energyStorage.setMaxReceive(ENERGY_STORAGE);
        redstoneMode = Reference.MODE_REDSTONE_LOW;
    }

    @Override
    protected void RegisterSlots(Inventory inventory)
    {
        INPUT_SLOT_ID = inventory.AddSlot(new SlotRecycler(true));
        OUTPUT_SLOT_ID = inventory.AddSlot(new RemoveOnlySlot(false));
        super.RegisterSlots(inventory);
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        this.manageRecycle();
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt)
    {
        super.readCustomNBT(nbt);
        this.recycleTime = nbt.getShort("RecycleTime");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt)
    {
        super.writeCustomNBT(nbt);
        nbt.setShort("RecycleTime", (short)this.recycleTime);
    }

    public void manageRecycle()
    {
        if(!worldObj.isRemote)
        {
            if (this.isRecycling())
            {
                if(this.energyStorage.getEnergyStored() >= getEnergyDrainPerTick())
                {
                    this.recycleTime++;
                    extractEnergy(ForgeDirection.DOWN, getEnergyDrainPerTick(), false);
                    recycleProgress = Math.round(((float) (recycleTime) / (float) getSpeed()) * 100);

                    if (this.recycleTime >= getSpeed())
                    {
                        this.recycleTime = 0;
                        this.recycleItem();
                    }
                }
            }
        }

        if (!this.isRecycling())
        {
            this.recycleTime = 0;
            recycleProgress = 0;
        }
    }

    public boolean isRecycling()
    {
        if (getStackInSlot(INPUT_SLOT_ID) != null && getStackInSlot(INPUT_SLOT_ID).getItem() instanceof IRecyclable)
        {
            return ((IRecyclable) getStackInSlot(INPUT_SLOT_ID).getItem()).canRecycle(getStackInSlot(INPUT_SLOT_ID)) && canPutInOutput();
        }
        return false;
    }

    public int getEnergyDrainPerTick()
    {
        int maxEnergy = getEnergyDrainMax();
        return maxEnergy / getSpeed();
    }

    public int getEnergyDrainMax()
    {
        int matter = ((IRecyclable)getStackInSlot(INPUT_SLOT_ID).getItem()).getRecycleMatter(getStackInSlot(INPUT_SLOT_ID));
        double upgradeMultiply = getUpgradeMultiply(UpgradeTypes.PowerUsage);
        return MathHelper.round((matter * RECYCLE_ENERGY_PER_MATTER) * upgradeMultiply);
    }

    public int getSpeed()
    {
        int matter = ((IRecyclable)getStackInSlot(INPUT_SLOT_ID).getItem()).getRecycleMatter(getStackInSlot(INPUT_SLOT_ID));
        if (matter > 0) {
            return MathHelper.round(RECYCLE_SPEED_PER_MATTER * Math.log(RECYCLE_SPEED_PER_MATTER * matter) * getUpgradeMultiply(UpgradeTypes.Speed));
        }else
        {
            return 1;
        }
    }

    private boolean canPutInOutput()
    {
        ItemStack stack = getStackInSlot(OUTPUT_SLOT_ID);
        ItemStack inputStack = getStackInSlot(INPUT_SLOT_ID);

        if(stack == null)
        {
            return true;
        }
        else if (inputStack != null && inputStack.getItem() instanceof IRecyclable)
        {
            ItemStack outputStack = ((IRecyclable)inputStack.getItem()).getOutput(inputStack);
            if(outputStack != null && stack.isItemEqual(outputStack) && stack.stackSize + outputStack.stackSize < stack.getMaxStackSize())
            {
                return true;
            }
        }

        return false;
    }

    public void recycleItem()
    {
        if (getStackInSlot(INPUT_SLOT_ID) != null && canPutInOutput())
        {
            ItemStack outputStack = ((IRecyclable)getStackInSlot(INPUT_SLOT_ID).getItem()).getOutput(getStackInSlot(INPUT_SLOT_ID));
            ItemStack stackInOutput = getStackInSlot(OUTPUT_SLOT_ID);

            if (stackInOutput == null)
            {
                setInventorySlotContents(OUTPUT_SLOT_ID,outputStack);
            }else if (stackInOutput != null)
            {
                stackInOutput.stackSize++;
            }

            decrStackSize(INPUT_SLOT_ID,1);
            ForceSync();
        }
    }

    @Override
    public String getSound() {
        return "machine";
    }

    @Override
    public boolean hasSound() {
        return true;
    }

    @Override
    public boolean isActive() {
        return isRecycling() && this.energyStorage.getEnergyStored() >= getEnergyDrainPerTick();
    }

    @Override
    public float soundVolume() {
        return 1;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        return new int[]{INPUT_SLOT_ID,OUTPUT_SLOT_ID};
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack item, int side)
    {
        if (slot != OUTPUT_SLOT_ID)
        {
            return super.canInsertItem(slot,item,side);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack item, int side)
    {
        return slot == OUTPUT_SLOT_ID;
    }
}
