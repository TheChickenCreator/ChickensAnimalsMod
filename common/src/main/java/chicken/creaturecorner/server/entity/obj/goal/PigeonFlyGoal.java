package chicken.creaturecorner.server.entity.obj.goal;

import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;

public class PigeonFlyGoal extends WaterAvoidingRandomFlyingGoal {
    private final PigeonEntity pigeonEntity;
    public PigeonFlyGoal(PigeonEntity pMob, double pSpeedModifier) {
        super(pMob, pSpeedModifier);
        this.pigeonEntity = pMob;
        this.interval = 55;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !this.mob.isBaby() && !this.mob.isPanicking();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && !this.mob.isPanicking();
    }

    @Override
    public void start() {
        this.pigeonEntity.setFlying(true);
        super.start();
    }

    protected Vec3 getPosition() {
        Vec3 vec3 = this.mob.getViewVector(0.0F);
        Vec3 vec31 = HoverRandomPos.getPos(this.mob, 16, 16, vec3.x, vec3.z, 1.5707964F, 3, 1);
        return vec31 != null ? vec31 : AirAndWaterRandomPos.getPos(this.mob, 16, 16, -2, vec3.x, vec3.z, 1.5707963705062866);
    }

    @Override
    public void stop() {
        this.pigeonEntity.setFlying(false);
        super.stop();
    }
}
