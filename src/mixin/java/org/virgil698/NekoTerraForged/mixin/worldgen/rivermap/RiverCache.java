package org.virgil698.NekoTerraForged.mixin.worldgen.rivermap;

import java.util.concurrent.ConcurrentHashMap;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

/**
 * 河流缓存
 * 移植自 ReTerraForged
 */
public class RiverCache {
    protected RiverGenerator generator;
    protected ConcurrentHashMap<Long, Rivermap> cache;

    public RiverCache(RiverGenerator generator) {
        this.cache = new ConcurrentHashMap<>();
        this.generator = generator;
    }

    public Rivermap getRivers(int x, int z) {
        long key = PosUtil.pack(x, z);
        return this.cache.computeIfAbsent(key, id -> {
            return this.generator.generateRivers(PosUtil.unpackLeft(id), PosUtil.unpackRight(id), id);
        });
    }

    public void clear() {
        this.cache.clear();
    }
}
