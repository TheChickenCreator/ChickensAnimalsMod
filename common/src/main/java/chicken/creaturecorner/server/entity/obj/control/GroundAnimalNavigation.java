package chicken.creaturecorner.server.entity.obj.control;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GroundAnimalNavigation extends GroundPathNavigation {
    public GroundAnimalNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected void followThePath() {
        Vec3 vec3 = this.getTempMobPos();
        this.maxDistanceToWaypoint = Math.max(0.5F, this.mob.getBbWidth()/2);
        assert this.path != null;
        Vec3 vec3i = this.path.getNextNodePos().getCenter();
        double hw = mob.getBbWidth()/2;
        double d0 = Math.abs(this.mob.getX() - vec3i.x() + hw);
        double d1 = Math.abs(this.mob.getY() - vec3i.y());
        double d2 = Math.abs(this.mob.getZ() - vec3i.z() + hw);
        boolean flag = d0 <= (double)this.maxDistanceToWaypoint && d2 <= (double)this.maxDistanceToWaypoint && d1 < this.mob.getBbHeight();
        if (flag || this.shouldTargetNextNodeInDirection(vec3)) {
            this.path.advance();
        }

        this.doStuckDetection(vec3);
    }

    @Override
    protected @NotNull PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new WalkCustomEvaluator();
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canMoveDirectly(@NotNull Vec3 posVec31, @NotNull Vec3 posVec32) {
        return isClearForMovementBetween(this.mob, posVec31, posVec32);
    }

    protected static boolean isClearForMovementBetween(Mob mob, Vec3 pos1, Vec3 pos2) {
        Vec3 vec3 = new Vec3(pos2.x, pos2.y + (mob.getBbHeight()-0.5), pos2.z);
        return mob.level().clip(new ClipContext(pos1, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob)).getType() == HitResult.Type.MISS;
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 vec) {
        assert this.path != null;
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vec3 vec3 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
            if (!vec.closerThan(vec3, this.mob.getBbWidth())) {
                return false;
            } else if (this.canMoveDirectly(vec, this.path.getNextEntityPos(this.mob))) {
                return true;
            } else {
                Vec3 vec31 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec3 vec32 = vec3.subtract(vec);
                Vec3 vec33 = vec31.subtract(vec);
                double d0 = vec32.lengthSqr();
                double d1 = vec33.lengthSqr();
                boolean flag = d1 < d0;
                boolean flag1 = d0 < 1;
                if (!flag && !flag1) {
                    return false;
                } else {
                    return vec33.distanceToSqr(vec32) < (mob.getBbWidth()*1.5);
                }
            }
        }
    }
}
