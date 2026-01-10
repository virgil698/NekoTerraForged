package org.virgil698.NekoTerraForged.mixin.worldgen.rivermap;

import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain.Domain;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.gen.GenWarp;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.river.Network;

/**
 * 河流地图
 * 移植自 ReTerraForged
 */
public class Rivermap {
    public static final Rivermap EMPTY = new Rivermap(0, 0, new Network[0], GenWarp.EMPTY);

    private int x;
    private int z;
    private Domain lakeWarp;
    private Domain riverWarp;
    private Network[] networks;
    private long timestamp;

    public Rivermap(int x, int z, Network[] networks, GenWarp warp) {
        this.timestamp = System.currentTimeMillis();
        this.x = x;
        this.z = z;
        this.networks = networks;
        this.lakeWarp = warp.lake();
        this.riverWarp = warp.river();
    }

    public void apply(Cell cell, float x, float z) {
        float rx = this.riverWarp.getX(x, z, 0);
        float rz = this.riverWarp.getZ(x, z, 0);
        float lx = this.lakeWarp.getOffsetX(rx, rz, 0);
        float lz = this.lakeWarp.getOffsetZ(rx, rz, 0);
        for (Network network : this.networks) {
            if (network.contains(rx, rz)) {
                network.carve(cell, rx, rz, lx, lz);
            }
        }
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public Network[] getNetworks() {
        return this.networks;
    }

    public static Rivermap get(Cell cell, Rivermap instance) {
        return get(cell.continentX, cell.continentZ, instance);
    }

    public static Rivermap get(int x, int z, Rivermap instance) {
        if (instance != null && x == instance.getX() && z == instance.getZ()) {
            return instance;
        }
        // 如果没有匹配的实例，返回空的 Rivermap
        return EMPTY;
    }

    public static Rivermap get(Cell cell, Rivermap instance, org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Heightmap heightmap) {
        if (instance != null && cell.continentX == instance.getX() && cell.continentZ == instance.getZ()) {
            return instance;
        }
        return heightmap.continent().getRivermap(cell.continentX, cell.continentZ);
    }
}
