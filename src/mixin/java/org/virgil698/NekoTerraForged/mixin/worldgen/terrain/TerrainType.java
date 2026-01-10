package org.virgil698.NekoTerraForged.mixin.worldgen.terrain;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * 地形类型注册表
 * 移植自 ReTerraForged
 */
public class TerrainType {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();
    public static final Map<String, Terrain> REGISTRY = new ConcurrentHashMap<>();

    // 基础地形类型
    public static final Terrain NONE = register("none", TerrainCategory.NONE);
    public static final Terrain DEEP_OCEAN = register("deep_ocean", TerrainCategory.DEEP_OCEAN);
    public static final Terrain OCEAN = register("ocean", TerrainCategory.SHALLOW_OCEAN);
    public static final Terrain SHALLOW_OCEAN = register("shallow_ocean", TerrainCategory.SHALLOW_OCEAN);
    public static final Terrain COAST = register("coast", TerrainCategory.COAST);
    public static final Terrain BEACH = register("beach", TerrainCategory.BEACH);
    public static final Terrain RIVER = register("river", TerrainCategory.RIVER);
    public static final Terrain LAKE = register("lake", TerrainCategory.LAKE);
    public static final Terrain WETLAND = register("wetland", TerrainCategory.WETLAND);
    
    // 陆地地形类型
    public static final Terrain FLATS = register("flats", TerrainCategory.FLATLAND);
    public static final Terrain PLAINS = register("plains", TerrainCategory.LOWLAND);
    public static final Terrain STEPPE = register("steppe", TerrainCategory.LOWLAND);
    public static final Terrain HILLS = register("hills", TerrainCategory.HIGHLAND);
    public static final Terrain DALES = register("dales", TerrainCategory.HIGHLAND);
    public static final Terrain PLATEAU = register("plateau", TerrainCategory.HIGHLAND);
    public static final Terrain BADLANDS = register("badlands", TerrainCategory.HIGHLAND);
    public static final Terrain TORRIDONIAN = register("torridonian", TerrainCategory.HIGHLAND);
    
    // 山地地形类型
    public static final Terrain MOUNTAINS = register("mountains", TerrainCategory.MOUNTAIN);
    public static final Terrain MOUNTAIN_CHAIN = register("mountain_chain", TerrainCategory.MOUNTAIN);
    public static final Terrain VOLCANO = register("volcano", TerrainCategory.MOUNTAIN);
    public static final Terrain VOLCANO_PIPE = register("volcano_pipe", TerrainCategory.MOUNTAIN);

    public static Terrain register(String name, TerrainCategory category) {
        Terrain terrain = new Terrain(ID_COUNTER.getAndIncrement(), name, category);
        REGISTRY.put(name, terrain);
        return terrain;
    }

    public static Terrain get(String name) {
        return REGISTRY.getOrDefault(name, NONE);
    }

    public static Terrain get(int id) {
        for (Terrain terrain : REGISTRY.values()) {
            if (terrain.getId() == id) {
                return terrain;
            }
        }
        return NONE;
    }

    public static Terrain registerComposite(Terrain t1, Terrain t2) {
        String name = t1.getName() + "_" + t2.getName();
        Terrain existing = REGISTRY.get(name);
        if (existing != null) {
            return existing;
        }
        TerrainCategory category = t1.getDelegate().ordinal() > t2.getDelegate().ordinal() 
            ? t1.getDelegate() 
            : t2.getDelegate();
        return register(name, category);
    }

    /**
     * 获取所有注册的地形类型流
     */
    public static Stream<Terrain> stream() {
        return REGISTRY.values().stream();
    }

    /**
     * 获取所有注册的地形类型
     */
    public static Collection<Terrain> values() {
        return REGISTRY.values();
    }

    /**
     * 获取所有地形名称
     */
    public static Stream<String> names() {
        return REGISTRY.keySet().stream();
    }
}
