package org.virgil698.NekoTerraForged.mixin.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.Climate.TargetPoint;

/**
 * 出生点搜索 Mixin
 * 修复 RTF 地形生成时的出生点搜索问题
 * 
 * 注入 Climate.findSpawnPosition 方法，使用 RTF 的大陆中心作为搜索起点
 */
@Mixin(Climate.class)
public class MixinSpawnFinder {

    @Inject(at = @At("HEAD"), method = "findSpawnPosition", cancellable = true)
    private static void ntf$findSpawnPosition(List<ParameterPoint> list, Sampler sampler, CallbackInfoReturnable<BlockPos> callback) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized()) {
            BlockPos center = bridge.getSpawnSearchCenter();
            if (center != null) {
                SpawnSearchResult result = ntf$findBestSpawnPosition(list, sampler, center);
                callback.setReturnValue(result.location());
            }
        }
    }

    /**
     * 搜索最佳出生点
     */
    private static SpawnSearchResult ntf$findBestSpawnPosition(List<ParameterPoint> list, Sampler sampler, BlockPos center) {
        SpawnSearchResult result = ntf$getSpawnPositionAndFitness(list, sampler, center.getX(), center.getZ());
        result = ntf$radialSearch(list, sampler, result, 2048.0f, 512.0f);
        result = ntf$radialSearch(list, sampler, result, 512.0f, 32.0f);
        return result;
    }

    /**
     * 径向搜索
     */
    private static SpawnSearchResult ntf$radialSearch(List<ParameterPoint> list, Sampler sampler, 
            SpawnSearchResult current, float maxRadius, float step) {
        float angle = 0.0f;
        float radius = step;
        BlockPos blockPos = current.location();
        SpawnSearchResult result = current;

        while (radius <= maxRadius) {
            int x = blockPos.getX() + (int) (Math.sin(angle) * radius);
            int z = blockPos.getZ() + (int) (Math.cos(angle) * radius);
            SpawnSearchResult candidate = ntf$getSpawnPositionAndFitness(list, sampler, x, z);

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
     * 使用距离惩罚 + 生物群系适应度来评估位置
     */
    private static SpawnSearchResult ntf$getSpawnPositionAndFitness(List<ParameterPoint> list, Sampler sampler, int x, int z) {
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
            // 使用 access widener 访问 fitness 方法
            minFitness = Math.min(minFitness, parameterPoint.fitness(targetPoint2));
        }

        return new SpawnSearchResult(new BlockPos(x, 0, z), distancePenalty + minFitness);
    }

    /**
     * 出生点搜索结果
     */
    private record SpawnSearchResult(BlockPos location, long fitness) {
    }
}
