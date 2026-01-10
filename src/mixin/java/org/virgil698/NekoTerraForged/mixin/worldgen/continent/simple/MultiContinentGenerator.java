package org.virgil698.NekoTerraForged.mixin.worldgen.continent.simple;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Seed;

/**
 * 多大陆生成器
 * 移植自 ReTerraForged
 */
public class MultiContinentGenerator extends ContinentGenerator {

    public MultiContinentGenerator(Seed seed, GeneratorContext context) {
        super(seed, context);
    }
}
