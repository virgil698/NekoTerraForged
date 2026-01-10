package org.virgil698.NekoTerraForged.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Climate;

/**
 * 注入 Climate.Sampler 来处理出生点搜索
 * 在 Leaves 1.21.10 中，Climate.Sampler.findSpawnPosition() 用于查找出生点
 * 
 * 参考 ReTerraForged MixinClimateSampler 实现
 */
@Mixin(Climate.Sampler.class)
public class MixinClimateSampler {
    @Unique
    private BlockPos ntf$spawnSearchCenter = BlockPos.ZERO;

    @Unique
    public void ntf$setSpawnSearchCenter(BlockPos center) {
        this.ntf$spawnSearchCenter = center;
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null) {
            bridge.setSpawnSearchCenter(center);
        }
    }

    @Unique
    public BlockPos ntf$getSpawnSearchCenter() {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null) {
            return bridge.getSpawnSearchCenter();
        }
        return this.ntf$spawnSearchCenter;
    }

    /**
     * 注入 findSpawnPosition 方法来使用 RTF 的出生点搜索
     * 在 Leaves 1.21.10 中，findSpawnPosition 是 Climate.Sampler 的实例方法
     */
    @Inject(
        at = @At("HEAD"),
        method = "findSpawnPosition",
        cancellable = true
    )
    private void ntf$findSpawnPosition(CallbackInfoReturnable<BlockPos> cir) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null && bridge.isInitialized()) {
            BlockPos center = bridge.getSpawnSearchCenter();
            if (center != null && !center.equals(BlockPos.ZERO)) {
                // 使用 RTF 计算的出生点中心
                cir.setReturnValue(center);
            }
        }
    }
}
