package com.MO.MatterOverdrive.world;

import com.MO.MatterOverdrive.handler.MOConfigurationHandler;
import com.MO.MatterOverdrive.init.MatterOverdriveBlocks;
import com.MO.MatterOverdrive.tile.TileEntityGravitationalAnomaly;
import com.MO.MatterOverdrive.util.IConfigSubscriber;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import java.util.Random;

/**
 * Created by Simeon on 5/18/2015.
 */
public class WorldGenGravitationalAnomaly extends WorldGenerator implements IConfigSubscriber
{
    float defaultChance;
    float chance;
    int minMatter;
    int maxMatter;

    public WorldGenGravitationalAnomaly(float chance,int minMatter,int maxMatter)
    {
        this.defaultChance = chance;
        this.chance = chance;
        this.minMatter = minMatter;
        this.maxMatter = maxMatter;
    }

    @Override
    public boolean generate(World world, Random random, int x, int y, int z)
    {
        if (random.nextFloat() < chance)
        {
            if (world.setBlock(x,y,z, MatterOverdriveBlocks.gravitational_anomaly))
            {
                TileEntityGravitationalAnomaly anomaly = new TileEntityGravitationalAnomaly(minMatter + random.nextInt(maxMatter - minMatter));
                world.setTileEntity(x,y,z,anomaly);
            }
        }
        return false;
    }

    @Override
    public void onConfigChanged(MOConfigurationHandler config)
    {
        chance = config.config.getFloat(MOConfigurationHandler.KEY_GRAVITATIONAL_ANOMALY_SPAWN_CHANCE,MOConfigurationHandler.CATEGORY_WORLD_GEN,defaultChance,0,1,"Spawn Chance of Gravity Anomaly pre chunk");
    }
}
