package org.virgil698.NekoTerraForged.mixin.worldgen.continent;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Seed;

/**
 * 大陆类型枚举
 * 移植自 ReTerraForged
 */
public enum ContinentType {
    MULTI {
        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new SimpleContinentGenerator(seed, context);
        }
    },
    SINGLE {
        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new SimpleContinentGenerator(seed, context);
        }
    },
    INFINITE {
        @Override
        public Continent create(Seed seed, GeneratorContext context) {
            return new InfiniteContinentGenerator(context);
        }
    };

    public abstract Continent create(Seed seed, GeneratorContext context);

    public String getName() {
        return this.name();
    }
}
