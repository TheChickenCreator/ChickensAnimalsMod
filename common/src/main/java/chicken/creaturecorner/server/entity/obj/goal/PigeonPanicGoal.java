package chicken.creaturecorner.server.entity.obj.goal;

import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PigeonPanicGoal extends Goal {
    private final PigeonEntity pigeonEntity;
    protected LivingEntity lastHurtEntity;
    protected double posX;
    protected double posY;
    protected double posZ;
    public PigeonPanicGoal(PigeonEntity pMob) {
        this.pigeonEntity = pMob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if(!shouldPanic()) {
            return false;
        } else {
            if (this.pigeonEntity.isOnFire()) {
                BlockPos blockpos = this.lookForWater(this.pigeonEntity.level(), this.pigeonEntity, 5);
                if (blockpos != null) {
                    this.posX = blockpos.getX();
                    this.posY = blockpos.getY();
                    this.posZ = blockpos.getZ();
                    return true;
                }
            }

            return this.findRandomPosition();
        }
    }

    @Override
    public void start() {
        this.pigeonEntity.setFlying(false);
        this.pigeonEntity.setPanic(true);
        this.pigeonEntity.getNavigation().moveTo(this.posX, this.posY, this.posZ, 1.6F);
    }

    @Override
    public void stop() {
        this.pigeonEntity.setPanic(false);
    }

    public boolean canContinueToUse() {
        return !this.pigeonEntity.getNavigation().isDone();
    }

    protected boolean findRandomPosition() {
        Vec3 vec3 = null;
        if(lastHurtEntity != null) {
            vec3 = DefaultRandomPos.getPosAway(this.pigeonEntity, 8, 5, lastHurtEntity.position());
        } else {
            vec3 = DefaultRandomPos.getPos(this.pigeonEntity, 12, 4);
        }
        if (vec3 == null) {
            return false;
        } else {
            this.posX = vec3.x;
            this.posY = vec3.y;
            this.posZ = vec3.z;
            return true;
        }
    }

    protected BlockPos lookForWater(BlockGetter level, Entity entity, int range) {
        BlockPos blockpos = entity.blockPosition();
        return !level.getBlockState(blockpos).getCollisionShape(level, blockpos).isEmpty() ? null : BlockPos.findClosestMatch(entity.blockPosition(), range, 1, (p_196649_) -> level.getFluidState(p_196649_).is(FluidTags.WATER)).orElse(null);
    }

    protected boolean shouldPanic() {
        lastHurtEntity = this.pigeonEntity.getLastHurtByMob();
        return lastHurtEntity != null || this.pigeonEntity.isFreezing() || this.pigeonEntity.isOnFire();
    }
}
