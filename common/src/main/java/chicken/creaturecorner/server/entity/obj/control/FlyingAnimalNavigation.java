package chicken.creaturecorner.server.entity.obj.control;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FlyingAnimalNavigation extends FlyingPathNavigation {
    public FlyingAnimalNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected void followThePath() {
        Vec3 vec3 = this.getTempMobPos();
        this.maxDistanceToWaypoint = Math.max(0.5F, this.mob.getBbWidth()/2);
        assert this.path != null;
        Vec3i vec3i = this.path.getNextNodePos();
        double d0 = Math.abs(this.mob.getX() - ((double)vec3i.getX() + ((int)(this.mob.getBbWidth()/2))));
        double d1 = Math.abs(this.mob.getY() - (double)vec3i.getY());
        double d2 = Math.abs(this.mob.getZ() - ((double)vec3i.getZ() + ((int)(this.mob.getBbWidth()/2))));
        boolean flag = d0 <= (double)this.maxDistanceToWaypoint && d2 <= (double)this.maxDistanceToWaypoint && d1 < this.mob.getBbHeight();
        if (flag || this.shouldTargetNextNodeInDirection(vec3)) {
            this.path.advance();
        }

        this.doStuckDetection(vec3);
    }

    @Override
    protected @NotNull PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new FlyNodeEvaluator();
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 vec) {
        assert this.path != null;
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vec3 vec3 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
            if (!vec.closerThan(vec3, this.mob.getBbWidth()/2)) {
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
                    Vec3 vec34 = vec32.normalize();
                    Vec3 vec35 = vec33.normalize();
                    return vec35.dot(vec34) < 0.0;
                }
            }
        }
    }
}
