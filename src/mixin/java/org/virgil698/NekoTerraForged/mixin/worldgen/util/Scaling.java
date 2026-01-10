package org.virgil698.NekoTerraForged.mixin.worldgen.util;

/**
 * 缩放工具类
 * 移植自 ReTerraForged
 */
public record Scaling(int worldHeight, float unit, int waterY, int groundY, int groundLevel, int waterLevel, 
                      float ground, float water, float elevationRange) {
    
    public int scale(float value) {
        return (int) (value * this.worldHeight);
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
        return this.scale(y - this.waterY) / this.elevationRange;
    }
    
    public float blocks(int level) {
        return div(level, this.worldHeight);
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
    
    public static Scaling make(int height, int seaLevel) {
        int worldHeight = Math.max(1, height);
        float unit = div(1, worldHeight);
        int waterLevel = seaLevel;
        int groundLevel = waterLevel + 1;
        int waterY = Math.min(waterLevel - 1, worldHeight);
        int groundY = Math.min(groundLevel - 1, worldHeight);
        float ground = div(groundY, worldHeight);
        float water = div(waterY, worldHeight);
        float elevationRange = 1.0F - water;
        return new Scaling(worldHeight, unit, waterY, groundY, groundLevel, waterLevel, ground, water, elevationRange);
    }
}
