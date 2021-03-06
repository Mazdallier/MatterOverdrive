package com.MO.MatterOverdrive.proxy;

import com.MO.MatterOverdrive.client.RenderHandler;
import com.MO.MatterOverdrive.client.render.*;
import com.MO.MatterOverdrive.handler.KeyHandler;
import com.MO.MatterOverdrive.handler.TooltipHandler;
import com.MO.MatterOverdrive.init.MatterOverdriveBlocks;
import com.MO.MatterOverdrive.init.MatterOverdriveIcons;
import com.MO.MatterOverdrive.init.MatterOverdriveItems;
import com.MO.MatterOverdrive.tile.*;
import com.MO.MatterOverdrive.tile.pipes.TileEntityMatterPipe;
import com.MO.MatterOverdrive.tile.pipes.TileEntityNetworkPipe;
import com.MO.MatterOverdrive.tile.pipes.TileEntityPipe;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy
{
    public static TileEntityRendererPipe pipeRenderer;
    public static TileEntityRendererMatterPipe matter_pipeRenderer;
    public static TileEntityRendererNetworkPipe network_pipeRenderer;
    public static TileEntityRendererReplicator replicator_renderer;
    public static TileEntityRendererPatterStorage pattern_storage_renderer;
    public static TileEntityRendererWeaponStation renderer_weapon_station;
    public static TileEntityRendererPatternMonitor pattern_monitor_renderer;
    public static TileEntityRendererGravitationalAnomaly gravitational_anomaly_renderer;
    public static TileEntityRendererGravitationalStabilizer gravitational_stabilizer_renderer;
    public static TileEntityRendererFusionReactorController fusion_reactor_controller_renderer;

    public static RenderHandler renderHandler;
    public static MOBlockRenderer blockRenderer;
    public static RendererBlockGravitationalStabilizer gravitationalStabilizerRenderer;

    @Override
	public void registerProxies()
	{
        renderHandler = new RenderHandler(Minecraft.getMinecraft().theWorld,Minecraft.getMinecraft().getTextureManager());

        FMLCommonHandler.instance().bus().register(new KeyHandler());
        MinecraftForge.EVENT_BUS.register(new MatterOverdriveIcons());
        MinecraftForge.EVENT_BUS.register(renderHandler);
        MinecraftForge.EVENT_BUS.register(new TooltipHandler());
        FMLCommonHandler.instance().bus().register(renderHandler);

        blockRenderer = new MOBlockRenderer();
        gravitationalStabilizerRenderer = new RendererBlockGravitationalStabilizer();

        pipeRenderer = new TileEntityRendererPipe();
        matter_pipeRenderer = new TileEntityRendererMatterPipe();
        network_pipeRenderer = new TileEntityRendererNetworkPipe();
        replicator_renderer = new TileEntityRendererReplicator();
        pattern_storage_renderer = new TileEntityRendererPatterStorage();
        renderer_weapon_station = new TileEntityRendererWeaponStation();
        pattern_monitor_renderer = new TileEntityRendererPatternMonitor();
        gravitational_anomaly_renderer = new TileEntityRendererGravitationalAnomaly();
        gravitational_stabilizer_renderer = new TileEntityRendererGravitationalStabilizer();
        fusion_reactor_controller_renderer = new TileEntityRendererFusionReactorController();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipe.class, pipeRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMatterPipe.class, matter_pipeRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNetworkPipe.class, network_pipeRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineReplicator.class,replicator_renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePatternStorage.class,pattern_storage_renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWeaponStation.class,renderer_weapon_station);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePatternMonitor.class,pattern_monitor_renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGravitationalAnomaly.class,gravitational_anomaly_renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineGravitationalStabilizer.class,gravitational_stabilizer_renderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineFusionReactorController.class,fusion_reactor_controller_renderer);

        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MatterOverdriveBlocks.matter_pipe), new ItemRendererPipe(matter_pipeRenderer, new TileEntityMatterPipe(), 2));
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MatterOverdriveBlocks.network_pipe),new ItemRendererPipe(network_pipeRenderer,new TileEntityNetworkPipe(),2));
        MinecraftForgeClient.registerItemRenderer(MatterOverdriveItems.phaser, new ItemRendererPhaser());
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MatterOverdriveBlocks.replicator),new ItemRendererTileEntityMachine(replicator_renderer,new TileEntityMachineReplicator()));
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MatterOverdriveBlocks.pattern_storage),new ItemRendererTileEntityMachine(pattern_storage_renderer,new TileEntityMachinePatternStorage()));

        RenderingRegistry.registerBlockHandler(blockRenderer);
        RenderingRegistry.registerBlockHandler(gravitationalStabilizerRenderer);
	}

    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx)
    {
        return (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : super.getPlayerEntity(ctx));
    }

}
