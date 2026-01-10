package org.virgil698.NekoTerraForged.mixin.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * RTF 预设配置
 * 简化版本，使用 JSON 配置替代数据包生成器
 */
public class RTFPreset {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 世界设置
    public WorldSettings world = new WorldSettings();
    // 表面设置
    public SurfaceSettings surface = new SurfaceSettings();
    // 气候设置
    public ClimateSettings climate = new ClimateSettings();
    // 地形设置
    public TerrainSettings terrain = new TerrainSettings();
    // 河流设置
    public RiverSettings rivers = new RiverSettings();
    // 杂项设置
    public MiscSettings misc = new MiscSettings();

    /**
     * 从文件加载预设
     */
    public static RTFPreset load(Path path) {
        if (!Files.exists(path)) {
            RTFPreset preset = new RTFPreset();
            preset.save(path);
            return preset;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, RTFPreset.class);
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to load preset: " + e.getMessage());
            return new RTFPreset();
        }
    }

    /**
     * 保存预设到文件
     */
    public void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to save preset: " + e.getMessage());
        }
    }

    /**
     * 世界设置
     */
    public static class WorldSettings {
        public int continentScale = 3000;
        public int continentShape = 1; // 0=multi, 1=single, 2=pie
        public float continentJitter = 0.7f;
        public float continentSkipping = 0.25f;
        public float continentSizeVariance = 0.25f;
        public float continentNoiseOctaves = 5;
        public float continentNoiseGain = 0.5f;
        public float continentNoiseLacunarity = 2.5f;
        public int spawnType = 0; // 0=world_origin, 1=continent_center
    }

    /**
     * 表面设置
     */
    public static class SurfaceSettings {
        public boolean erosionEnabled = true;
        public int erosionDropletLifetime = 30;
        public int erosionDropletVolume = 140;
        public int erosionDropletVelocity = 40;
        public float erosionRate = 0.65f;
        public float depositionRate = 0.475f;
        public boolean strataEnabled = true;
        public boolean naturalSnowEnabled = true;
    }

    /**
     * 气候设置
     */
    public static class ClimateSettings {
        public int temperatureScale = 8;
        public int moistureScale = 6;
        public int biomeSize = 4;
        public int biomeWarpScale = 150;
        public int biomeWarpStrength = 80;
        public float temperatureOffset = 0.0f;
        public float moistureOffset = 0.0f;
    }

    /**
     * 地形设置
     */
    public static class TerrainSettings {
        public int regionSize = 1000;
        public float globalVerticalScale = 1.0f;
        public float globalHorizontalScale = 1.0f;
        public float seaLevel = 63;
        public float mountainHeight = 1.0f;
        public float volcanoChance = 0.7f;
        public boolean fancyMountains = true;
    }

    /**
     * 河流设置
     */
    public static class RiverSettings {
        public int riverCount = 14;
        public int mainRiverCount = 1;
        public float riverBedDepth = 5.0f;
        public float riverMinBankHeight = 2.0f;
        public float riverMaxBankHeight = 8.0f;
        public float riverBedWidth = 4.0f;
        public float riverBankWidth = 15.0f;
        public float riverFade = 0.75f;
        public boolean lakesEnabled = true;
        public float lakeChance = 0.3f;
        public float lakeDepth = 10.0f;
        public float lakeSizeMin = 50.0f;
        public float lakeSizeMax = 150.0f;
        public boolean wetlandsEnabled = true;
        public float wetlandChance = 0.6f;
    }

    /**
     * 杂项设置
     */
    public static class MiscSettings {
        public boolean smoothLayerDecorator = true;
        public boolean strataDecorator = true;
        public boolean customBiomeFeatures = true;
        public boolean terrainMatchStructures = true;
        public boolean smoothStructures = true;
    }
}
