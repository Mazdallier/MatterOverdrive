package com.MO.MatterOverdrive.tile;

import cofh.lib.util.TimeTracker;

import cofh.lib.util.helpers.MathHelper;
import com.MO.MatterOverdrive.MatterOverdrive;
import com.MO.MatterOverdrive.Reference;
import com.MO.MatterOverdrive.api.inventory.UpgradeTypes;
import com.MO.MatterOverdrive.api.matter.IMatterConnection;
import com.MO.MatterOverdrive.api.matter.IMatterHandler;
import com.MO.MatterOverdrive.api.network.IMatterNetworkConnection;
import com.MO.MatterOverdrive.data.Inventory;
import com.MO.MatterOverdrive.data.inventory.MatterSlot;
import com.MO.MatterOverdrive.data.inventory.RemoveOnlySlot;
import com.MO.MatterOverdrive.init.MatterOverdriveItems;
import com.MO.MatterOverdrive.items.MatterDust;
import com.MO.MatterOverdrive.network.packet.client.PacketMatterUpdate;
import com.MO.MatterOverdrive.util.MatterHelper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class TileEntityMachineDecomposer extends MOTileEntityMachineMatter implements ISidedInventory, IMatterConnection
{
	public static final int MATTER_STORAGE = 1024;
	public static final int ENERGY_STORAGE = 512000;
    public  static  final  int MATTER_EXTRACT_SPEED = 100;
    public  static  final float FAIL_CHANGE = 0.05f;

    public static final int DECEOPOSE_SPEED_PER_MATTER = 80;
    public static final int DECOMPOSE_ENERGY_PER_MATTER = 8000;

    public int INPUT_SLOT_ID;
    public int OUTPUT_SLOT_ID;

    private TimeTracker time;
    private static Random random = new Random();
	public int decomposeTime;
    public int decomposeProgress;
	
	public TileEntityMachineDecomposer()
	{
		super(4);
        this.energyStorage.setCapacity(ENERGY_STORAGE);
        this.energyStorage.setMaxExtract(ENERGY_STORAGE);
        this.energyStorage.setMaxReceive(ENERGY_STORAGE);

        this.matterStorage.setCapacity(MATTER_STORAGE);
        this.matterStorage.setMaxReceive(MATTER_STORAGE);
        this.matterStorage.setMaxExtract(MATTER_STORAGE);
        time = new TimeTracker();
        redstoneMode = Reference.MODE_REDSTONE_LOW;
	}

    @Override
    protected void RegisterSlots(Inventory inventory)
    {
        INPUT_SLOT_ID = inventory.AddSlot(new MatterSlot(true));
        OUTPUT_SLOT_ID = inventory.AddSlot(new RemoveOnlySlot(false));
        super.RegisterSlots(inventory);
    }
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		this.manageDecompose();
        this.manageExtract();
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
    public float soundVolume() { return 1;}

    @Override
    public void onContainerOpen(Side side) {

    }

    private void  manageExtract()
    {
        if(!worldObj.isRemote)
        {
            if(time.hasDelayPassed(worldObj,MATTER_EXTRACT_SPEED))
            {
                for (int i = 0; i < 6; i++)
                {
                    ForgeDirection dir = ForgeDirection.values()[i];
                    TileEntity e = worldObj.getTileEntity(this.xCoord + dir.offsetX,this.yCoord + dir.offsetY,this.zCoord + dir.offsetZ);
                    if(e instanceof IMatterHandler)
                    {
                        if (MatterHelper.Transfer(dir,MATTER_STORAGE,this,(IMatterHandler)e) != 0)
                        {
                            updateClientMatter();
                        }
                    }
                }
            }
        }
    }

	protected void manageDecompose()
	{
        if(!worldObj.isRemote)
        {
            if (this.isDecomposing())
            {
                if(this.energyStorage.getEnergyStored() >= getEnergyDrainPerTick())
                {
                    this.decomposeTime++;
                    extractEnergy(ForgeDirection.DOWN, getEnergyDrainPerTick(), false);

                    if (this.decomposeTime >= getSpeed())
                    {
                        this.decomposeTime = 0;
                        this.decomposeItem();
                    }

                    decomposeProgress = Math.round(((float) (decomposeTime) / (float) getSpeed()) * 100);
                }
            }
        }

        if (!this.isDecomposing())
            {
			this.decomposeTime = 0;
                decomposeProgress = 0;
		}
	}

	public boolean isDecomposing()
    {
        int matter = MatterHelper.getMatterAmountFromItem(this.getStackInSlot(INPUT_SLOT_ID));
        return getRedstoneActive()
                && this.getStackInSlot(INPUT_SLOT_ID) != null
                && isItemValidForSlot(0, getStackInSlot(INPUT_SLOT_ID))
                && matter <= this.getMatterCapacity() - this.getMatterStored()
                && canPutInOutput(matter);
    }

    @Override
    public boolean isActive()
    {
        return isDecomposing() && this.energyStorage.getEnergyStored() >= getEnergyDrainPerTick();
    }

    public double getFailChance()
    {
        double upgradeMultiply = getUpgradeMultiply(UpgradeTypes.Fail);
        //this does not nagate all fail chance if item is not fully scanned
        return FAIL_CHANGE * upgradeMultiply * upgradeMultiply;
    }

    public int getSpeed()
    {
        int matter = MatterHelper.getMatterAmountFromItem(inventory.getStackInSlot(INPUT_SLOT_ID));
        if (matter > 0) {
            return MathHelper.round(DECEOPOSE_SPEED_PER_MATTER * Math.log(DECEOPOSE_SPEED_PER_MATTER * matter) * getUpgradeMultiply(UpgradeTypes.Speed));
        }else
        {
            return 1;
        }
    }

    public int getEnergyDrainPerTick()
    {
        int maxEnergy = getEnergyDrainMax();
        return maxEnergy / getSpeed();
    }

    public int getEnergyDrainMax()
    {
        int matter = MatterHelper.getMatterAmountFromItem(inventory.getStackInSlot(INPUT_SLOT_ID));
        double upgradeMultiply = getUpgradeMultiply(UpgradeTypes.PowerUsage);
        return MathHelper.round((matter * DECOMPOSE_ENERGY_PER_MATTER) * upgradeMultiply);
    }

    private boolean canPutInOutput(int matter)
    {
        ItemStack stack = getStackInSlot(OUTPUT_SLOT_ID);
        if(stack == null)
        {
            return true;
        }
        else
        {
            if(stack.getItem() == MatterOverdriveItems.matter_dust)
            {
                if (stack.getItemDamage() == matter && stack.stackSize < stack.getMaxStackSize())
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void failDecompose()
    {
        ItemStack stack = getStackInSlot(OUTPUT_SLOT_ID);
        int matter =MatterHelper.getMatterAmountFromItem(getStackInSlot(INPUT_SLOT_ID));

        if (stack != null)
        {
            if (stack.getItem() == MatterOverdriveItems.matter_dust && stack.getItemDamage() == matter && stack.stackSize < stack.getMaxStackSize())
            {
                stack.stackSize++;
            }
        }
        else
        {
            stack = new ItemStack(MatterOverdriveItems.matter_dust);
            MatterOverdriveItems.matter_dust.setMatter(stack, matter);
            setInventorySlotContents(OUTPUT_SLOT_ID, stack);
        }
    }
	
	private void decomposeItem() 
	{
        int matterAmount = MatterHelper.getMatterAmountFromItem(getStackInSlot(INPUT_SLOT_ID));

		if(getStackInSlot(INPUT_SLOT_ID) != null && canPutInOutput(matterAmount))
		{
            if(random.nextFloat() < getFailChance())
            {
                failDecompose();
            }
            else
            {
                int matter = this.matterStorage.getMatterStored();
                this.matterStorage.setMatterStored(matterAmount + matter);
                updateClientMatter();
            }

            this.decrStackSize(INPUT_SLOT_ID, 1);
            ForceSync();
		}
	}

    @Override
	public void readCustomNBT(NBTTagCompound nbt)
    {
        super.readCustomNBT(nbt);
        this.decomposeTime = nbt.getShort("DecomposeTime");
    }

    @Override
	public void writeCustomNBT(NBTTagCompound nbt)
    {
        super.writeCustomNBT(nbt);
        nbt.setShort("DecomposeTime", (short)this.decomposeTime);
    }

	@Override
	public int[] getAccessibleSlotsFromSide(int side) 
	{
        return new int[]{INPUT_SLOT_ID,OUTPUT_SLOT_ID};
	}

	@Override
	public boolean canExtractItem(int i, ItemStack item,
			int j) 
	{
		return j != 0 || i != INPUT_SLOT_ID;
	}

    @Override
    public boolean canConnectFrom(ForgeDirection dir)
    {
        return true;
    }

    @Override
    public int receiveMatter(ForgeDirection side, int amount, boolean simulate)
    {
        return 0;
    }
}
