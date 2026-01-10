package org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellField;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;

/**
 * RTF 密度函数注册
 * 移植自 ReTerraForged
 */
public class RTFDensityFunctions {

    public static void bootstrap() {
        register("rtf_noise_sampler", NoiseSampler.Marker.MAP_CODEC);
        register("rtf_cell", CellSampler.Marker.MAP_CODEC);
        register("rtf_clamp_to_nearest_unit", ClampToNearestUnit.MAP_CODEC);
        register("rtf_linear_spline", LinearSplineFunction.MAP_CODEC);
    }

    public static NoiseSampler.Marker noise(Noise noise) {
        return new NoiseSampler.Marker(noise);
    }

    public static CellSampler.Marker cell(CellField field) {
        return new CellSampler.Marker(field);
    }

    public static ClampToNearestUnit clampToNearestUnit(DensityFunction function, int resolution) {
        return new ClampToNearestUnit(function, resolution);
    }

    @SuppressWarnings("unchecked")
    private static void register(String name, MapCodec<? extends DensityFunction> codec) {
        Registry.register(
            (Registry<MapCodec<? extends DensityFunction>>) (Registry<?>) BuiltInRegistries.DENSITY_FUNCTION_TYPE,
            "nekoterraforged:" + name,
            codec
        );
    }
}
