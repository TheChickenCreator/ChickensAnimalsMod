package net.voidarkana.chickensanimalsmod.common.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.voidarkana.chickensanimalsmod.common.entity.ai.FlyingLookControl;
import net.voidarkana.chickensanimalsmod.common.entity.ai.GroundAndFlyingMoveControl;

import javax.annotation.Nullable;

public abstract class AbstractFlyingAnimal extends Animal {

    private static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(AbstractFlyingAnimal.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_LANDING = SynchedEntityData.defineId(AbstractFlyingAnimal.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> FLIGHT_TICKS = SynchedEntityData.defineId(AbstractFlyingAnimal.class, EntityDataSerializers.INT);
    public final int MAX_FLIGHT_TICKS = 12000;
    public BirdWanderGoal wanderGoal;
    public LandGoal landGoal;
    private boolean isLandNavigator;
    private int timeFlying = 0;

    protected AbstractFlyingAnimal(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new GroundAndFlyingMoveControl(this, 10, MAX_FLIGHT_TICKS);
        this.lookControl = new FlyingLookControl(this, 10);
        this.setMaxUpStep(1F);
        //this.lookControl = new FlyingLookControl(this, 10);
        switchNavigator(true);
    }

//    @Override
//    protected PathNavigation createNavigation(Level pLevel) {
//        FlyingPathNavigation nav = new FlyingPathNavigation(this, pLevel);
//        nav.setCanOpenDoors(false);
//        nav.setCanFloat(true);
//        nav.setCanPassDoors(true);
//        return nav;
//    }

    public void switchNavigator(boolean onLand) {
        if (onLand) {
            //this.moveControl = new MoveControl(this);
            this.navigation = new GroundPathNavigation(this, level());
            this.isLandNavigator = true;
        } else {
            //this.moveControl = new GroundAndFlyingMoveControl(this, 10, MAX_FLIGHT_TICKS);
            this.navigation = new FlyingPathNavigation(this, level());
            navigation.setCanFloat(true);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_FLYING, false);
        this.entityData.define(FLIGHT_TICKS, 0);
        this.entityData.define(IS_LANDING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("IsFlying", this.isFlying());
        pCompound.putInt("FlightTicks", this.getFlightTicks());
        pCompound.putBoolean("IsLanding", this.isLanding());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setFlying(pCompound.getBoolean("IsFlying"));
        this.setFlightTicks(pCompound.getInt("FlightTicks"));
        this.setLanding(pCompound.getBoolean("IsLanding"));
    }

    public boolean isFlying() {
        return this.entityData.get(IS_FLYING);
    }

    public void setFlying(boolean flying) {
        //switchNavigator(flying);
        this.entityData.set(IS_FLYING, flying);
    }

    public boolean isLanding() {
        return this.entityData.get(IS_LANDING);
    }

    public void setLanding(boolean landing) {
        this.entityData.set(IS_LANDING, landing);
    }

    public int getFlightTicks() {
        return this.entityData.get(FLIGHT_TICKS);
    }

    public void setFlightTicks(int flightTicks) {
        this.entityData.set(FLIGHT_TICKS, flightTicks);
    }

    public boolean wantsToFly() {
        return getFlightTicks() <= MAX_FLIGHT_TICKS;
    }

    public boolean canFly() {
        BlockPos pos = blockPosition();

        return !level().getBlockState(pos.offset(0, -1, 0)).isSolid();
    }

    @Override
    public void setNoGravity(boolean pNoGravity) {
        super.setNoGravity(isFlying() || isLanding());
    }

    @Override
    public void travel(Vec3 vec3d) {
        boolean flying = this.isFlying();
        float speed = (float) this.getAttributeValue(flying ? Attributes.FLYING_SPEED : Attributes.MOVEMENT_SPEED);
        if (flying && !isNoAi()) {
            this.moveRelative(speed, vec3d);
            this.move(MoverType.SELF, getDeltaMovement());
            double down = 0.0F;

            this.setDeltaMovement(getDeltaMovement().scale(0.91F).add(0.0F, down, 0.0F));
            this.calculateEntityAnimation(true);
        }
        else {
            super.travel(vec3d);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!isVehicle() && level().getBlockState(blockPosition().below()).isAir() && !isFlying() && !isLanding()) {
            if (landGoal != null) {
                landGoal.trigger();
            }
        }

        if (onGround() && this.isFlying()){
            this.setFlying(false);
            switchNavigator(true);
        }

        if (getFlightTicks() <= MAX_FLIGHT_TICKS && (isFlying() || isLanding()) && !isVehicle() && !isNoAi()) {
            int prevFlightTicks = getFlightTicks();
            setFlightTicks(prevFlightTicks + 2);
        }

        if (onGround() || getFlightTicks() >= MAX_FLIGHT_TICKS || isUnderWater()) {
            setFlying(false);
            switchNavigator(true);
        }

        if (onGround() && isLanding()) {
            setLanding(false);
        }

        if (getFlightTicks() > 0 && !isFlying() && !isVehicle()) {
            int prevFlightTicks = getFlightTicks();
            setFlightTicks(prevFlightTicks - 1);
        }

        double x = getDeltaMovement().x();
        double z = getDeltaMovement().z();

        boolean notMoving = Math.abs(x) < 0.1D && Math.abs(z) < 0.1D;

        if (wanderGoal != null && isFlying() && !isLanding() && wantsToFly() && !isVehicle() && notMoving) {
            wanderGoal.trigger();
        }
    }

    public class LandGoal extends WaterAvoidingRandomStrollGoal {
        private final AbstractFlyingAnimal mob;

        public LandGoal(AbstractFlyingAnimal goalOwner, double speedMod) {
            super(goalOwner, speedMod, 1);
            this.mob = goalOwner;
        }

        @Override
        public void start() {
            super.start();
            mob.setFlying(false);
            switchNavigator(true);
            mob.setLanding(true);
        }

        @Override
        public boolean canUse() {
            return mob.isFlying() && !mob.wantsToFly() && super.canUse();
        }

        public void trigger() {
            forceTrigger = true;
        }

        @Override
        public void tick() {
            super.tick();

            if (mob.isLanding() && mob.onGround()) {
                mob.setLanding(false);
            }

        }

        @Nullable
        @Override
        protected Vec3 getPosition() {
            return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 32, 24) : super.getPosition();
        }

        @Override
        public void stop() {
            mob.setLanding(false);
        }
    }

    public class HippogryphWanderGoal extends WaterAvoidingRandomStrollGoal {
        private final PigeonEntity mob;

        public HippogryphWanderGoal (PigeonEntity goalOwner, double speedMod, float probability) {
            super(goalOwner, speedMod, probability);
            this.mob = goalOwner;
        }

        @Override
        public void start() {
            super.start();

            if (!mob.isFlying() && mob.wantsToFly() && !mob.getPanic()) {
                mob.setDeltaMovement(mob.getDeltaMovement().add(0.0D, 0.25D, 0.0D));
                mob.setFlying(true);
                switchNavigator(false);
            }
        }

        @Override
        public boolean canUse() {
            return (forceTrigger || mob.wantsToFly()) && super.canUse();
        }

        @Override
        public void tick() {
            if (mob.wantsToFly() && mob.getNavigation().isDone()) {
                trigger();
            }
            if (mob.isInWater()) {
                mob.setFlying(true);
                switchNavigator(false);
                mob.setDeltaMovement(0.0D, 0.25D, 0.0D);
            }

            // speedModifier = mob.isFlying() && !mob.isControlledByLocalInstance() ? 10.0D : 1.0D;
        }

        public void trigger() {
            forceTrigger = true;
        }

        @Nullable
        @Override
        protected Vec3 getPosition() {
            Vec3 vec3 = this.mob.getViewVector(1.0F);
            int i = 8;
            Vec3 pos = findPos(this.mob, i, 4, vec3.x, vec3.z, ((float)Math.PI / 2F), 8, 6);
            return pos != null ? pos : AirAndWaterRandomPos.getPos(this.mob, i, 4, -2, vec3.x, vec3.z, ((float)Math.PI / 2F));
        }

        @Nullable
        public static Vec3 findPos(PathfinderMob pMob, int pRadius, int pYRange, double pX, double pZ, float pAmplifier, int pMaxSwimUp, int pMinSwimUp) {
            boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
            return RandomPos.generateRandomPos(pMob, () -> {
                BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pMob.getRandom(), pRadius, pYRange, 24, pX, pZ, pAmplifier);
                if (blockpos == null) {
                    return null;
                } else {
                    BlockPos blockpos1 = LandRandomPos.generateRandomPosTowardDirection(pMob, pRadius, flag, blockpos);
                    if (blockpos1 == null) {
                        return null;
                    } else {
                        blockpos1 = RandomPos.moveUpToAboveSolid(blockpos1, pMob.getRandom().nextInt(pMaxSwimUp - pMinSwimUp + 1) + pMinSwimUp, pMob.level().getMaxBuildHeight(), (p_148486_) -> GoalUtils.isSolid(pMob, p_148486_));
                        return !GoalUtils.isWater(pMob, blockpos1) && !GoalUtils.hasMalus(pMob, blockpos1) ? blockpos1 : null;
                    }
                }
            });
        }

        private boolean wantsToLand() {
            return mob.isFlying() && mob.getRandom().nextFloat() > 0.85F;
        }

        @Override
        public void stop() {
            if (wantsToLand()) {
                mob.landGoal.trigger();
            }
        }
    }

    public class BirdWanderGoal extends WaterAvoidingRandomStrollGoal {
        private final AbstractFlyingAnimal mob;

        public BirdWanderGoal(AbstractFlyingAnimal goalOwner, double speedMod, float probability) {
            super(goalOwner, speedMod, probability);
            this.mob = goalOwner;
        }

        @Override
        public void start() {
            super.start();

            if (!mob.isFlying() && mob.wantsToFly()) {
                mob.setDeltaMovement(mob.getDeltaMovement().add(0.0D, 0.25D, 0.0D));
                mob.setFlying(true);
                switchNavigator(false);
            }
        }

        @Override
        public boolean canUse() {
            return (forceTrigger || mob.wantsToFly()) && super.canUse();
        }

        @Override
        public void tick() {
            if (mob.wantsToFly() && mob.getNavigation().isDone()) {
                trigger();
            }
            if (mob.isInWater()) {
                mob.setFlying(true);
                switchNavigator(false);
                mob.setDeltaMovement(0.0D, 0.25D, 0.0D);
            }

        }

        public void trigger() {
            forceTrigger = true;
        }

        @Nullable
        @Override
        protected Vec3 getPosition() {
            Vec3 vec3 = this.mob.getViewVector(1.0F);
            int i = 8;
            Vec3 pos = findPos(this.mob, i, 4, vec3.x, vec3.z, ((float)Math.PI / 2F), 8, 6);
            return pos != null ? pos : AirAndWaterRandomPos.getPos(this.mob, i, 4, -2, vec3.x, vec3.z, ((float)Math.PI / 2F));
        }

        @Nullable
        public static Vec3 findPos(PathfinderMob pMob, int pRadius, int pYRange, double pX, double pZ, float pAmplifier, int pMaxSwimUp, int pMinSwimUp) {
            boolean flag = GoalUtils.mobRestricted(pMob, pRadius);
            return RandomPos.generateRandomPos(pMob, () -> {
                BlockPos blockpos = RandomPos.generateRandomDirectionWithinRadians(pMob.getRandom(), pRadius, pYRange, 24, pX, pZ, pAmplifier);
                if (blockpos == null) {
                    return null;
                } else {
                    BlockPos blockpos1 = LandRandomPos.generateRandomPosTowardDirection(pMob, pRadius, flag, blockpos);
                    if (blockpos1 == null) {
                        return null;
                    } else {
                        blockpos1 = RandomPos.moveUpToAboveSolid(blockpos1, pMob.getRandom().nextInt(pMaxSwimUp - pMinSwimUp + 1) + pMinSwimUp, pMob.level().getMaxBuildHeight(), (p_148486_) -> GoalUtils.isSolid(pMob, p_148486_));
                        return !GoalUtils.isWater(pMob, blockpos1) && !GoalUtils.hasMalus(pMob, blockpos1) ? blockpos1 : null;
                    }
                }
            });
        }

        private boolean wantsToLand() {
            return mob.isFlying() && mob.getRandom().nextFloat() > 0.85F;
        }

        @Override
        public void stop() {
            if (wantsToLand()) {
                mob.landGoal.trigger();
            }
        }
    }
}
