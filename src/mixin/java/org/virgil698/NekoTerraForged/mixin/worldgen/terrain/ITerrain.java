package org.virgil698.NekoTerraForged.mixin.worldgen.terrain;

/**
 * 地形接口
 * 移植自 ReTerraForged
 */
public interface ITerrain {
    default float erosionModifier() {
        return 1.0F;
    }

    default boolean isFlat() {
        return false;
    }

    default boolean isRiver() {
        return false;
    }

    default boolean isLake() {
        return false;
    }

    default boolean isWetland() {
        return false;
    }

    default boolean isCoast() {
        return false;
    }

    default boolean isShallowOcean() {
        return false;
    }

    default boolean isDeepOcean() {
        return false;
    }

    default boolean isOcean() {
        return isShallowOcean() || isDeepOcean();
    }

    default boolean isMountain() {
        return false;
    }

    default boolean isVolcano() {
        return false;
    }

    default boolean isSubmerged() {
        return isDeepOcean() || isShallowOcean() || isRiver() || isLake();
    }

    default boolean isOverground() {
        return false;
    }

    default boolean overridesRiver() {
        return isDeepOcean() || isShallowOcean() || isCoast();
    }

    default boolean overridesCoast() {
        return isVolcano();
    }

    interface Delegate extends ITerrain {
        TerrainCategory getDelegate();

        @Override
        default float erosionModifier() {
            return getDelegate().erosionModifier();
        }

        @Override
        default boolean isFlat() {
            return getDelegate().isFlat();
        }

        @Override
        default boolean isRiver() {
            return getDelegate().isRiver();
        }

        @Override
        default boolean isLake() {
            return getDelegate().isLake();
        }

        @Override
        default boolean isWetland() {
            return getDelegate().isWetland();
        }

        @Override
        default boolean isCoast() {
            return getDelegate().isCoast();
        }

        @Override
        default boolean isShallowOcean() {
            return getDelegate().isShallowOcean();
        }

        @Override
        default boolean isDeepOcean() {
            return getDelegate().isDeepOcean();
        }

        @Override
        default boolean isOcean() {
            return getDelegate().isOcean();
        }

        @Override
        default boolean isMountain() {
            return getDelegate().isMountain();
        }

        @Override
        default boolean isVolcano() {
            return getDelegate().isVolcano();
        }

        @Override
        default boolean isSubmerged() {
            return getDelegate().isSubmerged();
        }

        @Override
        default boolean isOverground() {
            return getDelegate().isOverground();
        }

        @Override
        default boolean overridesRiver() {
            return getDelegate().overridesRiver();
        }

        @Override
        default boolean overridesCoast() {
            return getDelegate().overridesCoast();
        }
    }
}
