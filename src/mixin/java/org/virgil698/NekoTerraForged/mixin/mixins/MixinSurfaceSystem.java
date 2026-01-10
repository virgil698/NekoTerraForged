package org.virgil698.NekoTerraForged.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceSystem;

/**
 * 注入 SurfaceSystem 来处理 RTF 表面规则
 * 在 Leaves 1.21.10 中，SurfaceSystem 构造函数签名为:
 * SurfaceSystem(RandomState randomState, BlockState defaultBlock, int seaLevel, PositionalRandomFactory noiseRandom)
 */
@Mixin(SurfaceSystem.class)
public class MixinSurfaceSystem {
    @Unique
    private static final ResourceLocation NTF_STRATA_RANDOM = ResourceLocation.withDefaultNamespace("nekoterraforged_strata");

    @Unique
    private RandomSource ntf$strataRandom;

    /**
     * 在 SurfaceSystem 构造函数末尾注入，初始化 strata 随机源
     */
    @Inject(
        at = @At("TAIL"),
        method = "<init>"
    )
    public void ntf$onInit(RandomState randomState, BlockState blockState, int seaLevel, 
            PositionalRandomFactory positionalRandomFactory, CallbackInfo ci) {
        this.ntf$strataRandom = positionalRandomFactory.fromHashOf(NTF_STRATA_RANDOM);
    }

    /**
     * 获取 strata 随机源
     */
    @Unique
    public RandomSource ntf$getStrataRandom() {
        return this.ntf$strataRandom;
    }
}
