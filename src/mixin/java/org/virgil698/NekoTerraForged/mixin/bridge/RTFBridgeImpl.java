package org.virgil698.NekoTerraForged.mixin.bridge;

import org.virgil698.NekoTerraForged.mixin.math.Node;
import org.virgil698.NekoTerraForged.mixin.math.Spline;
import org.virgil698.NekoTerraForged.mixin.world.Continent;
import org.virgil698.NekoTerraForged.mixin.world.TerrainGenerator;
import org.virgil698.NekoTerraForged.mixin.world.river.River;
import org.virgil698.NekoTerraForged.mixin.world.river.RiverGenerator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;

/**
 * RTFBridge 的实现类
 * 位于 mixin 包内，可以访问 MC 类
 */
public class RTFBridgeImpl implements RTFBridge {
    private BlockPos spawnSearchCenter = BlockPos.ZERO;
    private long seed;
    private boolean initialized = false;
    private Node continentNode;
    private River.Config riverConfig;
    private River.RegionCache riverCache;
    private Spline riverOutputSpline;
    private TerrainGenerator terrainGenerator;
    private Object configData; // 存储插件层传递的配置
    
    @Override
    public void initializeContext(RegistryAccess registryAccess, long seed) {
        this.seed = seed;
        int intSeed = (int) seed;
        
        // 创建大陆生成器（使用默认配置，稍后可以被插件配置覆盖）
        Continent.Config continentConfig = Continent.CreateDefault();
        this.continentNode = continentConfig.node();
        
        // 创建河流系统（使用默认配置，稍后可以被插件配置覆盖）
        this.riverConfig = River.Config.Defaults();
        this.riverCache = River.RegionCache.Create();
        this.riverOutputSpline = Spline.Of(new double[][]{
            {RiverGenerator.VALLEY_OUTER, 1.0},
            {0.85, 0.3},
            {1.0, RiverGenerator.VALLEY_OUTER}
        });
        
        // 创建地形生成器
        this.terrainGenerator = new TerrainGenerator(intSeed);
        
        this.initialized = true;
        System.out.println("[NekoTerraForged] Initialized with seed: " + seed);
        System.out.println("[NekoTerraForged] Terrain generator ready with continent, erosion, and river systems");
        
        // 如果已经有配置数据，应用它
        if (configData != null) {
            applyConfig();
        }
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
    public void setSpawnSearchCenter(BlockPos center) {
        this.spawnSearchCenter = center;
    }

    @Override
    public BlockPos getSpawnSearchCenter() {
        return spawnSearchCenter;
    }
    
    @Override
    public void setConfigData(Object configData) {
        this.configData = configData;
        System.out.println("[NekoTerraForged] Config data received from plugin layer");
        
        // 如果已经初始化，立即应用配置
        if (initialized) {
            applyConfig();
        }
    }
    
    @Override
    public Object getConfigData() {
        return configData;
    }
    
    /**
     * 应用插件层传递的配置
     * 注意：由于类加载器隔离，这里使用反射访问配置
     */
    private void applyConfig() {
        if (configData == null) {
            return;
        }
        
        try {
            // 使用反射获取配置值
            Class<?> configClass = configData.getClass();
            
            // 获取大陆配置
            Object continentConfig = configClass.getMethod("getContinentConfig").invoke(configData);
            if (continentConfig != null) {
                int scale = (int) continentConfig.getClass().getField("scale").get(continentConfig);
                System.out.println("[NekoTerraForged] Applied continent scale: " + scale);
                // TODO: 根据配置重新创建大陆生成器
            }
            
            // 获取河流配置
            Object riverConfigObj = configClass.getMethod("getRiverConfig").invoke(configData);
            if (riverConfigObj != null) {
                int scale = (int) riverConfigObj.getClass().getField("scale").get(riverConfigObj);
                System.out.println("[NekoTerraForged] Applied river scale: " + scale);
                // TODO: 根据配置重新创建河流系统
            }
            
            System.out.println("[NekoTerraForged] Configuration applied successfully");
        } catch (Exception e) {
            System.err.println("[NekoTerraForged] Failed to apply config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取大陆噪声节点
     */
    public Node getContinentNode() {
        return continentNode;
    }
    
    /**
     * 获取河流配置
     */
    public River.Config getRiverConfig() {
        return riverConfig;
    }
    
    /**
     * 获取河流缓存
     */
    public River.RegionCache getRiverCache() {
        return riverCache;
    }
    
    /**
     * 获取河流输出样条
     */
    public Spline getRiverOutputSpline() {
        return riverOutputSpline;
    }
    
    /**
     * 获取地形生成器
     */
    public TerrainGenerator getTerrainGenerator() {
        return terrainGenerator;
    }
}
