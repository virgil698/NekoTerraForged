package org.virgil698.NekoTerraForged.mixin.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.biome.spawn.NTFSpawnSearchHelper;
import org.virgil698.NekoTerraForged.mixin.worldgen.biome.spawn.SpawnSearchResult;

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
                SpawnSearchResult result = NTFSpawnSearchHelper.findBestSpawnPosition(list, sampler, center);
                callback.setReturnValue(result.location());
            }
        }
    }
}
