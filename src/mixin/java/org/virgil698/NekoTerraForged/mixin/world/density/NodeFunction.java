package org.virgil698.NekoTerraForged.mixin.world.density;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import org.virgil698.NekoTerraForged.mixin.math.Node;
import org.virgil698.NekoTerraForged.mixin.world.Continent;

/**
 * 节点密度函数
 * 用于大陆生成的密度函数实现
 * 同时实现 DensityFunction 和 Node 接口
 * 移植自 Valley
 */
public class NodeFunction implements DensityFunction, Node {
    private final int seed;
    private final Continent.Config config;

    public NodeFunction(int seed, Continent.Config config) {
        this.seed = seed;
        this.config = config;
    }

    public int seed() {
        return this.seed;
    }

    public Continent.Config config() {
        return this.config;
    }

    // ========== Node 接口实现 ==========
    
    @Override
    public double eval(int seed, double x, double y) {
        return this.config.node().eval(this.seed, x, y);
    }

    @Override
    public double min() {
        return this.config.node().min();
    }

    @Override
    public double max() {
        return this.config.node().max();
    }

    // ========== DensityFunction 接口实现 ==========
    
    @Override
    public double compute(FunctionContext context) {
        return this.config.node().eval(this.seed, context.blockX(), context.blockZ());
    }

    @Override
    public void fillArray(double[] array, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(array, this);
    }

    @NotNull
    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return this;
    }

    @Override
    public double minValue() {
        return this.config.node().min();
    }

    @Override
    public double maxValue() {
        return this.config.node().max();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        // Bukkit 插件不需要 Codec 序列化
        return null;
    }

    // ========== 工厂方法 ==========

    /**
     * 创建默认的大陆密度函数
     */
    public static NodeFunction continents() {
        return new NodeFunction(0, Continent.CreateDefault());
    }

    /**
     * 创建指定种子的大陆密度函数
     */
    public static NodeFunction continents(int seed) {
        return new NodeFunction(seed, Continent.CreateDefault());
    }

    /**
     * 创建自定义配置的大陆密度函数
     */
    public static NodeFunction create(int seed, Continent.Config config) {
        return new NodeFunction(seed, config);
    }
}
