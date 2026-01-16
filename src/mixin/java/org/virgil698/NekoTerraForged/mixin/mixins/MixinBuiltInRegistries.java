package org.virgil698.NekoTerraForged.mixin.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.virgil698.NekoTerraForged.mixin.bridge.RTFBridgeManager;
import org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction.RTFDensityFunctions;

import net.minecraft.core.registries.BuiltInRegistries;

/**
 * 注入 BuiltInRegistries 来注册 RTF 的密度函数类型
 * 必须在注册表冻结前完成注册
 */
@Mixin(BuiltInRegistries.class)
public class MixinBuiltInRegistries {

    /**
     * 在 bootStrap 方法开始时注册 RTF 密度函数类型
     * 这确保在注册表冻结前完成注册
     * 同时初始化 RTFBridge，确保在世界加载前就可用
     */
    @Inject(
        method = "bootStrap(Ljava/lang/Runnable;)V",
        at = @At("HEAD")
    )
    private static void ntf$onBootstrap(Runnable runnable, CallbackInfo ci) {
        // 首先初始化 Bridge，确保在世界加载前就可用
        System.out.println("[NekoTerraForged] Initializing RTFBridge early...");
        try {
            RTFBridgeManager.INSTANCE.initialize();
            System.out.println("[NekoTerraForged] RTFBridge initialized successfully");
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to initialize RTFBridge: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 然后注册密度函数类型
        System.out.println("[NekoTerraForged] Registering RTF density function types...");
        try {
            RTFDensityFunctions.bootstrap();
            System.out.println("[NekoTerraForged] RTF density function types registered successfully");
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to register RTF density function types: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
