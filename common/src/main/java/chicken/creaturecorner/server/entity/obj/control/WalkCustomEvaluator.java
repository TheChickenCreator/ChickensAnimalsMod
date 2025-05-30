package chicken.creaturecorner.server.entity.obj.control;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class WalkCustomEvaluator extends NodeEvaluator {
    private final Long2ObjectMap<PathType> pathTypesByPosCacheByMob = new Long2ObjectOpenHashMap<>();
    private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();
    private final Node[] reusableNeighbors = new Node[Direction.Plane.HORIZONTAL.length()];

    @Override
    public void prepare(@NotNull PathNavigationRegion level, @NotNull Mob mob) {
        super.prepare(level, mob);
        mob.onPathfindingStart();
    }

    @Override
    public void done() {
        this.mob.onPathfindingDone();
        this.pathTypesByPosCacheByMob.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Override
    public @NotNull Node getStart() {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int i = this.mob.getBlockY();
        BlockState blockstate = this.currentContext.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), i, this.mob.getZ()));
        if (!this.mob.canStandOnFluid(blockstate.getFluidState())) {
            if (this.canFloat() && this.mob.isInWater()) {
                while (true) {
                    if (!blockstate.is(Blocks.WATER) && blockstate.getFluidState() != Fluids.WATER.getSource(false)) {
                        i--;
                        break;
                    }

                    blockstate = this.currentContext.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), ++i, this.mob.getZ()));
                }
            } else if (this.mob.onGround()) {
                i = Mth.floor(this.mob.getY());
            } else {
                blockpos$mutableblockpos.set(this.mob.getX(), this.mob.getY() + 1.0, this.mob.getZ());

                while (blockpos$mutableblockpos.getY() > this.currentContext.level().getMinBuildHeight()) {
                    i = blockpos$mutableblockpos.getY();
                    blockpos$mutableblockpos.setY(blockpos$mutableblockpos.getY() - 1);
                    BlockState blockstate1 = this.currentContext.getBlockState(blockpos$mutableblockpos);
                    if (!blockstate1.isAir() && !blockstate1.isPathfindable(PathComputationType.LAND)) {
                        break;
                    }
                }
            }
        } else {
            while (this.mob.canStandOnFluid(blockstate.getFluidState())) {
                blockstate = this.currentContext.getBlockState(blockpos$mutableblockpos.set(this.mob.getX(), ++i, this.mob.getZ()));
            }

            i--;
        }

        BlockPos blockpos = this.mob.blockPosition();
        if (!this.canStartAt(blockpos$mutableblockpos.set(blockpos.getX(), i, blockpos.getZ()))) {
            AABB aabb = this.mob.getBoundingBox();
            if (this.canStartAt(blockpos$mutableblockpos.set(aabb.minX, i, aabb.minZ))
                    || this.canStartAt(blockpos$mutableblockpos.set(aabb.minX, i, aabb.maxZ))
                    || this.canStartAt(blockpos$mutableblockpos.set(aabb.maxX, i, aabb.minZ))
                    || this.canStartAt(blockpos$mutableblockpos.set(aabb.maxX, i, aabb.maxZ))) {
                return this.getStartNode(blockpos$mutableblockpos);
            }
        }

        return this.getStartNode(new BlockPos(blockpos.getX(), i, blockpos.getZ()));
    }

    protected Node getStartNode(BlockPos pos) {
        Node node = this.getNode(pos);
        node.type = this.getCachedPathType(node.x, node.y, node.z);
        node.costMalus = this.mob.getPathfindingMalus(node.type);
        return node;
    }

    protected boolean canStartAt(BlockPos pos) {
        PathType pathtype = this.getCachedPathType(pos.getX(), pos.getY(), pos.getZ());
        return pathtype != PathType.OPEN && this.mob.getPathfindingMalus(pathtype) >= 0.0F;
    }

    @Override
    public @NotNull Target getTarget(double x, double y, double z) {
        return this.getTargetNodeAt(x, y, z);
    }

    @Override
    public int getNeighbors(Node @NotNull [] outputArray, Node p_node) {
        int i = 0;
        int j = 0;
        PathType pathtype = this.getCachedPathType(p_node.x, p_node.y + 1, p_node.z);
        PathType pathtype1 = this.getCachedPathType(p_node.x, p_node.y, p_node.z);
        if (this.mob.getPathfindingMalus(pathtype) >= 0.0F && pathtype1 != PathType.STICKY_HONEY) {
            j = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
        }

        double d0 = this.getFloorLevel(new BlockPos(p_node.x, p_node.y, p_node.z));

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Node node = this.findAcceptedNode(p_node.x + direction.getStepX(), p_node.y, p_node.z + direction.getStepZ(), j, d0, direction, pathtype1);
            this.reusableNeighbors[direction.get2DDataValue()] = node;
            if (this.isNeighborValid(node, p_node)) {
                outputArray[i++] = node;
            }
        }

        for (Direction direction1 : Direction.Plane.HORIZONTAL) {
            Direction direction2 = direction1.getClockWise();
            if (this.isDiagonalValid(p_node, this.reusableNeighbors[direction1.get2DDataValue()], this.reusableNeighbors[direction2.get2DDataValue()])) {
                Node node1 = this.findAcceptedNode(
                        p_node.x + direction1.getStepX() + direction2.getStepX(),
                        p_node.y,
                        p_node.z + direction1.getStepZ() + direction2.getStepZ(),
                        j,
                        d0,
                        direction1,
                        pathtype1
                );
                if (this.isDiagonalValid(node1)) {
                    outputArray[i++] = node1;
                }
            }
        }

        return i;
    }

    protected boolean isNeighborValid(Node neighbor, Node node) {
        return neighbor != null && !neighbor.closed && (neighbor.costMalus >= 0.0F || node.costMalus < 0.0F);
    }

    protected boolean isDiagonalValid(Node root, Node xNode, Node zNode) {
        if (zNode == null || xNode == null || zNode.y > root.y || xNode.y > root.y) {
            return false;
        } else if (xNode.type != PathType.WALKABLE_DOOR && zNode.type != PathType.WALKABLE_DOOR) {
            return (zNode.y < root.y || zNode.costMalus >= 0.0F) && (xNode.y < root.y || xNode.costMalus >= 0.0F);
        } else {
            return false;
        }
    }

    protected boolean isDiagonalValid(Node node) {
        if (node == null || node.closed) {
            return false;
        } else {
            return node.type != PathType.WALKABLE_DOOR;
        }
    }

    private static boolean doesBlockHavePartialCollision(PathType pathType) {
        return pathType == PathType.FENCE || pathType == PathType.DOOR_WOOD_CLOSED || pathType == PathType.DOOR_IRON_CLOSED;
    }

    private boolean canReachWithoutCollision(Node node) {
        AABB aabb = this.mob.getBoundingBox();
        Vec3 vec3 = new Vec3(
                (double)node.x - this.mob.getX() + aabb.getXsize(),
                (double)node.y - this.mob.getY() + aabb.getYsize(),
                (double)node.z - this.mob.getZ() + aabb.getZsize()
        );
        int i = Mth.ceil(vec3.length() / aabb.getSize());
        vec3 = vec3.scale(1.0F / (float)i);

        for (int j = 1; j <= i; j++) {
            aabb = aabb.move(vec3);
            if (this.hasCollisions(aabb)) {
                return false;
            }
        }

        return true;
    }

    protected double getFloorLevel(BlockPos pos) {
        BlockGetter blockgetter = this.currentContext.level();
        return (this.canFloat() || this.isAmphibious()) && blockgetter.getFluidState(pos).is(FluidTags.WATER)
                ? (double)pos.getY() + 0.5
                : getFloorLevel(blockgetter, pos);
    }

    public static double getFloorLevel(BlockGetter level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        VoxelShape voxelshape = level.getBlockState(blockpos).getCollisionShape(level, blockpos);
        return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0 : voxelshape.max(Direction.Axis.Y));
    }

    protected boolean isAmphibious() {
        return false;
    }

    protected Node findAcceptedNode(int x, int y, int z, int verticalDeltaLimit, double nodeFloorLevel, Direction direction, PathType pathType) {
        Node node = null;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        double d0 = this.getFloorLevel(blockpos$mutableblockpos.set(x, y, z));
        if (d0 - nodeFloorLevel > this.getMobJumpHeight()) {
            return null;
        } else {
            PathType pathtype = this.getCachedPathType(x, y, z);
            float f = this.mob.getPathfindingMalus(pathtype);
            if (f >= 0.0F) {
                node = this.getNodeAndUpdateCostToMax(x, y, z, pathtype, f);
            }

            if (doesBlockHavePartialCollision(pathType) && node != null && node.costMalus >= 0.0F && !this.canReachWithoutCollision(node)) {
                node = null;
            }

            if (pathtype != PathType.WALKABLE && (!this.isAmphibious() || pathtype != PathType.WATER)) {
                if ((node == null || node.costMalus < 0.0F)
                        && verticalDeltaLimit > 0
                        && (pathtype != PathType.FENCE || this.canWalkOverFences())
                        && pathtype != PathType.UNPASSABLE_RAIL
                        && pathtype != PathType.TRAPDOOR
                        && pathtype != PathType.POWDER_SNOW) {
                    node = this.tryJumpOn(x, y, z, verticalDeltaLimit, nodeFloorLevel, direction, pathType, blockpos$mutableblockpos);
                } else if (!this.isAmphibious() && pathtype == PathType.WATER && !this.canFloat()) {
                    node = this.tryFindFirstNonWaterBelow(x, y, z, node);
                } else if (pathtype == PathType.OPEN) {
                    node = this.tryFindFirstGroundNodeBelow(x, y, z);
                } else if (doesBlockHavePartialCollision(pathtype) && node == null) {
                    node = this.getClosedNode(x, y, z, pathtype);
                }

            }
            return node;
        }
    }

    private double getMobJumpHeight() {
        return Math.max(1.125, this.mob.maxUpStep());
    }

    private Node getNodeAndUpdateCostToMax(int x, int y, int z, PathType pathType, float malus) {
        Node node = this.getNode(x, y, z);
        node.type = pathType;
        node.costMalus = Math.max(node.costMalus, malus);
        return node;
    }

    private Node getBlockedNode(int x, int y, int z) {
        Node node = this.getNode(x, y, z);
        node.type = PathType.BLOCKED;
        node.costMalus = -1.0F;
        return node;
    }

    private Node getClosedNode(int x, int y, int z, PathType pathType) {
        Node node = this.getNode(x, y, z);
        node.closed = true;
        node.type = pathType;
        node.costMalus = pathType.getMalus();
        return node;
    }

    private Node tryJumpOn(
            int x,
            int y,
            int z,
            int verticalDeltaLimit,
            double nodeFloorLevel,
            Direction direction,
            PathType pathType,
            BlockPos.MutableBlockPos pos
    ) {
        Node node = this.findAcceptedNode(x, y + 1, z, verticalDeltaLimit, nodeFloorLevel, direction, pathType);
        if (node == null) {
            return null;
        } else if (node.type != PathType.OPEN && node.type != PathType.WALKABLE) {
            return node;
        } else {
            double d0 = x - direction.getStepX();
            double d1 = z - direction.getStepZ();
            double d2 = this.mob.getBbWidth();
            AABB aabb = new AABB(
                    d0 - d2,
                    this.getFloorLevel(pos.set(d0, y + 1, d1)),
                    d1 - d2,
                    d0 + d2,
                    (double)this.mob.getBbHeight() + this.getFloorLevel(pos.set(node.x, node.y, (double)node.z)),
                    d1 + d2
            );
            return this.hasCollisions(aabb) ? null : node;
        }
    }

    private Node tryFindFirstNonWaterBelow(int x, int y, int z, Node node) {
        y--;

        while (y > this.mob.level().getMinBuildHeight()) {
            PathType pathtype = this.getCachedPathType(x, y, z);
            if (pathtype != PathType.WATER) {
                return node;
            }

            node = this.getNodeAndUpdateCostToMax(x, y, z, pathtype, this.mob.getPathfindingMalus(pathtype));
            y--;
        }

        return node;
    }

    private Node tryFindFirstGroundNodeBelow(int x, int y, int z) {
        for (int i = y - 1; i >= this.mob.level().getMinBuildHeight(); i--) {
            if (y - i > this.mob.getMaxFallDistance()) {
                return this.getBlockedNode(x, i, z);
            }

            PathType pathtype = this.getCachedPathType(x, i, z);
            float f = this.mob.getPathfindingMalus(pathtype);
            if (pathtype != PathType.OPEN) {
                if (f >= 0.0F) {
                    return this.getNodeAndUpdateCostToMax(x, i, z, pathtype, f);
                }

                return this.getBlockedNode(x, i, z);
            }
        }

        return this.getBlockedNode(x, y, z);
    }

    private boolean hasCollisions(AABB boundingBox) {
        return this.collisionCache.computeIfAbsent(boundingBox, box -> {
            boolean noCollision = this.currentContext.level().noCollision(this.mob, boundingBox);
            if (!noCollision) {
                return false;
            }

            List<Entity> entities = this.mob.level().getEntities(this.mob, boundingBox.inflate(0.1F), entity -> entity instanceof LivingEntity && entity != this.mob);

            return entities.isEmpty();
        });
    }

    protected PathType getCachedPathType(int x, int y, int z) {
        return this.pathTypesByPosCacheByMob
                .computeIfAbsent(
                        BlockPos.asLong(x, y, z),
                        p_330161_ -> this.getPathTypeOfMob(this.currentContext, x, y, z, this.mob)
                );
    }

    @Override
    public @NotNull PathType getPathTypeOfMob(@NotNull PathfindingContext context, int x, int y, int z, @NotNull Mob mob) {
        Set<PathType> set = this.getPathTypeWithinMobBB(context, x, y, z);
        if (set.contains(PathType.FENCE)) {
            return PathType.FENCE;
        } else if (set.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        } else {
            PathType pathtype = PathType.BLOCKED;

            for (PathType pathtype1 : set) {
                if (mob.getPathfindingMalus(pathtype1) < 0.0F) {
                    return pathtype1;
                }

                if (mob.getPathfindingMalus(pathtype1) >= mob.getPathfindingMalus(pathtype)) {
                    pathtype = pathtype1;
                }
            }

            return  pathtype != PathType.OPEN
                    && mob.getPathfindingMalus(pathtype) == 0.0F
                    && this.getPathType(context, x, y, z) == PathType.OPEN
                    ? PathType.OPEN
                    : pathtype;
        }
    }

    public Set<PathType> getPathTypeWithinMobBB(PathfindingContext context, int x, int y, int z) {
        EnumSet<PathType> enumset = EnumSet.noneOf(PathType.class);

        for (int i = 0; i < this.entityWidth; i++) {
            for (int j = 0; j < this.entityHeight; j++) {
                for (int k = 0; k < this.entityDepth; k++) {
                    int l = i + x;
                    int i1 = j + y;
                    int j1 = k + z;
                    PathType pathtype = this.getPathType(context, l, i1, j1);
                    BlockPos blockpos = this.mob.blockPosition();
                    boolean flag = this.canPassDoors();
                    if (pathtype == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && flag) {
                        pathtype = PathType.WALKABLE_DOOR;
                    }

                    if (pathtype == PathType.DOOR_OPEN && !flag) {
                        pathtype = PathType.BLOCKED;
                    }

                    if (pathtype == PathType.RAIL
                            && this.getPathType(context, blockpos.getX(), blockpos.getY(), blockpos.getZ()) != PathType.RAIL
                            && this.getPathType(context, blockpos.getX(), blockpos.getY() - 1, blockpos.getZ()) != PathType.RAIL) {
                        pathtype = PathType.UNPASSABLE_RAIL;
                    }

                    enumset.add(pathtype);
                }
            }
        }

        return enumset;
    }

    @Override
    public @NotNull PathType getPathType(@NotNull PathfindingContext context, int x, int y, int z) {
        return getPathTypeStatic(context, new BlockPos.MutableBlockPos(x, y, z));
    }

    public static PathType getPathTypeStatic(PathfindingContext context, BlockPos.MutableBlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        PathType pathtype = context.getPathTypeFromState(i, j, k);
        if (pathtype == PathType.OPEN && j >= context.level().getMinBuildHeight() + 1) {
            return switch (context.getPathTypeFromState(i, j - 1, k)) {
                case OPEN, WATER, LAVA, WALKABLE -> PathType.OPEN;
                case DAMAGE_FIRE -> PathType.DAMAGE_FIRE;
                case DAMAGE_OTHER -> PathType.DAMAGE_OTHER;
                case STICKY_HONEY -> PathType.STICKY_HONEY;
                case POWDER_SNOW -> PathType.DANGER_POWDER_SNOW;
                case DAMAGE_CAUTIOUS -> PathType.DAMAGE_CAUTIOUS;
                case TRAPDOOR -> PathType.DANGER_TRAPDOOR;
                default -> checkNeighbourBlocks(context, i, j, k, PathType.WALKABLE);
            };
        } else {
            return pathtype;
        }
    }

    public static PathType checkNeighbourBlocks(PathfindingContext context, int x, int y, int z, PathType pathType) {
        for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
                for(int k = -1; k <= 1; ++k) {
                    if (i != 0 || k != 0) {
                        PathType pathType2 = context.getPathTypeFromState(x + i, y + j, z + k);
                        if (pathType2 == PathType.DAMAGE_OTHER) {
                            return PathType.DANGER_OTHER;
                        }

                        if (pathType2 == PathType.DAMAGE_FIRE || pathType2 == PathType.LAVA) {
                            return PathType.DANGER_FIRE;
                        }

                        if (pathType2 == PathType.WATER) {
                            return PathType.WATER_BORDER;
                        }

                        if (pathType2 == PathType.DAMAGE_CAUTIOUS) {
                            return PathType.DAMAGE_CAUTIOUS;
                        }
                    }
                }
            }
        }

        return pathType;
    }
}
