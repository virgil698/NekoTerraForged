package org.virgil698.NekoTerraForged.mixin.mixins;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.virgil698.NekoTerraForged.mixin.world.density.NodeFunction;
import org.virgil698.NekoTerraForged.mixin.world.density.RiverFunction;

/**
 * 注册自定义密度函数类型到 Minecraft 的内置注册表
 */
@Mixin(BuiltInRegistries.class)
public class MixinBuiltInRegistries {
    
    private static boolean registered = false;
    
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void ntf$registerDensityFunctions(CallbackInfo ci) {
        if (registered) {
            return;
        }
        registered = true;
        
        try {
            // 注册 NodeFunction（大陆密度函数）
            ResourceLocation nodeFunctionId = ResourceLocation.fromNamespaceAndPath("nekotf", "node");
            Registry.register(
                BuiltInRegistries.DENSITY_FUNCTION_TYPE,
                nodeFunctionId,
                createNodeFunctionCodec()
            );
            
            // 注册 RiverFunction（河流密度函数）
            ResourceLocation riverFunctionId = ResourceLocation.fromNamespaceAndPath("nekotf", "rivers");
            Registry.register(
                BuiltInRegistries.DENSITY_FUNCTION_TYPE,
                riverFunctionId,
                createRiverFunctionCodec()
            );
            
            System.out.println("[NekoTerraForged] Successfully registered density function types");
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to register density function types: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建 NodeFunction 的 Codec
     * 由于 Bukkit 插件不使用 Mojang 的 Codec 系统，这里返回一个简单的实现
     */
    private static MapCodec<NodeFunction> createNodeFunctionCodec() {
        // 返回一个简单的 Codec，实际上我们不会通过数据包加载
        return MapCodec.unit(() -> NodeFunction.continents());
    }
    
    /**
     * 创建 RiverFunction 的 Codec
     */
    private static MapCodec<RiverFunction> createRiverFunctionCodec() {
        // 返回一个简单的 Codec，实际上我们不会通过数据包加载
        return MapCodec.unit(() -> RiverFunction.preset());
    }
}
