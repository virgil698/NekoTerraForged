package org.virgil698.NekoTerraForged.mixin.worldgen.noise.domain;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noise;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.module.Noises;

/**
 * 域变形工厂类
 * 移植自 ReTerraForged
 */
public class Domains {

    public static Domain domainPerlin(int seed, int scale, int octaves, float strength) {
        return domain(
            Noises.perlin(seed, scale, octaves),
            Noises.perlin(seed + 1, scale, octaves),
            Noises.constant(strength)
        );
    }

    public static Domain domainSimplex(int seed, int scale, int octaves, float strength) {
        return domain(
            Noises.simplex(seed, scale, octaves),
            Noises.simplex(seed + 1, scale, octaves),
            Noises.constant(strength)
        );
    }

    public static Domain domain(Noise x, Noise z, Noise distance) {
        return new DomainWarp(x, z, distance);
    }

    public static Domain direct() {
        return new DirectDomain();
    }

    public static Domain add(Domain a, Domain b) {
        return new AddDomain(a, b);
    }

    public static Domain compound(Domain a, Domain b) {
        return new CompoundDomain(a, b);
    }

    public static Domain direction(Noise direction, Noise distance) {
        return new DirectionDomain(direction, distance);
    }

    /**
     * 直接域（无变形）
     */
    private static class DirectDomain implements Domain {
        @Override
        public float getOffsetX(float x, float z, int seed) {
            return 0;
        }

        @Override
        public float getOffsetZ(float x, float z, int seed) {
            return 0;
        }

        @Override
        public Domain mapAll(Noise.Visitor visitor) {
            return this;
        }
    }

    /**
     * 组合域
     */
    private static class AddDomain implements Domain {
        private final Domain a;
        private final Domain b;

        AddDomain(Domain a, Domain b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public float getOffsetX(float x, float z, int seed) {
            return a.getOffsetX(x, z, seed) + b.getOffsetX(x, z, seed);
        }

        @Override
        public float getOffsetZ(float x, float z, int seed) {
            return a.getOffsetZ(x, z, seed) + b.getOffsetZ(x, z, seed);
        }

        @Override
        public Domain mapAll(Noise.Visitor visitor) {
            return new AddDomain(a.mapAll(visitor), b.mapAll(visitor));
        }
    }

    /**
     * 复合域 - 先应用第一个域，再应用第二个域
     */
    private static class CompoundDomain implements Domain {
        private final Domain a;
        private final Domain b;

        CompoundDomain(Domain a, Domain b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public float getOffsetX(float x, float z, int seed) {
            float ax = a.getOffsetX(x, z, seed);
            float az = a.getOffsetZ(x, z, seed);
            return ax + b.getOffsetX(x + ax, z + az, seed);
        }

        @Override
        public float getOffsetZ(float x, float z, int seed) {
            float ax = a.getOffsetX(x, z, seed);
            float az = a.getOffsetZ(x, z, seed);
            return az + b.getOffsetZ(x + ax, z + az, seed);
        }

        @Override
        public Domain mapAll(Noise.Visitor visitor) {
            return new CompoundDomain(a.mapAll(visitor), b.mapAll(visitor));
        }
    }

    /**
     * 方向域
     */
    private static class DirectionDomain implements Domain {
        private final Noise direction;
        private final Noise distance;

        DirectionDomain(Noise direction, Noise distance) {
            this.direction = direction;
            this.distance = distance;
        }

        @Override
        public float getOffsetX(float x, float z, int seed) {
            float angle = direction.compute(x, z, seed) * 6.2831855F;
            float dist = distance.compute(x, z, seed);
            return (float) Math.cos(angle) * dist;
        }

        @Override
        public float getOffsetZ(float x, float z, int seed) {
            float angle = direction.compute(x, z, seed) * 6.2831855F;
            float dist = distance.compute(x, z, seed);
            return (float) Math.sin(angle) * dist;
        }

        @Override
        public Domain mapAll(Noise.Visitor visitor) {
            return new DirectionDomain(direction.mapAll(visitor), distance.mapAll(visitor));
        }
    }
}
