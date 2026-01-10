package org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.river;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.virgil698.NekoTerraForged.mixin.worldgen.GeneratorContext;
import org.virgil698.NekoTerraForged.mixin.worldgen.continent.Continent;
import org.virgil698.NekoTerraForged.mixin.worldgen.heightmap.Levels;
import org.virgil698.NekoTerraForged.mixin.worldgen.noise.NoiseUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.RiverGenerator;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.Rivermap;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.gen.GenWarp;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.lake.LakeConfig;
import org.virgil698.NekoTerraForged.mixin.worldgen.rivermap.wetland.WetlandConfig;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.populator.LakePopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.populator.RiverPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.terrain.populator.WetlandPopulator;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.PosUtil;
import org.virgil698.NekoTerraForged.mixin.worldgen.util.Variance;

/**
 * 基础河流生成器
 * 移植自 ReTerraForged
 */
public abstract class BaseRiverGenerator<T extends Continent> implements RiverGenerator {
    protected int count;
    protected int continentScale;
    protected float minEdgeValue;
    protected int seed;
    protected LakeConfig lake;
    protected RiverConfig main;
    protected RiverConfig fork;
    protected WetlandConfig wetland;
    protected T continent;
    protected Levels levels;

    public BaseRiverGenerator(T continent, GeneratorContext context) {
        this.continent = continent;
        this.levels = context.levels;
        this.continentScale = 3000;
        this.minEdgeValue = 0.45F;
        this.seed = context.seed.root();
        this.count = 14;
        this.main = RiverConfig.builder(context.levels)
            .bankHeight(1, 6)
            .bankWidth(15)
            .bedWidth(4)
            .bedDepth(5)
            .fade(0.2F)
            .length(5000)
            .main(true)
            .order(0)
            .build();
        this.fork = RiverConfig.builder(context.levels)
            .bankHeight(1, 4)
            .bankWidth(10)
            .bedWidth(3)
            .bedDepth(4)
            .fade(0.15F)
            .length(4500)
            .order(1)
            .build();
        this.wetland = WetlandConfig.defaults();
        this.lake = LakeConfig.of(context.levels);
    }

    @Override
    public Rivermap generateRivers(int x, int z, long id) {
        Random random = new Random(id + this.seed);
        GenWarp warp = GenWarp.make((int) id, this.continentScale);
        List<Network.Builder> rivers = this.generateRoots(x, z, random, warp);
        Collections.shuffle(rivers, random);
        for (Network.Builder root : rivers) {
            this.generateForks(root, River.MAIN_SPACING, this.fork, random, warp, rivers, 0);
        }
        for (Network.Builder river : rivers) {
            this.generateWetlands(river, random);
        }
        Network[] networks = rivers.stream().map(Network.Builder::build).toArray(Network[]::new);
        return new Rivermap(x, z, networks, warp);
    }

    public List<Network.Builder> generateRoots(int x, int z, Random random, GenWarp warp) {
        return Collections.emptyList();
    }


    public void generateForks(Network.Builder parent, Variance spacing, RiverConfig config, Random random, GenWarp warp, List<Network.Builder> rivers, int depth) {
        if (depth > 2) {
            return;
        }
        float length = 0.44F * parent.carver.river.length;
        if (length < 300.0f) {
            return;
        }
        int direction = random.nextBoolean() ? 1 : -1;
        for (float offset = 0.25F; offset < 0.9f; offset += spacing.next(random)) {
            for (boolean attempt = true; attempt; attempt = false) {
                direction = -direction;
                float parentAngle = parent.carver.river.getAngle();
                float forkAngle = direction * 6.2831855F * River.FORK_ANGLE.next(random);
                float angle = parentAngle + forkAngle;
                float dx = NoiseUtil.sin(angle);
                float dz = NoiseUtil.cos(angle);
                long v1 = parent.carver.river.pos(offset);
                float x1 = PosUtil.unpackLeftf(v1);
                float z1 = PosUtil.unpackRightf(v1);
                if (this.continent.getEdgeValue(x1, z1) >= this.minEdgeValue) {
                    float x2 = x1 - dx * length;
                    float z2 = z1 - dz * length;
                    if (this.continent.getEdgeValue(x2, z2) >= this.minEdgeValue) {
                        RiverConfig forkConfig = parent.carver.createForkConfig(offset, this.levels);
                        River river = new River(x2, z2, x1, z1);
                        if (!this.riverOverlaps(river, parent, rivers)) {
                            float valleyWidth = 275.0f * River.FORK_VALLEY.next(random);
                            RiverPopulator.Settings settings = creatSettings(random);
                            settings.connecting = true;
                            settings.fadeIn = config.fade;
                            settings.valleySize = valleyWidth;
                            RiverWarp forkWarp = parent.carver.warp.createChild(0.15f, 0.75f, 0.65f, random);
                            RiverPopulator fork = new RiverPopulator(river, forkWarp, forkConfig, settings, this.levels);
                            Network.Builder builder = Network.builder(fork);
                            parent.children.add(builder);
                            this.generateForks(builder, River.FORK_SPACING, config, random, warp, rivers, depth + 1);
                        }
                    }
                }
            }
        }
        this.addLake(parent, random, warp);
    }

    public void generateWetlands(Network.Builder builder, Random random) {
        int skip = random.nextInt(this.wetland.skipSize);
        if (skip == 0) {
            float width = this.wetland.width.next(random);
            float length = this.wetland.length.next(random);
            float riverLength = builder.carver.river.length();
            float startPos = random.nextFloat() * 0.75f;
            float endPos = startPos + random.nextFloat() * (length / riverLength);
            long start = builder.carver.river.pos(startPos);
            long end = builder.carver.river.pos(endPos);
            float x1 = PosUtil.unpackLeftf(start);
            float z1 = PosUtil.unpackRightf(start);
            float x2 = PosUtil.unpackLeftf(end);
            float z2 = PosUtil.unpackRightf(end);
            builder.wetlands.add(new WetlandPopulator(random.nextInt(), new NoiseUtil.Vec2f(x1, z1), new NoiseUtil.Vec2f(x2, z2), width, this.levels));
        }
        for (Network.Builder child : builder.children) {
            this.generateWetlands(child, random);
        }
    }

    public void addLake(Network.Builder branch, Random random, GenWarp warp) {
        if (random.nextFloat() <= this.lake.chance) {
            float lakeSize = this.lake.sizeMin + random.nextFloat() * this.lake.sizeRange;
            float cx = branch.carver.river.x1;
            float cz = branch.carver.river.z1;
            if (this.lakeOverlapsOther(cx, cz, lakeSize, branch.lakes)) {
                return;
            }
            branch.lakes.add(new LakePopulator(new NoiseUtil.Vec2f(cx, cz), lakeSize, 1.0f, this.lake));
        }
    }

    public boolean riverOverlaps(River river, Network.Builder parent, List<Network.Builder> rivers) {
        for (Network.Builder other : rivers) {
            if (other.overlaps(river, parent, 250.0f)) {
                return true;
            }
        }
        return false;
    }

    public boolean lakeOverlapsOther(float x, float z, float size, List<LakePopulator> lakes) {
        float dist2 = size * size;
        for (LakePopulator other : lakes) {
            if (other.overlaps(x, z, dist2)) {
                return true;
            }
        }
        return false;
    }

    public static RiverPopulator.Settings creatSettings(Random random) {
        RiverPopulator.Settings settings = new RiverPopulator.Settings();
        settings.valleyCurve = RiverPopulator.getValleyType(random);
        return settings;
    }
}
