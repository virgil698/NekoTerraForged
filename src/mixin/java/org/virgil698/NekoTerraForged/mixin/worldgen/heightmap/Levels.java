package org.virgil698.NekoTerraForged.mixin.worldgen.heightmap;

/**
 * 高度级别配置，用于地形生成的高度计算
 * 移植自 ReTerraForged
 */
public class Levels {
    public final int worldHeight;
    public final int minY;
    public final float unit;
    public final int waterY;
    private final int groundY;
    public final int groundLevel;
    public final int waterLevel;
    public final float ground;
    public final float water;
    private final float elevationRange;

    public Levels(float terrainScale, int seaLevel) {
        this(terrainScale, seaLevel, -64);
    }

    public Levels(float terrainScale, int seaLevel, int minY) {
        this.minY = minY;
        this.worldHeight = Math.max(1, (int) (384 * terrainScale));
        this.unit = div(1, this.worldHeight);
        this.waterLevel = seaLevel;
        this.groundLevel = this.waterLevel + 1;
        this.waterY = Math.min(this.waterLevel - 1, this.worldHeight);
        this.groundY = Math.min(this.groundLevel - 1, this.worldHeight);
        this.ground = div(this.groundY, this.worldHeight);
        this.water = div(this.waterY, this.worldHeight);
        this.elevationRange = 1.0F - this.water;
    }

    public int scale(float value) {
        return (int) (value * this.worldHeight);
    }

    public float scale(int level) {
        return div(level, this.worldHeight);
    }

    public float elevation(float value) {
        if (value <= this.water) {
            return 0.0F;
        }
        return (value - this.water) / this.elevationRange;
    }

    public float elevation(int y) {
        if (y <= this.waterY) {
            return 0.0F;
        }
        return scale(y - this.waterY) / this.elevationRange;
    }

    public float water(int amount) {
        return div(this.waterY + amount, this.worldHeight);
    }

    public float ground(int amount) {
        return div(this.groundY + amount, this.worldHeight);
    }

    private static float div(int a, int b) {
        return (float) a / (float) b;
    }
}
