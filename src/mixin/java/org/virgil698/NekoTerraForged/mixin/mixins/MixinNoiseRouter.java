package org.virgil698.NekoTerraForged.mixin.mixins;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.virgil698.NekoTerraForged.mixin.world.density.NodeFunction;
import org.virgil698.NekoTerraForged.mixin.world.density.RiverFunction;
import org.virgil698.NekoTerraForged.mixin.world.density.DensityNode;

/**
 * 修改 NoiseRouter 以注入我们的自定义密度函数
 */
@Mixin(NoiseRouter.class)
public class MixinNoiseRouter {
    private static boolean loggedOnce = false;
    
    /**
     * 拦截 mapAll 方法，替换 continents 和其他密度函数
     */
    @Inject(method = "mapAll", at = @At("RETURN"), cancellable = true)
    private void ntf$injectCustomDensityFunctions(
            DensityFunction.Visitor visitor,
            CallbackInfoReturnable<NoiseRouter> cir) {
        
        NoiseRouter original = cir.getReturnValue();
        
        try {
            // 创建我们的自定义密度函数
            NodeFunction continentsFunction = NodeFunction.continents();
            RiverFunction riverFunction = RiverFunction.create(
                0, 
                org.virgil698.NekoTerraForged.mixin.world.river.River.Config.Defaults(),
                new DensityNode(null) // 使用空的 DensityNode，因为我们在 ValleyChunkFiller 中处理
            );
            
            // 创建新的 NoiseRouter，替换 continents 和 erosion
            NoiseRouter modified = new NoiseRouter(
                original.barrierNoise(),
                original.fluidLevelFloodednessNoise(),
                original.fluidLevelSpreadNoise(),
                original.lavaNoise(),
                original.temperature(),
                original.vegetation(),
                continentsFunction,  // 替换 continents
                riverFunction,       // 替换 erosion 为河流函数
                original.depth(),
                original.ridges(),
                original.preliminarySurfaceLevel(),
                original.finalDensity(),
                original.veinToggle(),
                original.veinRidged(),
                original.veinGap()
            );
            
            cir.setReturnValue(modified);
            
            // 只在第一次注入时记录日志，避免刷屏
            if (!loggedOnce) {
                System.out.println("[NekoTerraForged] Successfully injected custom density functions into NoiseRouter");
                loggedOnce = true;
            }
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to inject custom density functions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
