package org.virgil698.NekoTerraForged.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * NekoTerraForged 配置管理器
 */
public class RTFConfig {
    private final Plugin plugin;
    private File configFile;
    private FileConfiguration config;
    
    // 配置值缓存
    private boolean enabled;
    private ContinentConfig continentConfig;
    private RiverConfig riverConfig;
    private TerrainConfig terrainConfig;
    private NoiseConfig noiseConfig;
    private CacheConfig cacheConfig;
    private PerformanceConfig performanceConfig;
    
    public RTFConfig(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig() {
        // 创建插件数据文件夹
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // 配置文件路径
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        // 如果配置文件不存在，从资源中复制
        if (!configFile.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                    plugin.getLogger().info("已创建默认配置文件");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("无法创建配置文件: " + e.getMessage());
            }
        }
        
        // 加载配置
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // 读取配置值
        enabled = config.getBoolean("enabled", true);
        continentConfig = loadContinentConfig();
        riverConfig = loadRiverConfig();
        terrainConfig = loadTerrainConfig();
        noiseConfig = loadNoiseConfig();
        cacheConfig = loadCacheConfig();
        performanceConfig = loadPerformanceConfig();
        
        plugin.getLogger().info("配置文件已加载");
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadConfig();
        plugin.getLogger().info("配置文件已重新加载");
    }
    
    /**
     * 保存配置
     */
    public void saveConfig() {
        try {
            config.save(configFile);
            plugin.getLogger().info("配置文件已保存");
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存配置文件: " + e.getMessage());
        }
    }
    
    // ========== 配置加载方法 ==========
    
    private ContinentConfig loadContinentConfig() {
        int scale = config.getInt("continent.scale", 1600);
        int fbmOctaves = config.getInt("continent.fbm.octaves", 2);
        double fbmLacunarity = config.getDouble("continent.fbm.lacunarity", 1.5);
        double fbmGain = config.getDouble("continent.fbm.gain", 0.25);
        double warpStrength = config.getDouble("continent.warp.strength", 0.00125);
        double warpScale = config.getDouble("continent.warp.scale", 175.0);
        
        List<SplinePoint> splinePoints = loadSplinePoints("continent.spline.points");
        
        return new ContinentConfig(scale, fbmOctaves, fbmLacunarity, fbmGain, 
                                   warpStrength, warpScale, splinePoints);
    }
    
    private RiverConfig loadRiverConfig() {
        int scale = config.getInt("river.scale", 300);
        double ocean = config.getDouble("river.ocean", -0.25);
        double coast = config.getDouble("river.coast", -0.19);
        
        List<SplinePoint> riverRadius = loadSplinePoints("river.river_radius.points");
        List<SplinePoint> valleyRadius = loadSplinePoints("river.valley_radius.points");
        List<SplinePoint> output = loadSplinePoints("river.output.points");
        
        return new RiverConfig(scale, ocean, coast, riverRadius, valleyRadius, output);
    }
    
    private TerrainConfig loadTerrainConfig() {
        int seaLevel = config.getInt("terrain.sea_level", 63);
        int minHeight = config.getInt("terrain.min_height", -64);
        int maxHeight = config.getInt("terrain.max_height", 320);
        
        return new TerrainConfig(seaLevel, minHeight, maxHeight);
    }
    
    private NoiseConfig loadNoiseConfig() {
        return new NoiseConfig(
            config.getInt("noise.erosion.scale", 800),
            config.getInt("noise.erosion.octaves", 3),
            config.getInt("noise.peaks_valleys.scale", 400),
            config.getInt("noise.temperature.scale", 2000),
            config.getInt("noise.humidity.scale", 2000)
        );
    }
    
    private CacheConfig loadCacheConfig() {
        return new CacheConfig(
            config.getInt("cache.river_region_cache_size", 256),
            config.getInt("cache.river_region_cache_concurrency", 16),
            config.getInt("cache.linear_cache_size", 16)
        );
    }
    
    private PerformanceConfig loadPerformanceConfig() {
        return new PerformanceConfig(
            config.getBoolean("performance.multi_threading", true),
            config.getBoolean("performance.enable_cache", true),
            config.getBoolean("performance.debug_logging", false)
        );
    }
    
    private List<SplinePoint> loadSplinePoints(String path) {
        List<SplinePoint> points = new ArrayList<>();
        List<Map<?, ?>> pointMaps = config.getMapList(path);
        
        for (Map<?, ?> pointMap : pointMaps) {
            double input = ((Number) pointMap.get("input")).doubleValue();
            double output = ((Number) pointMap.get("output")).doubleValue();
            points.add(new SplinePoint(input, output));
        }
        
        return points;
    }
    
    // ========== Getter 方法 ==========
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public ContinentConfig getContinentConfig() {
        return continentConfig;
    }
    
    public RiverConfig getRiverConfig() {
        return riverConfig;
    }
    
    public TerrainConfig getTerrainConfig() {
        return terrainConfig;
    }
    
    public NoiseConfig getNoiseConfig() {
        return noiseConfig;
    }
    
    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }
    
    public PerformanceConfig getPerformanceConfig() {
        return performanceConfig;
    }
    
    public FileConfiguration getRawConfig() {
        return config;
    }
    
    // ========== 配置数据类 ==========
    
    public static class ContinentConfig {
        public final int scale;
        public final int fbmOctaves;
        public final double fbmLacunarity;
        public final double fbmGain;
        public final double warpStrength;
        public final double warpScale;
        public final List<SplinePoint> splinePoints;
        
        public ContinentConfig(int scale, int fbmOctaves, double fbmLacunarity, double fbmGain,
                              double warpStrength, double warpScale, List<SplinePoint> splinePoints) {
            this.scale = scale;
            this.fbmOctaves = fbmOctaves;
            this.fbmLacunarity = fbmLacunarity;
            this.fbmGain = fbmGain;
            this.warpStrength = warpStrength;
            this.warpScale = warpScale;
            this.splinePoints = splinePoints;
        }
    }
    
    public static class RiverConfig {
        public final int scale;
        public final double ocean;
        public final double coast;
        public final List<SplinePoint> riverRadius;
        public final List<SplinePoint> valleyRadius;
        public final List<SplinePoint> output;
        
        public RiverConfig(int scale, double ocean, double coast,
                          List<SplinePoint> riverRadius, List<SplinePoint> valleyRadius,
                          List<SplinePoint> output) {
            this.scale = scale;
            this.ocean = ocean;
            this.coast = coast;
            this.riverRadius = riverRadius;
            this.valleyRadius = valleyRadius;
            this.output = output;
        }
    }
    
    public static class TerrainConfig {
        public final int seaLevel;
        public final int minHeight;
        public final int maxHeight;
        
        public TerrainConfig(int seaLevel, int minHeight, int maxHeight) {
            this.seaLevel = seaLevel;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }
    }
    
    public static class NoiseConfig {
        public final int erosionScale;
        public final int erosionOctaves;
        public final int peaksValleysScale;
        public final int temperatureScale;
        public final int humidityScale;
        
        public NoiseConfig(int erosionScale, int erosionOctaves, int peaksValleysScale,
                          int temperatureScale, int humidityScale) {
            this.erosionScale = erosionScale;
            this.erosionOctaves = erosionOctaves;
            this.peaksValleysScale = peaksValleysScale;
            this.temperatureScale = temperatureScale;
            this.humidityScale = humidityScale;
        }
    }
    
    public static class CacheConfig {
        public final int riverRegionCacheSize;
        public final int riverRegionCacheConcurrency;
        public final int linearCacheSize;
        
        public CacheConfig(int riverRegionCacheSize, int riverRegionCacheConcurrency, int linearCacheSize) {
            this.riverRegionCacheSize = riverRegionCacheSize;
            this.riverRegionCacheConcurrency = riverRegionCacheConcurrency;
            this.linearCacheSize = linearCacheSize;
        }
    }
    
    public static class PerformanceConfig {
        public final boolean multiThreading;
        public final boolean enableCache;
        public final boolean debugLogging;
        
        public PerformanceConfig(boolean multiThreading, boolean enableCache, boolean debugLogging) {
            this.multiThreading = multiThreading;
            this.enableCache = enableCache;
            this.debugLogging = debugLogging;
        }
    }
    
    public static class SplinePoint {
        public final double input;
        public final double output;
        
        public SplinePoint(double input, double output) {
            this.input = input;
            this.output = output;
        }
    }
}
