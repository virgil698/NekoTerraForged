package org.virgil698.NekoTerraForged.mixin.bridge;

import org.jetbrains.annotations.Nullable;
import org.virgil698.NekoTerraForged.mixin.config.RTFConfig;
import org.virgil698.NekoTerraForged.mixin.preset.BuiltinPresets;
import org.virgil698.NekoTerraForged.mixin.preset.RTFPreset;
import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.WorldGenFlags;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.Cell;
import org.virgil698.NekoTerraForged.mixin.worldgen.cell.CellField;
import org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction.CellSampler;
import org.virgil698.NekoTerraForged.mixin.worldgen.densityfunction.NoiseSampler;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.WorldLookup;
import org.virgil698.NekoTerraForged.mixin.worldgen.surface.SurfaceRegion;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.Terrain;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.TerrainType;
import org.virgil698.NekoTerraForged.mixin.worldgen.tile.Tile;

import com.google.common.base.Suppliers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * RTFBridge 的实现类
 * 位于 mixin 包内，可以访问 MC 类和 worldgen 类
 */
public class RTFBridgeImpl implements RTFBridge {
    @Nullable
    private GeneratorContext generatorContext;
    private BlockPos spawnSearchCenter = BlockPos.ZERO;
    private long seed;
    private boolean initialized = false;
    
    // 配置管理器
    @Nullable
    private RTFConfig config;
    private Path configPath;

    // 线程本地 Cell 缓存
    private final ThreadLocal<Cell> cellCache = ThreadLocal.withInitial(Cell::new);
    
    public RTFBridgeImpl() {
        // 默认配置路径
        this.configPath = Paths.get("plugins", "NekoTerraForged", "config.json");
        // 加载配置（配置文件由插件层负责生成）
        getOrCreateConfig();
        System.out.println("[NekoTerraForged] Config initialized at: " + configPath.toAbsolutePath());
    }
    
    /**
     * 设置配置文件路径
     */
    public void setConfigPath(Path path) {
        this.configPath = path;
        if (this.config != null) {
            this.config = new RTFConfig(path);
            this.config.load();
        }
    }
    
    private RTFConfig getOrCreateConfig() {
        if (config == null) {
            config = new RTFConfig(configPath);
            config.load();
        }
        return config;
    }
    
    // ==================== 配置相关实现 ====================
    
    @Override
    public <T> T getConfig(String key, T defaultValue) {
        return getOrCreateConfig().get(key, defaultValue);
    }
    
    @Override
    public void setConfig(String key, Object value) {
        getOrCreateConfig().set(key, value);
    }
    
    @Override
    public void reloadConfig() {
        getOrCreateConfig().reload();
        // 应用配置到 WorldGenFlags
        applyConfigToFlags();
    }
    
    @Override
    public void saveConfig() {
        getOrCreateConfig().save();
    }
    
    @Override
    public Map<String, Object> getAllConfig() {
        return getOrCreateConfig().getAll();
    }
    
    private void applyConfigToFlags() {
        RTFConfig cfg = getOrCreateConfig();
        WorldGenFlags.setCullNoiseSections(cfg.isCullNoiseSections());
        WorldGenFlags.setFastLookups(cfg.isFastLookups());
        WorldGenFlags.setFastCellLookups(cfg.isFastCellLookups());
    }
    
    // ==================== 调试信息实现 ====================
    
    @Override
    public String getDebugInfo(int x, int z) {
        Cell cell = (Cell) applyCell(x, z);
        if (cell == null) {
            return "No cell data available";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== RTF Debug Info ===\n");
        sb.append(String.format("Position: (%d, %d)\n", x, z));
        sb.append(String.format("Height: %.2f\n", cell.height));
        sb.append(String.format("Continentalness: %.4f\n", cell.continentalness));
        sb.append(String.format("Erosion: %.4f\n", cell.erosion));
        sb.append(String.format("Weirdness: %.4f\n", cell.weirdness));
        sb.append(String.format("Temperature: %.4f\n", cell.temperature));
        sb.append(String.format("Moisture: %.4f\n", cell.moisture));
        sb.append(String.format("Biome Region Edge: %.4f\n", cell.biomeRegionEdge));
        sb.append(String.format("River Distance: %.4f\n", cell.riverDistance));
        
        if (cell.terrain != null) {
            sb.append(String.format("Terrain: %s\n", cell.terrain.getName()));
        }
        
        return sb.toString();
    }
    
    @Override
    @Nullable
    public String getTerrainType(int x, int z) {
        Cell cell = (Cell) applyCell(x, z);
        if (cell != null && cell.terrain != null) {
            return cell.terrain.getName();
        }
        return null;
    }
    
    @Override
    public float getBiomeEdge(int x, int z) {
        Cell cell = (Cell) applyCell(x, z);
        if (cell != null) {
            return cell.biomeRegionEdge;
        }
        return 0.0F;
    }
    
    @Override
    public boolean exportHeightmap(int centerX, int centerZ, int radius, String outputPath) {
        if (generatorContext == null) {
            return false;
        }
        
        try {
            int size = radius * 2 * 16; // 转换为方块数
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            
            int startX = centerX - radius * 16;
            int startZ = centerZ - radius * 16;
            
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    int worldX = startX + dx;
                    int worldZ = startZ + dz;
                    
                    Cell cell = (Cell) applyCell(worldX, worldZ);
                    if (cell != null) {
                        // 将高度映射到灰度值
                        int height = (int) (cell.height * 255);
                        height = Math.max(0, Math.min(255, height));
                        int color = (height << 16) | (height << 8) | height;
                        image.setRGB(dx, dz, color);
                    }
                }
            }
            
            File output = new File(outputPath);
            output.getParentFile().mkdirs();
            ImageIO.write(image, "PNG", output);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== 地形定位实现 ====================
    
    @Override
    public List<String> getTerrainTypes() {
        // 返回所有可搜索的地形类型（排除 NONE 和 VOLCANO_PIPE）
        return TerrainType.stream()
            .filter(t -> !t.getName().equals("none") && !t.getName().equals("volcano_pipe"))
            .map(Terrain::getName)
            .collect(Collectors.toList());
    }
    
    @Override
    @Nullable
    public int[] locateTerrain(int originX, int originZ, String terrainName, int step, int minRadius, int maxRadius, int timeoutSeconds) {
        if (generatorContext == null) {
            return null;
        }
        
        Terrain target = TerrainType.get(terrainName);
        if (target == null || target.getName().equals("none")) {
            return null;
        }
        
        WorldLookup lookup = generatorContext.getLookup();
        if (lookup == null) {
            return null;
        }
        
        return searchTerrain(lookup, target, originX, originZ, step, minRadius, maxRadius, timeoutSeconds);
    }
    
    @Nullable
    private int[] searchTerrain(WorldLookup lookup, Terrain target, int originX, int originZ, 
            int step, int minRadius, int maxRadius, int timeoutSeconds) {
        int radius = maxRadius;
        double minRadiusSq = (double) minRadius * minRadius;
        int x = 0;
        int z = 0;
        int dx = 0;
        int dz = -1;
        int size = radius + 1 + radius;
        long max = (long) size * (long) size;
        long timeOut = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);
        
        Cell cell = new Cell();
        
        for (long i = 0; i < max; i++) {
            if (System.currentTimeMillis() > timeOut) {
                break;
            }
            
            if ((-radius <= x) && (x <= radius) && (-radius <= z) && (z <= radius)) {
                int posX = originX + (x * step);
                int posZ = originZ + (z * step);
                
                double distSq = (double)(posX - originX) * (posX - originX) + 
                               (double)(posZ - originZ) * (posZ - originZ);
                
                if (minRadiusSq == 0 || distSq >= minRadiusSq) {
                    // 测试该位置的地形
                    cell.reset();
                    lookup.apply(cell, posX, posZ);
                    
                    if (cell.terrain != null && cell.terrain.equals(target)) {
                        return new int[] { posX, posZ };
                    }
                }
            }
            
            // 螺旋搜索模式
            if ((x == z) || ((x < 0) && (x == -z)) || ((x > 0) && (x == 1 - z))) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }
            
            x += dx;
            z += dz;
        }
        
        return null;
    }
    
    // ==================== 预设相关实现 ====================
    
    @Override
    public List<String> getPresetNames() {
        return new ArrayList<>(BuiltinPresets.getNames());
    }
    
    @Override
    public boolean loadPreset(String presetName) {
        try {
            RTFPreset preset = BuiltinPresets.get(presetName);
            if (preset == null) {
                return false;
            }
            
            // 应用预设到配置
            RTFConfig cfg = getOrCreateConfig();
            
            // 世界设置
            cfg.set("terrain.continent_scale", preset.world.continentScale);
            cfg.set("terrain.continent_shape", preset.world.continentShape);
            cfg.set("terrain.region_scale", preset.terrain.regionSize);
            cfg.set("terrain.mountain_height", preset.terrain.mountainHeight);
            cfg.set("terrain.volcano_chance", preset.terrain.volcanoChance);
            
            // 气候设置
            cfg.set("climate.biome_size", preset.climate.biomeSize);
            cfg.set("climate.biome_warp_scale", preset.climate.biomeWarpScale);
            cfg.set("climate.biome_warp_strength", preset.climate.biomeWarpStrength);
            
            // 表面设置
            cfg.set("surface.erosion_enabled", preset.surface.erosionEnabled);
            cfg.set("surface.strata_enabled", preset.surface.strataEnabled);
            cfg.set("surface.natural_snow_enabled", preset.surface.naturalSnowEnabled);
            
            // 保存配置
            cfg.save();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== 原有方法 ====================

    @Override
    public void initializeContext(RegistryAccess registryAccess, long seed) {
        this.seed = seed;
        // 默认海平面 63
        this.generatorContext = GeneratorContext.create((int) seed, 63);
        this.initialized = true;
    }

    @Override
    @Nullable
    public Object getGeneratorContext() {
        return generatorContext;
    }

    @Override
    public void setSpawnSearchCenter(BlockPos center) {
        this.spawnSearchCenter = center;
    }

    @Override
    public BlockPos getSpawnSearchCenter() {
        return spawnSearchCenter;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    @Nullable
    public Object applyCell(int x, int z) {
        if (generatorContext == null) {
            return null;
        }

        Cell cell = cellCache.get().reset();
        WorldLookup lookup = generatorContext.getLookup();

        if (lookup != null) {
            lookup.apply(cell, x, z);
        }

        return cell;
    }

    @Override
    public float getHeight(int x, int z) {
        Cell cell = (Cell) applyCell(x, z);
        if (cell != null) {
            return cell.height;
        }
        return 0.0F;
    }

    @Override
    public float getContinentalness(int x, int z) {
        Cell cell = (Cell) applyCell(x, z);
        if (cell != null) {
            return cell.continentalness;
        }
        return 0.0F;
    }

    @Override
    public float getErosion(int x, int z) {
        Cell cell = (Cell) applyCell(x, z);
        if (cell != null) {
            return cell.erosion;
        }
        return 0.0F;
    }

    @Override
    public float getCellField(int x, int z, String fieldName) {
        Cell cell = (Cell) applyCell(x, z);
        if (cell == null) {
            return 0.0F;
        }

        try {
            CellField field = CellField.valueOf(fieldName.toUpperCase());
            return field.read(cell);
        } catch (IllegalArgumentException e) {
            return 0.0F;
        }
    }

    @Override
    public void setSurfaceRegion(@Nullable Object region) {
        if (region instanceof WorldGenRegion worldGenRegion) {
            SurfaceRegion.set(worldGenRegion);
        } else {
            SurfaceRegion.set(null);
        }
    }

    @Override
    @Nullable
    public Object getSurfaceRegion() {
        return SurfaceRegion.get();
    }

    @Override
    @Nullable
    public Object createCellSampler(Object marker) {
        if (marker instanceof CellSampler.Marker m) {
            return new CellSampler(Suppliers.memoize(() -> this.generatorContext), m.field());
        }
        return null;
    }

    @Override
    @Nullable
    public Object createNoiseSampler(Object marker, int seed) {
        if (marker instanceof NoiseSampler.Marker m) {
            return new NoiseSampler(m.noise(), seed);
        }
        return null;
    }

    @Override
    public int getGenerationHeight(int chunkX, int chunkZ, NoiseGeneratorSettings settings) {
        if (generatorContext == null) {
            return settings.noiseSettings().height();
        }
        
        WorldLookup lookup = generatorContext.getLookup();
        if (lookup != null) {
            return lookup.getGenerationHeight(chunkX, chunkZ, settings, true);
        }
        
        return settings.noiseSettings().height();
    }

    @Override
    @Nullable
    public Object createCache2d() {
        return new CellSampler.Cache2d();
    }

    @Override
    @Nullable
    public Object getTileChunk(int chunkX, int chunkZ) {
        if (generatorContext == null) {
            return null;
        }
        
        // 从 TileCache 获取 Tile，然后获取 Chunk
        Tile tile = generatorContext.getCache().provideAtChunk(chunkX, chunkZ);
        if (tile != null) {
            return tile.getChunkReader(chunkX, chunkZ);
        }
        
        return null;
    }

    @Override
    @Nullable
    public Object createCachedCellSampler(Object cellSampler, @Nullable Object tileChunk, 
            @Nullable Object cache2d, int chunkX, int chunkZ) {
        if (cellSampler instanceof CellSampler sampler) {
            Tile.Chunk chunk = tileChunk instanceof Tile.Chunk tc ? tc : null;
            CellSampler.Cache2d cache = cache2d instanceof CellSampler.Cache2d c ? c : null;
            return sampler.new CacheChunk(chunk, cache, chunkX, chunkZ);
        }
        return null;
    }

    @Override
    public int getLavaLevel() {
        // 默认岩浆层高度，可以通过配置修改
        return -54;
    }

    @Override
    public void setCullNoiseSections(boolean cull) {
        WorldGenFlags.setCullNoiseSections(cull);
    }

    @Override
    public boolean isCullNoiseSections() {
        return WorldGenFlags.cullNoiseSections();
    }

    @Override
    public void setFastLookups(boolean fast) {
        WorldGenFlags.setFastLookups(fast);
    }

    @Override
    public boolean isFastLookups() {
        return WorldGenFlags.fastLookups();
    }

    @Override
    public void setFastCellLookups(boolean fast) {
        WorldGenFlags.setFastCellLookups(fast);
    }

    @Override
    public boolean isFastCellLookups() {
        return WorldGenFlags.fastCellLookups();
    }

    @Override
    public void queueTileAtChunk(int chunkX, int chunkZ) {
        if (generatorContext != null) {
            generatorContext.getCache().queueAtChunk(chunkX, chunkZ);
        }
    }

    @Override
    public void dropTileAtChunk(int chunkX, int chunkZ) {
        if (generatorContext != null) {
            generatorContext.getCache().dropAtChunk(chunkX, chunkZ);
        }
    }

    /**
     * 获取类型安全的 GeneratorContext
     */
    @Nullable
    public GeneratorContext getTypedGeneratorContext() {
        return generatorContext;
    }
}
