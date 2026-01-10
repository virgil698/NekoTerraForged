package org.virgil698.NekoTerraForged.mixin.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;

/**
 * 列装饰器
 * 移植自 ReTerraForged
 */
@Deprecated
public class ColumnDecorator {
    private static final Noise VARIANCE = Noises.perlin(0, 100, 1);

    public static void fillDownSolid(ChunkAccess chunk, BlockPos.MutableBlockPos pos, int from, int to, BlockState state) {
        for (int dy = from; dy > to; dy--) {
            replaceSolid(chunk, pos.setY(dy), state);
        }
    }

    public static void replaceSolid(ChunkAccess chunk, BlockPos pos, BlockState state) {
        if (chunk.getBlockState(pos).isAir()) {
            return;
        }
        chunk.setBlockState(pos, state, Block.UPDATE_NONE);
    }
    
    public static float sampleNoise(float x, float z, float scale, float bias) {
        return VARIANCE.compute(x, z, 0) * scale + bias;
    }

    public static float sampleNoise(float x, float z, int scale, int bias) {
        return sampleNoise(x, z, scale / 255F, bias / 255F);
    }
}
