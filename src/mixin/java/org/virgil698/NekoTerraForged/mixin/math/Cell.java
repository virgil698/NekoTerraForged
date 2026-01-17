package org.virgil698.NekoTerraForged.mixin.math;

/**
 * Cell/Worley 噪声实现
 * 移植自 Valley
 */
public class Cell implements Node {
    private final GridType grid;
    private final ResultType result;
    private final double jitter;
    
    public static final double HEX_X_SCALE = 1.0;
    public static final double HEX_Y_SCALE = Math.pow(2.0, 0.25);

    public Cell(GridType grid, ResultType result, double jitter) {
        this.grid = grid;
        this.result = result;
        this.jitter = jitter;
    }

    @Override
    public double eval(int seed, double x, double y) {
        return Sample(seed, x, y, jitter, grid, result);
    }

    @Override
    public double min() {
        switch (result) {
            case VALUE_0:
            case VALUE_1:
                return -1.0;
            case DIST_0:
            case DIST_1:
            case DIST_ADD:
            case DIST_DIV:
            case DIST_MUL:
            case DIST_SUB:
                return 0.0;
            default:
                return -1.0;
        }
    }

    @Override
    public double max() {
        switch (result) {
            case VALUE_0:
            case VALUE_1:
            case DIST_0:
            case DIST_1:
            case DIST_DIV:
            case DIST_MUL:
            case DIST_SUB:
                return 1.0;
            case DIST_ADD:
                return 2.0;
            default:
                return 1.0;
        }
    }

    public enum GridType {
        SQUARE,
        HEX
    }

    public enum ResultType {
        VALUE_0,
        VALUE_1,
        DIST_0,
        DIST_1,
        DIST_ADD,
        DIST_DIV,
        DIST_MUL,
        DIST_SUB
    }

    public static double X2D(int hash) {
        return (hash & 0xFFFF) / 65535.0;
    }

    public static double Y2D(int hash) {
        return ((hash >> 16) & 0xFFFF) / 65535.0;
    }

    public static double PointX2D(int hash, int cx, int cy, double jitter) {
        double jitterX = (hash & 0xFFFF) / 65535.0;
        return cx + (jitterX * jitter);
    }

    public static double PointY2D(int hash, int cx, int cy, double jitter) {
        double jitterY = ((hash >> 16) & 0xFFFF) / 65535.0;
        return cy + (jitterY * jitter);
    }

    public static double HexPointX2D(int hash, int cx, int cy, double jitter) {
        double offsetX = (cy & 1) * 0.5;
        double jitterX = (hash & 0xFFFF) / 131070.0;
        return cx + offsetX + (jitterX * jitter);
    }

    public static double HexPointY2D(int hash, int cx, int cy, double jitter) {
        double jitterY = ((hash >> 16) & 0xFFFF) / 65535.0;
        return cy + (jitterY * jitter);
    }

    public static double Sample(int seed, double x, double y, double jitter, GridType gridType, ResultType resultType) {
        double x2 = GridScaleX(gridType, x);
        double y2 = GridScaleY(gridType, y);
        int maxX = Mth.Floor(x2) + 1;
        int maxY = Mth.Floor(y2) + 1;
        int hash0 = 0;
        int hash1 = 0;
        double min0 = Double.MAX_VALUE;
        double min1 = Double.MAX_VALUE;
        
        for (int cy = maxY - 2; cy <= maxY; cy++) {
            for (int cx = maxX - 2; cx <= maxX; cx++) {
                int hash = Mth.Hash(seed, cx, cy);
                double dist2 = GridDist2(x2, y2, cx, cy, hash, jitter, gridType);
                if (dist2 < min0) {
                    hash1 = hash0;
                    hash0 = hash;
                    min1 = min0;
                    min0 = dist2;
                } else if (dist2 < min1) {
                    min1 = dist2;
                    hash1 = hash;
                }
            }
        }
        
        switch (resultType) {
            case VALUE_0:
                return Mth.Noise(hash0);
            case VALUE_1:
                return Mth.Noise(hash1);
            case DIST_0:
                return Mth.Sqrt(min0);
            case DIST_1:
                return Mth.Sqrt(min1);
            case DIST_ADD:
                return Mth.Sqrt(min0 + min1);
            case DIST_DIV:
                return Mth.Sqrt(min0 / min1);
            case DIST_MUL:
                return Mth.Sqrt(min0 * min1);
            case DIST_SUB:
                return Mth.Sqrt(min1 - min0);
            default:
                return 0.0;
        }
    }

    public static double GridScaleX(GridType type, double x) {
        return x;
    }

    public static double GridScaleY(GridType type, double y) {
        switch (type) {
            case SQUARE:
                return y;
            case HEX:
                return y * HEX_Y_SCALE;
            default:
                return y;
        }
    }

    public static double PointX(GridType type, int hash, int cx, int cy, double jitter) {
        return type == GridType.HEX ? HexPointX2D(hash, cx, cy, jitter) : PointX2D(hash, cx, cy, jitter);
    }

    public static double PointY(GridType type, int hash, int cx, int cy, double jitter) {
        return type == GridType.HEX ? HexPointY2D(hash, cx, cy, jitter) : PointY2D(hash, cx, cy, jitter);
    }

    private static double GridDist2(double x, double y, int cx, int cy, int hash, double jitter, GridType type) {
        switch (type) {
            case SQUARE:
                return Mth.Dist2(x, y, PointX2D(hash, cx, cy, jitter), PointY2D(hash, cx, cy, jitter));
            case HEX:
                return Mth.Dist2(x, y, HexPointX2D(hash, cx, cy, jitter), HexPointY2D(hash, cx, cy, jitter));
            default:
                return 0.0;
        }
    }
}
