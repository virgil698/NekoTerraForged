package org.virgil698.NekoTerraForged.mixin.mixins;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;

import org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition.NTFConstantCondition;

/**
 * 注入 SurfaceRules.BiomeConditionSource 来支持周围生物群系检测
 * 在 Leaves 1.21.10 中，BiomeConditionSource 是 SurfaceRules 的内部类
 * 
 * 这个 mixin 允许表面规则检查周围区块的生物群系，
 * 从而实现更平滑的生物群系边界过渡
 * 
 * 参考 ReTerraForged MixinSurfaceRules$BiomeConditionSource 实现
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$BiomeConditionSource")
public class MixinBiomeConditionSource {
    @Shadow
    @Final
    Predicate<ResourceKey<Biome>> biomeNameTest;

    // 缓存反射方法
    private static Method getSurroundingBiomesMethod;
    private static boolean methodLookupAttempted = false;

    /**
     * 注入 apply 方法来检查周围生物群系
     * 如果周围有匹配的生物群系，则返回 true
     */
    @Inject(at = @At("HEAD"), method = "apply", cancellable = true)
    @SuppressWarnings("unchecked")
    public void ntf$apply(SurfaceRules.Context ctx, CallbackInfoReturnable<SurfaceRules.Condition> cir) {
        // 尝试获取周围生物群系
        Set<ResourceKey<Biome>> surroundingBiomes = getSurroundingBiomes(ctx);
        
        if (surroundingBiomes != null) {
            boolean result = surroundingBiomes.stream().anyMatch(this.biomeNameTest);
            // 如果没有匹配或只有一个生物群系，直接返回结果
            // 使用独立的类文件代替 lambda，避免 Mixin 类加载问题
            if (!result || surroundingBiomes.size() == 1) {
                cir.setReturnValue(NTFConstantCondition.of(result));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<ResourceKey<Biome>> getSurroundingBiomes(SurfaceRules.Context ctx) {
        if (!methodLookupAttempted) {
            methodLookupAttempted = true;
            try {
                getSurroundingBiomesMethod = ctx.getClass().getMethod("ntf$getSurroundingBiomes");
            } catch (NoSuchMethodException e) {
                // 方法不存在，mixin 可能未应用
            }
        }
        
        if (getSurroundingBiomesMethod != null) {
            try {
                return (Set<ResourceKey<Biome>>) getSurroundingBiomesMethod.invoke(ctx);
            } catch (Exception e) {
                // 调用失败
            }
        }
        
        return null;
    }
}
