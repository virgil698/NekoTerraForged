package org.virgil698.NekoTerraForged.mixin.world.density;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import org.virgil698.NekoTerraForged.mixin.math.Node;
import org.virgil698.NekoTerraForged.mixin.math.Spline;
import org.virgil698.NekoTerraForged.mixin.world.river.River;
import org.virgil698.NekoTerraForged.mixin.world.river.RiverGenerator;

/**
 * 河流密度函数
 * 用于河流生成的密度函数实现
 * 同时实现 DensityFunction.SimpleFunction 和 Node 接口
 * 移植自 Valley
 */
public class RiverFunction implements DensityFunction.SimpleFunction, Node {
    private final int seed;
    private final River.Config config;
    private final Spline output;
    private final DensityNode continents;
    private final River.RegionCache cache;

    public RiverFunction(int seed, River.Config config, Spline output, DensityNode continents, River.RegionCache cache) {
        this.seed = seed;
        this.config = config;
        this.output = output;
        this.continents = continents;
        this.cache = cache;
    }

    public RiverFunction(int seed, River.Config config, Spline output, DensityNode continents) {
        this(seed, config, output, continents, River.RegionCache.Create());
    }

    public int seed() {
        return this.seed;
    }

    public River.Config config() {
        return this.config;
    }

    public Spline output() {
        return this.output;
    }

    public DensityNode continents() {
        return this.continents;
    }

    public River.RegionCache cache() {
        return this.cache;
    }

    // ========== Node 接口实现 ==========

    @Override
    public double min() {
        return -1.0;
    }

    @Override
    public double max() {
        return 1.0;
    }

    @Override
    public double eval(int seed, double x, double y) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(y);
        
        // 防御性检查：如果 continents 为 null 或 EMPTY，返回默认值
        if (this.continents == null || this.continents == DensityNode.EMPTY) {
            return 0.0;
        }
        
        Node noise = this.continents.node();
        double dist = RiverGenerator.Sample(this.seed, blockX, blockZ, noise, this.config, this.cache);
        double sign = RiverGenerator.Sign(this.seed, blockX, blockZ);
        return sign * this.output.eval(dist);
    }

    // ========== DensityFunction 接口实现 ==========

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX();
        int z = context.blockZ();
        
        // 防御性检查：如果 continents 为 null 或 EMPTY，返回默认值
        if (this.continents == null || this.continents == DensityNode.EMPTY) {
            return 0.0;
        }
        
        Node noise = this.continents.node();
        double dist = RiverGenerator.Sample(this.seed, x, z, noise, this.config, this.cache);
        double sign = RiverGenerator.Sign(this.seed, x, z);
        return sign * this.output.eval(dist);
    }

    @NotNull
    @Override
    public DensityFunction mapAll(Visitor visitor) {
        // 只有当 continents 有有效的 entry 时才调用 visit
        if (this.continents != null && this.continents != DensityNode.EMPTY && this.continents.entry() != null) {
            this.continents.visit(visitor);
        }
        return this;
    }

    @Override
    public double minValue() {
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        // Bukkit 插件不需要 Codec 序列化
        return null;
    }

    // ========== 工厂方法 ==========

    /**
     * 创建预设的河流密度函数
     */
    public static RiverFunction preset() {
        return new RiverFunction(0, River.Config.Defaults(), defaultOutput(), DensityNode.EMPTY, null);
    }

    /**
     * 创建指定种子的河流密度函数
     */
    public static RiverFunction create(int seed, River.Config config, DensityNode continents) {
        return new RiverFunction(seed, config, defaultOutput(), continents);
    }

    /**
     * 默认的河流输出样条曲线
     */
    private static Spline defaultOutput() {
        return Spline.Of(new double[][]{
            {RiverGenerator.VALLEY_OUTER, 1.0},
            {0.85, 0.3},
            {1.0, RiverGenerator.VALLEY_OUTER}
        });
    }
}
