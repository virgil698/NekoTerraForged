package org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;

/**
 * 生物群系过滤器
 * 用于筛选要应用修改的生物群系
 * 移植自 ReTerraForged
 */
public record Filter(HolderSet<Biome> biomes, Behavior behavior) {
    public static final Codec<Filter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Biome.LIST_CODEC.fieldOf("biomes").forGetter(Filter::biomes),
        Behavior.CODEC.fieldOf("behavior").forGetter(Filter::behavior)
    ).apply(instance, Filter::new));
    
    /**
     * 测试生物群系是否通过过滤器
     */
    public boolean test(Holder<Biome> biome) {
        return this.behavior.test(this.biomes, biome);
    }
    
    /**
     * 过滤器行为枚举
     */
    public enum Behavior implements StringRepresentable {
        /**
         * 白名单模式 - 只有在列表中的生物群系才通过
         */
        WHITELIST("whitelist") {
            @Override
            public boolean test(HolderSet<Biome> biomes, Holder<Biome> biome) {
                return biomes.contains(biome);
            }
        },
        /**
         * 黑名单模式 - 不在列表中的生物群系才通过
         */
        BLACKLIST("blacklist") {
            @Override
            public boolean test(HolderSet<Biome> biomes, Holder<Biome> biome) {
                return !biomes.contains(biome);
            }
        };
        
        public static final Codec<Behavior> CODEC = StringRepresentable.fromEnum(Behavior::values);
        
        private String name;
        
        private Behavior(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
        
        public abstract boolean test(HolderSet<Biome> biomes, Holder<Biome> biome);
    }
}
