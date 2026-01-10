package org.virgil698.NekoTerraForged.mixin.worldgen.terrain;

import com.mojang.serialization.Codec;

/**
 * 地形类型
 * 移植自 ReTerraForged
 */
public class Terrain implements ITerrain.Delegate {
    public static final Codec<Terrain> CODEC = Codec.STRING.xmap(TerrainType::get, Terrain::getName);

    private final int id;
    private final String name;
    private final TerrainCategory category;
    private final ITerrain delegate;

    Terrain(int id, String name, Terrain terrain) {
        this(id, name, terrain.getCategory(), terrain);
    }

    Terrain(int id, String name, TerrainCategory category) {
        this(id, name, category, category);
    }

    Terrain(int id, String name, TerrainCategory category, ITerrain delegate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.delegate = delegate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TerrainCategory getCategory() {
        return category;
    }

    @Override
    public TerrainCategory getDelegate() {
        return category;
    }

    @Override
    public String toString() {
        return getName();
    }

    public Terrain withId(int id) {
        ITerrain delegate = (this.delegate instanceof Terrain) ? this.delegate : this;
        return new Terrain(id, this.name, this.category, delegate);
    }
}
