package org.virgil698.NekoTerraForged.mixin.worldgen.surface.rule;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;

import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.surface.RTFSurfaceSystem;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;

/**
 * 地层规则 - 用于生成岩石层
 * 移植自 ReTerraForged
 */
public record StrataRule(ResourceLocation cacheId, int buffer, int iterations, 
                         Noise selector, List<Layer> layers) implements SurfaceRules.RuleSource {

    public static final MapCodec<StrataRule> MAP_CODEC = MapCodec.unit(() -> 
        new StrataRule(ResourceLocation.withDefaultNamespace("default"), 0, 1, Noise.ConstantNoise.ZERO, List.of()));

    @Override
    public SurfaceRules.SurfaceRule apply(Context ctx) {
        // 获取 RTFSurfaceSystem
        RTFSurfaceSystem surfaceSystem = getSurfaceSystem(ctx);
        if (surfaceSystem != null) {
            return new Rule(ctx, surfaceSystem.getOrCreateStrata(this.cacheId, this::generate));
        }
        // 如果没有 RTF 系统，返回空规则
        return (x, y, z) -> null;
    }

    @Override
    public KeyDispatchDataCodec<? extends SurfaceRules.RuleSource> codec() {
        return KeyDispatchDataCodec.of(MAP_CODEC);
    }

    @Nullable
    private RTFSurfaceSystem getSurfaceSystem(Context ctx) {
        // 通过 Bridge 获取
        var bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null) {
            Object context = bridge.getGeneratorContext();
            if (context instanceof GeneratorContext gc) {
                // 这里需要从 GeneratorContext 获取 SurfaceSystem
                // 暂时返回 null，后续实现
            }
        }
        return null;
    }
    
    public List<RTFSurfaceSystem.Strata> generate(RandomSource random) {
        List<RTFSurfaceSystem.Strata> strata = new ArrayList<>(this.iterations);
        for (int i = 0; i < this.iterations; i++) {
            strata.add(this.generateStrata(random));
        }
        return strata;
    }
    
    private RTFSurfaceSystem.Strata generateStrata(RandomSource random) {
        List<RTFSurfaceSystem.Stratum> stratum = new ArrayList<>();
        for (Layer layer : this.layers) {
            int layerCount = layer.layers(random.nextFloat());
            for (int i = 0; i < layerCount; i++) {
                float depth = layer.depth(random.nextFloat());
                stratum.add(new RTFSurfaceSystem.Stratum(
                    net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 
                    depth
                ));
            }
        }
        return new RTFSurfaceSystem.Strata(stratum);
    }
    
    public class Rule implements SurfaceRules.SurfaceRule {
        private Context context;
        private List<RTFSurfaceSystem.Strata> strataEntries;
        private Levels levels;
        private Tile.Chunk chunk;
        private RTFSurfaceSystem.Strata strata;
        @Nullable
        private RTFSurfaceSystem.Stratum bufferMaterial;
        private int height;
        private int index;
        private float[] depthBuffer;
        private long lastXZ;
        
        public Rule(Context context, List<RTFSurfaceSystem.Strata> strataEntries) {
            this.context = context;
            this.strataEntries = strataEntries;
            
            var bridge = RTFBridgeManager.INSTANCE.getBridge();
            if (bridge != null) {
                Object ctx = bridge.getGeneratorContext();
                if (ctx instanceof GeneratorContext gc) {
                    ChunkPos chunkPos = context.chunk.getPos();
                    this.levels = gc.levels;
                    Object tileChunk = bridge.getTileChunk(chunkPos.x, chunkPos.z);
                    if (tileChunk instanceof Tile.Chunk tc) {
                        this.chunk = tc;
                    }
                }
            }
        }
        
        private RTFSurfaceSystem.Strata selectStrata(int x, int z) {
            float value = StrataRule.this.selector.compute(x, z, 0);
            int index = (int) (value * this.strataEntries.size());
            index = Math.min(this.strataEntries.size() - 1, index);
            return this.strataEntries.get(index);
        }

        private void update(int x, int z) {
            RTFSurfaceSystem.Strata strata = this.selectStrata(x, z);
            
            if (this.strata != strata) {
                this.strata = strata;
                this.bufferMaterial = null;
                
                for (RTFSurfaceSystem.Stratum stratum : this.strata.stratum()) {
                    this.bufferMaterial = stratum;
                    break;
                }
            }
            
            List<RTFSurfaceSystem.Stratum> stratum = this.strata.stratum();
            int stratumCount = stratum.size();
            
            if (this.depthBuffer == null || this.depthBuffer.length < stratumCount) {
                this.depthBuffer = new float[stratumCount];
            }

            float sum = 0.0F;
            for (int i = 0; i < stratumCount; i++) {
                float depth = stratum.get(i).depth();
                sum += depth;
                this.depthBuffer[i] = depth;
            }
            
            if (this.levels != null && this.chunk != null) {
                this.height = this.levels.scale(this.chunk.getCell(x, z).height);
            } else {
                this.height = 64;
            }
            
            this.index = 0;
            
            int dy = this.height;
            for (int i = 0; i < stratumCount; i++) {
                this.depthBuffer[i] = dy -= NoiseUtil.round((this.depthBuffer[i] / sum) * this.height);
            }
        }
        
        @Override
        public BlockState tryApply(int x, int y, int z) {
            if (this.strataEntries.isEmpty()) return null;
            
            long packedPos = PosUtil.pack(x, z);
            if (this.lastXZ != packedPos) {
                this.update(x, z);
                this.lastXZ = packedPos;
            }
            
            if (this.strata == null || this.strata.stratum().isEmpty()) return null;
            
            if (StrataRule.this.buffer != 0 && y > this.height - StrataRule.this.buffer) {
                return this.bufferMaterial != null ? this.bufferMaterial.state() : null;
            }
            
            List<RTFSurfaceSystem.Stratum> stratum = this.strata.stratum();
            while (this.index + 1 < stratum.size() && y < this.depthBuffer[this.index]) {
                this.index++;
            }
            return stratum.get(this.index).state();
        }
    }
    
    /**
     * 地层层配置
     */
    public record Layer(TagKey<Block> materials, int attempts, int minLayers, int maxLayers, 
                        float minDepth, float maxDepth) {
        
        public int layers(float f) {
            int range = this.maxLayers - this.minLayers;
            return this.minLayers + NoiseUtil.round(f * range);
        }
        
        public float depth(float f) {
            float range = this.maxDepth - this.minDepth;
            return this.minDepth + f * range;
        }
    }
}
