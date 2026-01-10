package org.virgil698.NekoTerraForged.mixin.worldgen.rivermap;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 传统河流缓存
 * 移植自 ReTerraForged
 */
public class LegacyRiverCache extends RiverCache {

    public LegacyRiverCache(RiverGenerator generator) {
        super(generator);
    }

    @Override
    public Rivermap getRivers(int x, int z) {
        long id = NoiseUtil.seed(x, z);
        return this.cache.computeIfAbsent(id, key -> this.generator.generateRivers(x, z, key));
    }
}
