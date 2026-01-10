package org.virgil698.NekoTerraForged.mixin.worldgen.biome.modifier;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;

/**
 * 生物群系过滤器
 * 移植自 ReTerraForged
 */
public record Filter(HolderSet<Biome> biomes, Behavior behavior) {
    
    public boolean test(Holder<Biome> biome) {
        return this.behavior.test(this.biomes, biome);
    }
    
    public enum Behavior implements StringRepresentable {
        WHITELIST("whitelist") {
            @Override
            public boolean test(HolderSet<Biome> biomes, Holder<Biome> biome) {
                return biomes.contains(biome);
            }
        },
        BLACKLIST("blacklist") {
            @Override
            public boolean test(HolderSet<Biome> biomes, Holder<Biome> biome) {
                return !biomes.contains(biome);
            }
        };
        
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
