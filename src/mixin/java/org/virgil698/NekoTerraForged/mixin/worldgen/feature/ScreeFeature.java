package org.virgil698.NekoTerraForged.mixin.worldgen.feature;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainType;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;

/**
 * 碎石特性 - 在陡峭山坡上生成碎石/岩屑堆积
 * Scree 是由于风化和重力作用在山坡上形成的松散岩石碎片
 * 移植自 ReTerraForged
 */
public class ScreeFeature extends Feature<ScreeFeature.Config> {
    
    public ScreeFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> placeContext) {
        WorldGenLevel level = placeContext.level();
        RandomSource random = placeContext.random();
        Config config = placeContext.config();
        
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        @Nullable GeneratorContext generatorContext = null;
        
        if (bridge != null && bridge.isInitialized()) {
            Object ctx = bridge.getGeneratorContext();
            if (ctx instanceof GeneratorContext gc) {
                generatorContext = gc;
            }
        }
        
        if (generatorContext == null) {
            return false;
        }
        
        ChunkGenerator generator = placeContext.chunkGenerator();
        ChunkPos chunkPos = new ChunkPos(placeContext.origin());
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
        
        Tile.Chunk tileChunk = generatorContext.cache.provideAtChunk(chunkX, chunkZ).getChunkReader(chunkX, chunkZ);
        org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Heightmap heightmap = generatorContext.localHeightmap.get();
        Levels levels = heightmap.levels();
        
        // 噪声用于变化
        Noise varianceNoise = Noises.perlin(generatorContext.seed.root(), 50, 2);
        Noise scatterNoise = Noises.white(generatorContext.seed.root() + 1, 1);
        
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int seaLevel = generator.getSeaLevel();
        int placed = 0;
        
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Cell cell = tileChunk.getCell(x, z);
                
                int worldX = chunkPos.getBlockX(x);
                int worldZ = chunkPos.getBlockZ(z);
                int surfaceY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                
                // 只在海平面以上处理
                if (surfaceY < seaLevel) {
                    continue;
                }
                
                float steepness = cell.gradient;
                float height = cell.height;
                
                // 检查是否应该生成碎石
                if (!shouldPlaceScree(config, cell, steepness, height, worldX, worldZ, varianceNoise)) {
                    continue;
                }
                
                // 随机散布检查
                float scatter = scatterNoise.compute(worldX, worldZ, 0);
                if (scatter > config.density()) {
                    continue;
                }
                
                pos.set(worldX, surfaceY, worldZ);
                
                // 检查当前方块是否可以被替换
                BlockState currentState = chunk.getBlockState(pos);
                if (!canReplace(currentState, config)) {
                    continue;
                }
                
                // 获取碎石方块状态
                BlockState screeState = getScreeState(config, cell, random, worldX, worldZ, varianceNoise);
                
                // 放置碎石
                if (placeScree(chunk, pos, screeState, config, random)) {
                    placed++;
                }
            }
        }
        
        return placed > 0;
    }
    
    /**
     * 检查是否应该在此位置生成碎石
     */
    private boolean shouldPlaceScree(Config config, Cell cell, float steepness, float height, 
                                      int x, int z, Noise varianceNoise) {
        // 基础陡峭度检查
        float threshold = config.steepness();
        
        // 添加噪声变化
        float variance = varianceNoise.compute(x, z, 0) * config.variance();
        threshold += variance;
        
        // 陡峭度必须超过阈值
        if (steepness < threshold) {
            return false;
        }
        
        // 高度检查 - 碎石通常出现在较高的地方
        if (height < config.minHeight()) {
            return false;
        }
        
        // 地形类型检查 - 某些地形更适合碎石
        if (cell.terrain == TerrainType.VOLCANO || 
            cell.terrain == TerrainType.VOLCANO_PIPE) {
            return true; // 火山地形总是可以有碎石
        }
        
        if (cell.terrain.isMountain()) {
            return true; // 山地地形
        }
        
        // 其他陡峭地形也可以有碎石
        return steepness > threshold * 1.2F;
    }
    
    /**
     * 检查方块是否可以被碎石替换
     */
    private boolean canReplace(BlockState state, Config config) {
        if (state.isAir()) {
            return false;
        }
        
        // 检查是否是可替换的方块
        if (state.is(BlockTags.DIRT)) {
            return true;
        }
        
        if (state.is(Blocks.GRASS_BLOCK) || 
            state.is(Blocks.STONE) || 
            state.is(Blocks.COBBLESTONE) ||
            state.is(Blocks.GRAVEL)) {
            return true;
        }
        
        // 检查自定义标签
        if (config.replaceableTag() != null) {
            return state.is(config.replaceableTag());
        }
        
        return false;
    }
    
    /**
     * 获取碎石方块状态
     */
    private BlockState getScreeState(Config config, Cell cell, RandomSource random, 
                                      int x, int z, Noise varianceNoise) {
        // 使用配置的方块提供者
        BlockStateProvider provider = config.screeBlocks();
        BlockPos pos = new BlockPos(x, 0, z);
        return provider.getState(random, pos);
    }
    
    /**
     * 放置碎石
     */
    private boolean placeScree(ChunkAccess chunk, BlockPos.MutableBlockPos pos, 
                               BlockState screeState, Config config, RandomSource random) {
        // 放置主碎石方块
        chunk.setBlockState(pos, screeState, 0);
        
        // 可选：向下延伸碎石层
        int depth = config.depth();
        if (depth > 1 && random.nextFloat() < 0.5F) {
            int actualDepth = 1 + random.nextInt(depth - 1);
            for (int dy = 1; dy < actualDepth; dy++) {
                pos.setY(pos.getY() - 1);
                BlockState below = chunk.getBlockState(pos);
                if (canReplaceBelow(below)) {
                    chunk.setBlockState(pos, screeState, 0);
                } else {
                    break;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 检查下方方块是否可以被替换
     */
    private boolean canReplaceBelow(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        return state.is(BlockTags.DIRT) || 
               state.is(Blocks.STONE) || 
               state.is(Blocks.GRASS_BLOCK);
    }
    
    /**
     * 碎石特性配置
     */
    public record Config(
        float steepness,      // 陡峭度阈值 (0.0-1.0)
        float minHeight,      // 最小高度 (归一化)
        float density,        // 密度 (0.0-1.0)
        float variance,       // 变化量
        int depth,            // 碎石深度
        BlockStateProvider screeBlocks,  // 碎石方块提供者
        @Nullable TagKey<Block> replaceableTag  // 可替换方块标签
    ) implements FeatureConfiguration {
        
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("steepness").forGetter(Config::steepness),
            Codec.FLOAT.fieldOf("min_height").forGetter(Config::minHeight),
            Codec.FLOAT.fieldOf("density").forGetter(Config::density),
            Codec.FLOAT.fieldOf("variance").forGetter(Config::variance),
            Codec.INT.fieldOf("depth").forGetter(Config::depth),
            BlockStateProvider.CODEC.fieldOf("scree_blocks").forGetter(Config::screeBlocks),
            TagKey.codec(Registries.BLOCK).optionalFieldOf("replaceable_tag", null).forGetter(Config::replaceableTag)
        ).apply(instance, Config::new));
        
        /**
         * 创建默认配置
         */
        public static Config defaults() {
            return new Config(
                0.65F,  // steepness - 较陡的坡度
                0.4F,   // minHeight - 中等高度以上
                0.3F,   // density - 30% 密度
                0.1F,   // variance - 10% 变化
                2,      // depth - 2 格深
                BlockStateProvider.simple(Blocks.GRAVEL),  // 默认使用砾石
                null    // 无自定义标签
            );
        }
        
        /**
         * 创建山地配置
         */
        public static Config mountain() {
            return new Config(
                0.55F,  // steepness - 稍低的阈值
                0.5F,   // minHeight - 较高位置
                0.4F,   // density - 40% 密度
                0.15F,  // variance - 15% 变化
                3,      // depth - 3 格深
                BlockStateProvider.simple(Blocks.COBBLESTONE),  // 使用圆石
                null
            );
        }
        
        /**
         * 创建火山配置
         */
        public static Config volcano() {
            return new Config(
                0.45F,  // steepness - 更低的阈值
                0.3F,   // minHeight - 较低位置也可以
                0.5F,   // density - 50% 密度
                0.2F,   // variance - 20% 变化
                4,      // depth - 4 格深
                BlockStateProvider.simple(Blocks.BLACKSTONE),  // 使用黑石
                null
            );
        }
    }
}
