package org.virgil698.NekoTerraForged.mixin.worldgen.sampler;

/**
 * 噪声采样器
 * 移植自 ReTerraForged
 */
public record NoiseSampler(ContinentSampler continent, RiverSampler rivers, ClimateSampler climate) {
}
