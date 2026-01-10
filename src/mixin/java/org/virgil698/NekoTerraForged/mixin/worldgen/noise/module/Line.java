package org.virgil698.NekoTerraForged.mixin.worldgen.noise.module;

import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;

/**
 * 线段工具类
 * 移植自 ReTerraForged
 */
public class Line {

    /**
     * 计算点到线段的距离参数 t
     */
    public static float distanceOnLine(float px, float pz, float x1, float z1, float x2, float z2) {
        float dx = x2 - x1;
        float dz = z2 - z1;
        float lengthSq = dx * dx + dz * dz;
        if (lengthSq == 0.0F) {
            return 0.0F;
        }
        float t = ((px - x1) * dx + (pz - z1) * dz) / lengthSq;
        return NoiseUtil.clamp(t, 0.0F, 1.0F);
    }

    /**
     * 计算点到线段的距离
     */
    public static float distanceToLine(float px, float pz, float x1, float z1, float x2, float z2) {
        float t = distanceOnLine(px, pz, x1, z1, x2, z2);
        float closestX = x1 + t * (x2 - x1);
        float closestZ = z1 + t * (z2 - z1);
        float dx = px - closestX;
        float dz = pz - closestZ;
        return NoiseUtil.sqrt(dx * dx + dz * dz);
    }

    /**
     * 检查两条线段是否相交
     */
    public static boolean intersect(float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4) {
        float d1 = direction(x3, z3, x4, z4, x1, z1);
        float d2 = direction(x3, z3, x4, z4, x2, z2);
        float d3 = direction(x1, z1, x2, z2, x3, z3);
        float d4 = direction(x1, z1, x2, z2, x4, z4);

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }

        if (d1 == 0 && onSegment(x3, z3, x4, z4, x1, z1)) return true;
        if (d2 == 0 && onSegment(x3, z3, x4, z4, x2, z2)) return true;
        if (d3 == 0 && onSegment(x1, z1, x2, z2, x3, z3)) return true;
        if (d4 == 0 && onSegment(x1, z1, x2, z2, x4, z4)) return true;

        return false;
    }

    private static float direction(float x1, float z1, float x2, float z2, float x3, float z3) {
        return (x3 - x1) * (z2 - z1) - (x2 - x1) * (z3 - z1);
    }

    private static boolean onSegment(float x1, float z1, float x2, float z2, float px, float pz) {
        return Math.min(x1, x2) <= px && px <= Math.max(x1, x2) &&
               Math.min(z1, z2) <= pz && pz <= Math.max(z1, z2);
    }

    /**
     * 计算两点之间的距离平方
     */
    public static float distSq(float x1, float z1, float x2, float z2) {
        float dx = x2 - x1;
        float dz = z2 - z1;
        return dx * dx + dz * dz;
    }
}
