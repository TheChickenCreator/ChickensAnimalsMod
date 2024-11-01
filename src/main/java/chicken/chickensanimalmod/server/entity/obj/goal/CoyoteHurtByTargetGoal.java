package chicken.chickensanimalmod.server.entity.obj.goal;

import chicken.chickensanimalmod.server.entity.obj.CoyoteEntity;
import chicken.chickensanimalmod.server.entity.obj.geo.GeoTamableEntity;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Team;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class CoyoteHurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private static final int ALERT_RANGE_Y = 10;
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    @Nullable
    private Class<?>[] toIgnoreAlert;
    private int unseenTicks;

    public CoyoteHurtByTargetGoal(PathfinderMob mob, Class<?>... toIgnoreDamage) {
        super(mob, true);
        this.toIgnoreDamage = toIgnoreDamage;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp();
        LivingEntity livingentity = this.mob.getLastHurtByMob();
        if (i != this.timestamp && livingentity != null) {
            if (livingentity.getType() == EntityType.PLAYER && this.mob.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                return false;
            } else {
                Class[] var3 = this.toIgnoreDamage;
                for (Class<?> oclass : var3) {
                    if (oclass.isAssignableFrom(livingentity.getClass())) {
                        return false;
                    }
                }

                return this.canAttack(livingentity, HURT_BY_TARGETING);
            }
        } else {
            return false;
        }
    }

    protected boolean canAttack(@Nullable LivingEntity potentialTarget, TargetingConditions targetPredicate) {
        if (potentialTarget == null) {
            return false;
        } else if(this.mob instanceof GeoTamableEntity entity) {
           return entity.isOwnedBy(potentialTarget);
        } else if (!targetPredicate.test(this.mob, potentialTarget)) {
            return false;
        } else return this.mob.isWithinRestriction(potentialTarget.blockPosition());
    }

    public boolean canContinueToUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity == null) {
            livingentity = this.targetMob;
        }

        if (livingentity == null) {
            return false;
        } else if (!this.mob.canAttack(livingentity)) {
            return false;
        } else {
            if(this.mob instanceof CoyoteEntity coyoteEntity) {
                if(((CoyoteEntity) this.mob).isOwnedBy(livingentity)) {
                    coyoteEntity.setShouldAttackOnce(true);
                    return !coyoteEntity.isAttackedOnce();
                } else {
                    return true;
                }
            } else {
                double d0 = this.getFollowDistance();
                if (this.mob.distanceToSqr(livingentity) > d0 * d0) {
                    return false;
                } else {
                    if (this.mustSee) {
                        if (this.mob.getSensing().hasLineOfSight(livingentity)) {
                            unseenTicks = 0;
                        } else if (++this.unseenTicks > reducedTickDelay(this.unseenMemoryTicks)) {
                            return false;
                        }
                    }

                    this.mob.setTarget(livingentity);
                    return true;
                }
            }
        }
    }

    public CoyoteHurtByTargetGoal setAlertOthers(Class<?>... reinforcementTypes) {
        this.alertSameType = true;
        this.toIgnoreAlert = reinforcementTypes;
        return this;
    }

    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            this.alertOthers();
        }
        if(this.mob instanceof CoyoteEntity entity) {
            entity.checkOwnerHurt();
            entity.setOrderedToSit(false);
        }

        super.start();
        this.unseenTicks = 0;
    }

    protected void alertOthers() {
        double d0 = this.getFollowDistance();
        AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, 10.0, d0);
        List<? extends Mob> list = this.mob.level().getEntitiesOfClass(this.mob.getClass(), aabb, EntitySelector.NO_SPECTATORS);
        Iterator<? extends Mob> iterator = list.iterator();

        while (true) {
            Mob mob;
            boolean flag;
            do {
                do {
                    do {
                        do {
                            do {
                                if (!iterator.hasNext()) {
                                    return;
                                }

                                mob = iterator.next();
                            } while (this.mob == mob);
                        } while (mob.getTarget() != null);
                    } while (this.mob instanceof TamableAnimal && ((TamableAnimal) this.mob).getOwner() != ((TamableAnimal) mob).getOwner());
                } while (mob.isAlliedTo(Objects.requireNonNull(this.mob.getLastHurtByMob())));

                if (this.toIgnoreAlert == null) {
                    break;
                }

                flag = false;
                Class[] var8 = this.toIgnoreAlert;

                for (Class<?> oclass : var8) {
                    if (mob.getClass() == oclass) {
                        flag = true;
                        break;
                    }
                }
            } while (flag);

            this.alertOther(mob, this.mob.getLastHurtByMob());
        }
    }

    protected void alertOther(Mob mob, LivingEntity target) {
        mob.setTarget(target);
    }
}