package org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.gen;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domain;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domains;

/**
 * 河流生成变形
 * 移植自 ReTerraForged
 */
public record GenWarp(Domain lake, Domain river) {
    public static final GenWarp EMPTY = new GenWarp(Domains.direct(), Domains.direct());

    public static GenWarp make(int seed, int continentScale) {
        Domain lake = Domains.domainPerlin(++seed, 200, 1, 300.0F);
        lake = Domains.add(lake, Domains.domainPerlin(++seed, 50, 2, 50.0F));

        Domain river = Domains.domainPerlin(++seed, 95, 1, 25.0F);
        river = Domains.add(river, Domains.domainPerlin(++seed, 16, 1, 5.0F));
        return new GenWarp(lake, river);
    }
}
