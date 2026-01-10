package org.virgil698.NekoTerraForged.mixin.worldgen;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.DensityFunction;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.settings.WorldSettings;

/**
 * RTF随机状态接口
 * 移植自 ReTerraForged
 */
public interface RTFRandomState {
    void initialize(RegistryAccess registryAccess);
    
    @Nullable
    RegistryAccess registryAccess();
    
    @Nullable
    WorldSettings settings();

    @Nullable
    GeneratorContext generatorContext();
    
    DensityFunction wrap(DensityFunction function);

    Noise wrap(Noise noise);
}
