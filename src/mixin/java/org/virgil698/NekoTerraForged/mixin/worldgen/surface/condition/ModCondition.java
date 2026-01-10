package org.virgil698.NekoTerraForged.mixin.worldgen.surface.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.Context;

/**
 * Mod 加载条件
 * 检查指定的 mod/插件 是否已加载
 * 移植自 ReTerraForged
 * 
 * 注意：在 Leaves 插件环境中，这是一个简单的实现
 */
public record ModCondition(String modId) implements SurfaceRules.ConditionSource {
    public static final MapCodec<ModCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("mod_id").forGetter(ModCondition::modId)
    ).apply(instance, ModCondition::new));

    @Override
    public SurfaceRules.Condition apply(Context ctx) {
        boolean loaded = isPluginLoaded(this.modId);
        return () -> loaded;
    }

    @Override
    public KeyDispatchDataCodec<ModCondition> codec() {
        return KeyDispatchDataCodec.of(MAP_CODEC);
    }

    private static boolean isPluginLoaded(String modId) {
        // 在 Leaves 服务端环境中检查插件
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object pluginManager = bukkitClass.getMethod("getPluginManager").invoke(null);
            Object plugin = pluginManager.getClass().getMethod("getPlugin", String.class).invoke(pluginManager, modId);
            return plugin != null;
        } catch (Exception e) {
            return false;
        }
    }
}
