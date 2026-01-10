package org.virgil698.NekoTerraForged.mixin.worldgen.terrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableSet;

import org.virgil698.NekoTerraForged.mixin.worldgen.biome.Erosion;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.RegionConfig;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.populator.Populators;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.populator.TerrainPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Seed;

/**
 * 地形提供器
 * 移植自 ReTerraForged
 */
public class TerrainProvider {

    private static final Set<Terrain> LOW_EROSION = ImmutableSet.of(
        TerrainType.PLATEAU, TerrainType.BADLANDS, TerrainType.TORRIDONIAN
    );

    public static List<CellPopulator> generateTerrain(Seed seed, RegionConfig config, Levels levels, Noise ground) {
        float verticalScale = 1.0F;
        Seed terrainSeed = seed.offset(0);

        List<TerrainPopulator> mixable = new ArrayList<>();
        mixable.add(Populators.makeSteppe(terrainSeed, ground, 1.0F));
        mixable.add(Populators.makePlains(terrainSeed, ground, verticalScale));
        mixable.add(Populators.makeDales(terrainSeed, ground));
        mixable.add(Populators.makeHills(terrainSeed, ground, verticalScale));
        mixable.add(Populators.makeTorridonian(terrainSeed, ground));
        mixable.add(Populators.makePlateau(terrainSeed, ground, verticalScale));
        mixable.add(Populators.makeBadlands(terrainSeed, ground));
        mixable = mixable.stream().filter((populator) -> populator.weight() > 0.0F).toList();

        List<CellPopulator> unmixable = new ArrayList<>();
        unmixable.add(Populators.makeBadlands(terrainSeed, ground));
        unmixable.add(Populators.makeMountains(terrainSeed, ground, verticalScale));
        unmixable.add(Populators.makeMountains2(terrainSeed, ground, verticalScale, 1.0F));
        unmixable.add(Populators.makeVolcano(terrainSeed, ground, config, levels, 1.0F));

        List<TerrainPopulator> mixed = combine(mixable, (t1, t2) -> {
            return combine(ground, t1, t2, terrainSeed, levels, config.scale() / 2);
        });

        List<CellPopulator> result = new ArrayList<>();
        result.addAll(mixed);
        result.addAll(unmixable);

        Collections.shuffle(result, new Random(terrainSeed.next()));
        return result;
    }

    private static TerrainPopulator combine(Noise ground, TerrainPopulator tp1, TerrainPopulator tp2, Seed seed, Levels levels, int scale) {
        Terrain type = TerrainType.registerComposite(tp1.type(), tp2.type());
        Noise selector = Noises.perlin(seed.next(), scale, 1);
        selector = Noises.warpPerlin(selector, seed.next(), scale / 2, 2, scale / 2.0F);

        Noise height = Noises.blend(selector, tp1.height(), tp2.height(), 0.5F, 0.25F);
        height = Noises.max(height, Noises.zero());

        Noise erosion = LOW_EROSION.contains(tp1.type()) && LOW_EROSION.contains(tp2.type()) 
            ? Erosion.LEVEL_3.source() 
            : Erosion.LEVEL_4.source();
        Noise weirdness = Noises.blend(selector, tp1.weirdness(), tp2.weirdness(), 0.5F, 0.25F);

        float weight = (tp1.weight() + tp2.weight()) / 2.0F;
        return new TerrainPopulator(type, ground, height, erosion, weirdness, weight);
    }

    private static <T> List<T> combine(List<T> input, BiFunction<T, T, T> operator) {
        int length = input.size();
        for (int i = 1; i < input.size(); ++i) {
            length += input.size() - i;
        }
        List<T> result = new ArrayList<>(length);
        for (int j = 0; j < length; ++j) {
            result.add(null);
        }
        int j = 0;
        int k = input.size();
        while (j < input.size()) {
            T t1 = input.get(j);
            result.set(j, t1);
            for (int l = j + 1; l < input.size(); ++l, ++k) {
                T t2 = input.get(l);
                T t3 = operator.apply(t1, t2);
                result.set(k, t3);
            }
            ++j;
        }
        return result;
    }
}
