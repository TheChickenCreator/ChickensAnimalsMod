package chicken.creaturecorner.server.entity.obj.geo.goal;

import chicken.creaturecorner.server.entity.obj.geo.GeoTamableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class GeoSitWhenOrderedToGoal extends Goal {
    private final GeoTamableEntity mob;

    public GeoSitWhenOrderedToGoal(GeoTamableEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canContinueToUse() {
        return true;//this.mob.isOrderedToSit();
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isTame()) {
            return false;
        } else if (this.mob.isInWaterOrBubble()) {
            return false;
        } else if (!this.mob.onGround()) {
            return false;
        } else {
            LivingEntity livingentity = this.mob.getOwner();
            if (livingentity == null) {
                return true;
            } else {
                return false;//(!(this.mob.distanceToSqr(livingentity) < 144.0) || livingentity.getLastHurtByMob() == null) && this.mob.isOrderedToSit();
            }
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.setInSittingPose(true);
    }

    @Override
    public void stop() {
        this.mob.setInSittingPose(false);
    }
}
