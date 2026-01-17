package org.virgil698.NekoTerraForged.mixin.world.density;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.virgil698.NekoTerraForged.mixin.math.Mth;
import org.virgil698.NekoTerraForged.mixin.math.Node;

/**
 * 密度节点包装器
 * 将 Minecraft 的 DensityFunction 转换为 Node 接口
 * 移植自 Valley
 */
public class DensityNode {
    public static final DensityNode EMPTY = new DensityNode(null);
    
    private volatile DensityFunction density;
    private final Holder<DensityFunction> entry;

    public DensityNode(Holder<DensityFunction> entry) {
        this.entry = entry;
    }

    public Holder<DensityFunction> entry() {
        return this.entry;
    }

    /**
     * 访问并初始化密度函数
     * @param visitor DensityFunction.Visitor
     */
    public void visit(DensityFunction.Visitor visitor) {
        if (this.entry != null) {
            this.density = visitor.apply(this.entry.value());
        }
    }

    /**
     * 获取包装后的 Node
     * 如果 density 未初始化，返回一个默认的 Node
     */
    public Node node() {
        if (this.density == null && this.entry != null) {
            // 如果 density 未初始化但 entry 存在，使用 entry 的值
            return Reader.wrap(this.entry.value());
        }
        return Reader.wrap(this.density);
    }

    /**
     * Reader - 将 DensityFunction 包装为 Node
     * 实现了 Node 和 DensityFunction.FunctionContext 接口
     */
    private static class Reader implements Node, DensityFunction.FunctionContext {
        private static final ThreadLocal<Reader> LOCAL = ThreadLocal.withInitial(Reader::new);
        private int x;
        private int z;
        private DensityFunction density;

        private Reader() {
        }

        @Override
        public double eval(int seed, double x, double y) {
            // 添加 null 检查，防止 NPE
            if (this.density == null) {
                return 0.0;
            }
            this.x = Mth.Floor(x);
            this.z = Mth.Floor(y);
            return this.density.compute(this);
        }

        @Override
        public int blockX() {
            return this.x;
        }

        @Override
        public int blockY() {
            return 0;
        }

        @Override
        public int blockZ() {
            return this.z;
        }

        /**
         * 包装 DensityFunction 为 Node
         * 如果 density 为 null，返回一个返回 0 的默认 Node
         */
        static Node wrap(DensityFunction density) {
            if (density == null) {
                // 返回一个默认的 Node，总是返回 0
                return ZERO_NODE;
            }
            if (density instanceof Node) {
                return (Node) density;
            }
            Reader reader = LOCAL.get();
            reader.density = density;
            return reader;
        }
        
        /**
         * 默认的零值 Node
         */
        private static final Node ZERO_NODE = new Node() {
            @Override
            public double eval(int seed, double x, double y) {
                return 0.0;
            }
        };
    }
}
