package org.virgil698.NekoTerraForged.mixin.mixins;

import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridge;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * 注入 ChunkMap 来初始化 RTF 上下文
 * 在 Leaves 1.21.10 中，ChunkMap 构造函数签名为:
 * ChunkMap(ServerLevel, LevelStorageSource.LevelStorageAccess, DataFixer, StructureTemplateManager,
 *          Executor, BlockableEventLoop, LightChunkGetter, ChunkGenerator, ChunkStatusUpdateListener,
 *          Supplier<DimensionDataStorage>, TicketStorage, int, boolean)
 * 
 * 参考 ReTerraForged MixinChunkMap 实现
 */
@Mixin(ChunkMap.class)
public class MixinChunkMap {
    @Shadow
    @Final
    private RandomState randomState;

    @Shadow
    @Final
    public ServerLevel level;

    /**
     * 在 ChunkMap 构造函数末尾注入，初始化 RTF 上下文
     * 这是 RTF 初始化的关键点，在这里设置 WorldGenFlags 并初始化 GeneratorContext
     */
    @Inject(
        at = @At("TAIL"),
        method = "<init>"
    )
    private void ntf$onInit(CallbackInfo ci) {
        RTFBridge bridge = RTFBridgeManager.INSTANCE.getBridge();
        if (bridge != null) {
            // 设置 WorldGenFlags
            bridge.setCullNoiseSections(true);
            
            // 初始化上下文
            bridge.initializeContext(this.level.registryAccess(), this.level.getSeed());
            
            // 调用 RandomState 的 ntf$initialize 方法 (通过反射，因为 Mixin 在运行时添加)
            try {
                Method initMethod = this.randomState.getClass().getMethod("ntf$initialize", RegistryAccess.class);
                initMethod.invoke(this.randomState, this.level.registryAccess());
            } catch (NoSuchMethodException e) {
                // 方法不存在，可能 Mixin 未应用
                System.out.println("[NekoTerraForged] RandomState.ntf$initialize not found - Mixin may not be applied");
            } catch (Exception e) {
                System.err.println("[NekoTerraForged] Failed to call RandomState.ntf$initialize: " + e.getMessage());
            }
        }
    }
}
