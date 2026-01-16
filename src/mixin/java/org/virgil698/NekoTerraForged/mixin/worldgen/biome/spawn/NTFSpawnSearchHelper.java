package org.virgil698.NekoTerraForged.mixin.worldgen.biome.spawn;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.Climate.TargetPoint;

/**
 * 出生点搜索辅助类
 * 从 MixinSpawnFinder 中提取出来，避免 Mixin 内部类/方法导致的问题
 */
public final class NTFSpawnSearchHelper {
    
    private NTFSpawnSearchHelper() {}

    /**
     * 搜索最佳出生点
     */
    public static SpawnSearchResult findBestSpawnPosition(List<ParameterPoint> list, Sampler sampler, BlockPos center) {
        SpawnSearchResult result = getSpawnPositionAndFitness(list, sampler, center.getX(), center.getZ());
        result = radialSearch(list, sampler, result, 2048.0f, 512.0f);
        result = radialSearch(list, sampler, result, 512.0f, 32.0f);
        return result;
    }

    /**
     * 径向搜索
     */
    public static SpawnSearchResult radialSearch(List<ParameterPoint> list, Sampler sampler, 
            SpawnSearchResult current, float maxRadius, float step) {
        float angle = 0.0f;
        float radius = step;
        BlockPos blockPos = current.location();
        SpawnSearchResult result = current;

        while (radius <= maxRadius) {
            int x = blockPos.getX() + (int) (Math.sin(angle) * radius);
            int z = blockPos.getZ() + (int) (Math.cos(angle) * radius);
            SpawnSearchResult candidate = getSpawnPositionAndFitness(list, sampler, x, z);

            if (candidate.fitness() < result.fitness()) {
                result = candidate;
            }

            angle += step / radius;
            if (angle > Math.PI * 2) {
                angle = 0.0f;
                radius += step;
            }
        }

        return result;
    }

    /**
     * 计算指定位置的出生点适应度
     */
    public static SpawnSearchResult getSpawnPositionAndFitness(List<ParameterPoint> list, Sampler sampler, int x, int z) {
        double d = Mth.square(2500.0);
        long distancePenalty = (long) ((double) Mth.square(10000.0f)
                * Math.pow((double) (Mth.square((long) x) + Mth.square((long) z)) / d, 2.0));

        TargetPoint targetPoint = sampler.sample(QuartPos.fromBlock(x), 0, QuartPos.fromBlock(z));
        TargetPoint targetPoint2 = new TargetPoint(
            targetPoint.temperature(),
            targetPoint.humidity(),
            targetPoint.continentalness(),
            targetPoint.erosion(),
            0L,
            targetPoint.weirdness()
        );

        long minFitness = Long.MAX_VALUE;
        for (ParameterPoint parameterPoint : list) {
            minFitness = Math.min(minFitness, parameterPoint.fitness(targetPoint2));
        }

        return new SpawnSearchResult(new BlockPos(x, 0, z), distancePenalty + minFitness);
    }
}
