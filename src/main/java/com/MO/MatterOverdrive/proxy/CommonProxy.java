package com.MO.MatterOverdrive.proxy;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;

public class CommonProxy
{
	public void registerProxies()
	{
	}

    public void registerBlockIcons(IIconRegister register)
    {

    }

    public EntityPlayer getPlayerEntity(MessageContext ctx)
    {
        return ctx.getServerHandler().playerEntity;
    }
}
