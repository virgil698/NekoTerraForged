package org.virgil698.NekoTerraForged.mixin.world;

/**
 * 地形类型
 * 基于 Valley 的大陆值定义不同的地形
 */
public enum TerrainType {
    DEEP_OCEAN(-1.5, -0.5, "Deep Ocean"),
    OCEAN(-0.5, -0.25, "Ocean"),
    SHALLOW_OCEAN(-0.25, -0.19, "Shallow Ocean"),
    BEACH(-0.19, -0.1, "Beach"),
    PLAINS(-0.1, 0.2, "Plains"),
    HILLS(0.2, 0.5, "Hills"),
    MOUNTAINS(0.5, 0.8, "Mountains"),
    HIGH_MOUNTAINS(0.8, 1.5, "High Mountains");

    private final double minValue;
    private final double maxValue;
    private final String name;

    TerrainType(double minValue, double maxValue, String name) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.name = name;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public String getName() {
        return name;
    }

    public boolean matches(double value) {
        return value >= minValue && value < maxValue;
    }

    public static TerrainType fromValue(double value) {
        for (TerrainType type : values()) {
            if (type.matches(value)) {
                return type;
            }
        }
        return PLAINS;
    }

    public boolean isOcean() {
        return this == DEEP_OCEAN || this == OCEAN || this == SHALLOW_OCEAN;
    }

    public boolean isLand() {
        return !isOcean();
    }

    public boolean isMountainous() {
        return this == MOUNTAINS || this == HIGH_MOUNTAINS;
    }
}
