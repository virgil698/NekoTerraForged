package org.virgil698.NekoTerraForged.mixin.worldgen.biome;

import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Biome Consumer 实现
 * 单独的类文件，避免 Mixin lambda 导致的 NoClassDefFoundError
 */
public final class NTFBiomeConsumer implements Consumer<Holder<Biome>> {
    private final Set<ResourceKey<Biome>> targetSet;
    
    public NTFBiomeConsumer(Set<ResourceKey<Biome>> targetSet) {
        this.targetSet = targetSet;
    }
    
    @Override
    public void accept(Holder<Biome> biome) {
        biome.unwrapKey().ifPresent(targetSet::add);
    }
}
