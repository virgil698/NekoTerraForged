package org.virgil698.NekoTerraForged.mixin.worldgen;

import org.jetbrains.annotations.Nullable;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Heightmap;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.WorldLookup;
import org.virgil698.NekoTerraForged.mixin.worldgen.settings.WorldSettings;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.TileCache;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.TileGenerator;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.filter.WorldFilters;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Seed;

/**
 * 地形生成器上下文，管理生成所需的所有状态
 * 移植自 ReTerraForged
 */
public class GeneratorContext {
    public final Seed seed;
    public final Levels levels;
    public final WorldSettings settings;
    public final int tileSize;
    public final int tileBorder;
    public final int batchCount;

    @Nullable
    private Heightmap heightmap;
    @Nullable
    public WorldLookup lookup;
    @Nullable
    public TileCache cache;
    @Nullable
    public ThreadLocal<Heightmap> localHeightmap;
    @Nullable
    public TileGenerator generator;

    public GeneratorContext(WorldSettings settings, int seed, int tileSize, int tileBorder, int batchCount) {
        this.settings = settings;
        this.seed = new Seed(seed);
        this.levels = new Levels(settings.terrainScaler, settings.seaLevel);
        this.tileSize = tileSize;
        this.tileBorder = tileBorder;
        this.batchCount = batchCount;
    }

    /**
     * 初始化高度图和世界查找器
     */
    public void initialize() {
        if (this.heightmap == null) {
            this.heightmap = Heightmap.make(this);
            
            // 创建 ThreadLocal Heightmap
            final Heightmap globalHeightmap = this.heightmap;
            this.localHeightmap = ThreadLocal.withInitial(globalHeightmap::cache);
            
            // 创建 WorldFilters
            WorldFilters filters = new WorldFilters(this, this.heightmap);
            
            // 创建 Tile 生成器和缓存
            this.generator = new TileGenerator(this.localHeightmap, filters, this.tileSize, this.tileBorder, this.batchCount);
            this.cache = new TileCache(this.tileSize, this.generator);
            this.lookup = new WorldLookup(this);
        }
    }

    /**
     * 获取高度图
     */
    @Nullable
    public Heightmap getHeightmap() {
        return heightmap;
    }

    /**
     * 获取世界查找器
     */
    @Nullable
    public WorldLookup getLookup() {
        return lookup;
    }

    /**
     * 获取 Tile 缓存
     */
    @Nullable
    public TileCache getCache() {
        return cache;
    }

    /**
     * 创建带缓存的生成器上下文
     */
    public static GeneratorContext makeCached(WorldSettings settings, int seed, int tileSize, int batchCount) {
        int tileBorder = Math.min(2, Math.max(1, settings.dropletLifetime / 16));
        GeneratorContext ctx = new GeneratorContext(settings, seed, tileSize, tileBorder, batchCount);
        ctx.initialize();
        return ctx;
    }

    /**
     * 创建不带缓存的生成器上下文
     */
    public static GeneratorContext makeUncached(WorldSettings settings, int seed, int tileSize, int tileBorder, int batchCount) {
        GeneratorContext ctx = new GeneratorContext(settings, seed, tileSize, tileBorder, batchCount);
        ctx.initialize();
        return ctx;
    }

    /**
     * 使用默认参数创建生成器上下文
     */
    public static GeneratorContext create(int seed, int seaLevel) {
        WorldSettings settings = WorldSettings.defaults();
        settings.seaLevel = seaLevel;
        return makeCached(settings, seed, 3, 6);
    }
}
