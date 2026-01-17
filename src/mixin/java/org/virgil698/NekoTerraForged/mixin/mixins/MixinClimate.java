package org.virgil698.NekoTerraForged.mixin.mixins;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复 Climate.Sampler 以支持自定义地形生成
 * 确保生物群系大小和分布与地形匹配
 */
@Mixin(Climate.Sampler.class)
public class MixinClimate {
    
    @Shadow
    @Final
    private DensityFunction continentalness;
    
    @Shadow
    @Final
    private DensityFunction erosion;
    
    /**
     * 拦截 Climate 采样，调整 continentalness 和 erosion 值
     * 使其与 Minecraft 的生物群系系统兼容
     */
    @Inject(method = "sample", at = @At("RETURN"), cancellable = true)
    private void ntf$adjustClimateValues(int x, int y, int z, CallbackInfoReturnable<Climate.TargetPoint> cir) {
        Climate.TargetPoint original = cir.getReturnValue();
        
        // 获取原始值
        long temp = original.temperature();
        long humidity = original.humidity();
        long continentalness = original.continentalness();
        long erosion = original.erosion();
        long depth = original.depth();
        long weirdness = original.weirdness();
        
        // 调整 continentalness 值以匹配 Minecraft 的范围
        // Minecraft 的 continentalness 范围：
        // -1.2 到 -1.05: 蘑菇岛
        // -1.05 到 -0.455: 深海
        // -0.455 到 -0.19: 海洋
        // -0.19 到 -0.11: 海岸
        // -0.11 到 0.55: 内陆
        // 0.55 到 1.0: 远内陆
        
        long adjustedContinentalness = adjustContinentalness(continentalness);
        long adjustedErosion = adjustErosion(erosion);
        
        // 创建调整后的 TargetPoint
        Climate.TargetPoint adjusted = new Climate.TargetPoint(
            temp,
            humidity,
            adjustedContinentalness,
            adjustedErosion,
            depth,
            weirdness
        );
        
        cir.setReturnValue(adjusted);
    }
    
    /**
     * 调整 continentalness 值
     * 将自定义地形的值映射到 Minecraft 的生物群系范围
     */
    private long adjustContinentalness(long value) {
        // 将 quantized 值转换为 float
        float unquantized = Climate.unquantizeCoord(value);
        
        // 我们的地形生成器可能返回 -1.0 到 1.0 的值
        // 需要映射到 Minecraft 的 continentalness 范围
        
        // 简单的线性映射：
        // -1.0 到 -0.5: 深海 (-1.05 到 -0.455)
        // -0.5 到 -0.2: 海洋 (-0.455 到 -0.19)
        // -0.2 到 0.0: 海岸 (-0.19 到 -0.11)
        // 0.0 到 0.6: 内陆 (-0.11 到 0.55)
        // 0.6 到 1.0: 远内陆 (0.55 到 1.0)
        
        float adjusted;
        if (unquantized < -0.5f) {
            // 深海区域
            adjusted = mapRange(unquantized, -1.0f, -0.5f, -1.05f, -0.455f);
        } else if (unquantized < -0.2f) {
            // 海洋区域
            adjusted = mapRange(unquantized, -0.5f, -0.2f, -0.455f, -0.19f);
        } else if (unquantized < 0.0f) {
            // 海岸区域
            adjusted = mapRange(unquantized, -0.2f, 0.0f, -0.19f, -0.11f);
        } else if (unquantized < 0.6f) {
            // 内陆区域
            adjusted = mapRange(unquantized, 0.0f, 0.6f, -0.11f, 0.55f);
        } else {
            // 远内陆区域
            adjusted = mapRange(unquantized, 0.6f, 1.0f, 0.55f, 1.0f);
        }
        
        return Climate.quantizeCoord(adjusted);
    }
    
    /**
     * 调整 erosion 值
     * 确保河流和山谷的生物群系正确
     */
    private long adjustErosion(long value) {
        // 将 quantized 值转换为 float
        float unquantized = Climate.unquantizeCoord(value);
        
        // Minecraft 的 erosion 范围：
        // -1.0 到 -0.78: 最高侵蚀（山峰）
        // -0.78 到 -0.375: 高侵蚀
        // -0.375 到 -0.2225: 中等侵蚀
        // -0.2225 到 0.05: 低侵蚀
        // 0.05 到 0.45: 河流
        // 0.45 到 0.55: 河流边缘
        // 0.55 到 1.0: 山谷
        
        // 我们的河流系统使用 0.9 到 1.0 表示河流
        // 需要映射到 Minecraft 的河流范围
        
        float adjusted;
        if (unquantized > 0.9f) {
            // 河流区域
            adjusted = mapRange(unquantized, 0.9f, 1.0f, 0.05f, 0.45f);
        } else if (unquantized > 0.85f) {
            // 河流边缘
            adjusted = mapRange(unquantized, 0.85f, 0.9f, 0.45f, 0.55f);
        } else if (unquantized > 0.0f) {
            // 山谷到低侵蚀
            adjusted = mapRange(unquantized, 0.0f, 0.85f, 0.55f, -0.2225f);
        } else {
            // 负值保持为高侵蚀/山峰
            adjusted = mapRange(unquantized, -1.0f, 0.0f, -1.0f, -0.2225f);
        }
        
        return Climate.quantizeCoord(adjusted);
    }
    
    /**
     * 线性映射函数
     */
    private float mapRange(float value, float fromMin, float fromMax, float toMin, float toMax) {
        // 防止除以零
        if (fromMax == fromMin) {
            return toMin;
        }
        
        // 线性插值
        float normalized = (value - fromMin) / (fromMax - fromMin);
        normalized = Math.max(0.0f, Math.min(1.0f, normalized)); // 限制在 [0, 1]
        return toMin + normalized * (toMax - toMin);
    }
}
