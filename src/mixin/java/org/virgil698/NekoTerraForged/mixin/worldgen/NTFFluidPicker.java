package org.virgil698.NekoTerraForged.mixin.worldgen;

import net.minecraft.world.level.levelgen.Aquifer;

/**
 * 自定义 FluidPicker 实现
 * 单独的类文件，避免 Mixin lambda 导致的 NoClassDefFoundError
 */
public final class NTFFluidPicker implements Aquifer.FluidPicker {
    private final Aquifer.FluidStatus lava;
    private final Aquifer.FluidStatus defaultFluid;
    private final int lavaLevel;
    private final int seaLevel;
    
    public NTFFluidPicker(Aquifer.FluidStatus lava, Aquifer.FluidStatus defaultFluid, int lavaLevel, int seaLevel) {
        this.lava = lava;
        this.defaultFluid = defaultFluid;
        this.lavaLevel = lavaLevel;
        this.seaLevel = seaLevel;
    }
    
    @Override
    public Aquifer.FluidStatus computeFluid(int x, int y, int z) {
        if (y < Math.min(lavaLevel, seaLevel)) {
            return lava;
        }
        return defaultFluid;
    }
}
