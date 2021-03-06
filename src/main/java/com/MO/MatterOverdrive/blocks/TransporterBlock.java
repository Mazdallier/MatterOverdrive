package com.MO.MatterOverdrive.blocks;

import com.MO.MatterOverdrive.blocks.includes.MOBlockMachine;
import com.MO.MatterOverdrive.blocks.includes.MOMatterEnergyStorageBlock;
import com.MO.MatterOverdrive.tile.TileEntityMachineTransporter;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import com.MO.MatterOverdrive.Reference;
import com.MO.MatterOverdrive.blocks.includes.MOBlockContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.World;

public class TransporterBlock extends MOMatterEnergyStorageBlock
{
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconFront;

	public TransporterBlock(Material material, String name) 
	{
		super(material, name,true,true);
		setHardness(20.0F);
		this.setResistance(9.0f);
		this.setHarvestLevel("pickaxe", 2);
		this.setHasGui(true);
	}
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon(Reference.MOD_ID + ":" + "transporter_side");
		this.iconTop = iconRegister.registerIcon(Reference.MOD_ID + ":" + "transporter_top");
		this.iconFront = iconRegister.registerIcon(Reference.MOD_ID + ":" + "transporter_front");
	}
	
	/**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata)
    {
    	if(side == 0 || side == 1)
    	{
    		return this.iconTop;
    	}
    	else if(side == metadata)
    	{
    		return this.iconFront;
    	}
    	
    	return this.blockIcon;
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
    {
        return new TileEntityMachineTransporter();
    }
}
