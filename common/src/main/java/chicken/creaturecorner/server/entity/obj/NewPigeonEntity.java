package chicken.creaturecorner.server.entity.obj;

import chicken.creaturecorner.server.entity.CCEntities;
import chicken.creaturecorner.server.entity.obj.control.AnimalFlyingMoveControl;
import chicken.creaturecorner.server.entity.obj.control.AnimalMoveControl;
import chicken.creaturecorner.server.entity.obj.control.FlyingAnimalNavigation;
import chicken.creaturecorner.server.entity.obj.control.GroundAnimalNavigation;
import chicken.creaturecorner.server.entity.obj.base.GeoEntityBase;
import chicken.creaturecorner.server.sound.CCSounds;
import com.mojang.datafixers.DataFixUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class NewPigeonEntity extends GeoEntityBase {
    public NewPigeonEntity leader;
    private int schoolSize = 1;
    @Getter
    protected ArrayList<NewPigeonEntity> followers = new ArrayList<>();

    /*
    Data variables
     */
    private static final EntityDataAccessor<Integer> FLY_TICKS = SynchedEntityData.defineId(NewPigeonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(NewPigeonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PANIC = SynchedEntityData.defineId(NewPigeonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(NewPigeonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WANTS_TO_LAND = SynchedEntityData.defineId(NewPigeonEntity.class, EntityDataSerializers.BOOLEAN);

    /*
    Nav
     */
    private final FlyingAnimalNavigation flyingPathNavigation;
    private final GroundAnimalNavigation groundPathNavigation;

    /*
    Move Control
     */
    private final AnimalFlyingMoveControl flyingMoveControl;
    private final AnimalMoveControl animalMoveControl;


    /*
    Animations
     */
    protected static final RawAnimation PIGEON_IDLE = RawAnimation.begin().thenLoop("animation.pigeon.idle");
    protected static final RawAnimation PIGEON_WALK = RawAnimation.begin().thenLoop("animation.pigeon.walk");
    protected static final RawAnimation PIGEON_PANIC = RawAnimation.begin().thenLoop("animation.pigeon.run");
    protected static final RawAnimation PIGEON_FLY = RawAnimation.begin().thenLoop("animation.pigeon.fly");

    public NewPigeonEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.flyingPathNavigation = new FlyingAnimalNavigation(this, level);
        this.groundPathNavigation = new GroundAnimalNavigation(this, level);
        this.animalMoveControl = new AnimalMoveControl(this, 15);
        this.flyingMoveControl = new AnimalFlyingMoveControl(this, 15, false);
        this.moveControl = animalMoveControl;
        this.navigation = groundPathNavigation;
        this.lookControl = new SmoothSwimmingLookControl(this, 15);

        this.setPathfindingMalus(PathType.FENCE, -32F);
        this.setPathfindingMalus(PathType.TRAPDOOR, -32F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -32F);
        this.setPathfindingMalus(PathType.BLOCKED, -32F);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(4, new PigeonFlock(this));
        goalSelector.addGoal(4, new PigeonFly(this, 1, 32).setWanderRange(64, 16));
        goalSelector.addGoal(4, new PigeonRoam(this, 1, 60).setWanderRange(16, 6));
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        NewPigeonEntity entity = null;
        if(entity != null) {
            if(ageableMob instanceof NewPigeonEntity otherParent) {
                if (otherParent.getVariant() != this.getVariant()){
                    entity.setVariant(this.random.nextBoolean() ? this.getVariant() : otherParent.getVariant());
                }else if(otherParent.getVariant() == this.getVariant()) {
                    if (this.getVariant() == 0){
                        int random = this.random.nextInt(100);
                        if (random>20){
                            entity.setVariant(this.getVariant());
                        }else {
                            entity.setVariant(this.random.nextBoolean() ? 1 : 2);
                        }
                    }else {
                        entity.setVariant(this.getVariant());
                    }
                }
            }

            entity.setBaby(true);
        }
        return entity;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, SpawnGroupData spawnGroupData) {

        super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);

        if (spawnType != MobSpawnType.SPAWN_EGG) {
            if (spawnGroupData == null) {
                spawnGroupData = new SchoolSpawnGroupData(true, this);
            } else {
                this.startFollowing(((SchoolSpawnGroupData)spawnGroupData).leader);
            }
        }

        if (spawnType != MobSpawnType.STRUCTURE){

            if (!this.level().isClientSide()){
                ServerLevel serverLevel = (ServerLevel)this.level();
                if (serverLevel.isVillage(this.blockPosition())){
                    this.setVariant(this.random.nextInt(3));
                }else {
                    this.setVariant(0);
                }
            }

        }else{
            this.setVariant(this.random.nextInt(3));

            int extraBirds = this.getRandom().nextInt(1, 5);
            for (int i = 0; i < extraBirds; i++){
                PigeonEntity pigeon = CCEntities.PIGEON_TYPE.get().create(level.getLevel());
                if (pigeon != null) {
                    pigeon.moveTo(this.getX(), this.getY(), this.getZ(), random.nextInt(360), 0.0F);
                    pigeon.finalizeSpawn(level, level.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.NATURAL, spawnGroupData);
                    pigeon.setVariant(this.random.nextInt(3));
                    level.addFreshEntity(pigeon);
                }
            }
        }

        return spawnGroupData;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "moveFlyIdleController", 2, this::moveFlyController));
    }

    private PlayState moveFlyController(AnimationState<NewPigeonEntity> state) {
        NewPigeonEntity entity = state.getAnimatable();
        if (entity.onGround()) {
            if(state.isMoving()) return state.setAndContinue(entity.getPanic() ? PIGEON_PANIC : PIGEON_WALK);
        } else if (!entity.isInWater() && !entity.onGround()) {
            return state.setAndContinue(PIGEON_FLY);
        }
        return state.setAndContinue(PIGEON_IDLE);
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLY_TICKS, 0);
        builder.define(VARIANT, 0);
        builder.define(PANIC, false);
        builder.define(FLYING, false);
        builder.define(WANTS_TO_LAND, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("FlyTicks", getFlyTicks());
        compound.putInt("Variant", getVariant());
        compound.putBoolean("Panic", getPanic());
        compound.putBoolean("IsFlying", isFlying());
        compound.putBoolean("IsLanding", wantsToLand());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if(this.getDeltaMovement().y < -0.5 && !this.isFlying() && !isInLiquid()) {
            this.entityData.set(FLYING, true);
        }
        if(this.getDeltaMovement().y > -0.5 && !this.isFlying() && !isInLiquid()) {
            this.entityData.set(FLYING, true);
            this.entityData.set(WANTS_TO_LAND, true);
        }
        var navigator = getNavigation();
        Path path = navigator.getPath();
        if(!this.onGround() && isFlying() && navigation == flyingPathNavigation) {
            if (path != null) {
                if (!path.isDone()) {
                    BlockPos nextPos = path.getNextNodePos();
                    int y = nextPos.getY();
                    int difference = y - getBlockY();
                    if (y > getBlockY()) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, difference > 1 ? difference > 5 ? downWardsPush()*2 : downWardsPush() : downWardsPush()/2, 0.0));
                    } else if (y < getBlockY()) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, difference > -1 ? difference > 5 ? -(downWardsPush()*2) : -downWardsPush() : -(downWardsPush()/2), 0.0));
                    }
                    if (navigator.isStuck()) {
                        navigator.stop();
                    }
                    this.getLookControl().setLookAt(nextPos.getCenter());
                }
            } else {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -downWardsPush()/2, 0.0));
                this.setOnGroundWithMovement(this.verticalCollisionBelow, getDeltaMovement());
            }
        }

        if(!this.isFlying() && this.navigation == flyingPathNavigation) {
            this.entityData.set(FLYING, true);
        }

        if (this.isFlying() && !wantsToLand()) {
            if (this.getMoveControl() != flyingMoveControl) {
                this.moveControl = flyingMoveControl;
                this.entityData.set(FLYING, true);
            }
            if (this.getNavigation() != flyingPathNavigation) {
                this.navigation = flyingPathNavigation;
                this.entityData.set(FLYING, true);
            }
            if (this.random.nextFloat() < 0.005F && random.nextBoolean()) {
                this.entityData.set(WANTS_TO_LAND, true);
                if(hasFollowers() || leader != null) {
                    if(!hasFollowers() && leader != null) {
                        if(leader.hasFollowers()) {
                            for (NewPigeonEntity follower : leader.followers) {
                                follower.getEntityData().set(WANTS_TO_LAND, true);
                            }
                        }
                    } else if(leader == this && hasFollowers()) {
                        for (NewPigeonEntity follower : followers) {
                            follower.getEntityData().set(WANTS_TO_LAND, true);
                        }
                    }
                }
            }
        } else if (this.onGround()) {
            if (this.wantsToLand()) {
                this.entityData.set(WANTS_TO_LAND, false);
                this.entityData.set(FLYING, false);
            }
            if (this.getMoveControl() != animalMoveControl) {
                this.moveControl = animalMoveControl;
                this.setDeltaMovement(new Vec3(0,0,0));
                this.entityData.set(FLYING, false);
            }
            if (this.getNavigation() != groundPathNavigation) {
                this.navigation = groundPathNavigation;
                this.setDeltaMovement(new Vec3(0,0,0));
                this.entityData.set(FLYING, false);
                return;
            }
            if(this.leader != null) {
                if(leader instanceof NewPigeonEntity base) {
                    if(base.isFlying()) {
                        this.entityData.set(FLYING, true);
                    }

                    if(base.wantsToLand()) {
                        this.entityData.set(WANTS_TO_LAND, true);
                    }
                }
            }
            if (this.random.nextFloat() < 0.005F && random.nextBoolean()) {
                if(hasFollowers() || leader == null) {
                    this.entityData.set(FLYING, true);
                    for (NewPigeonEntity follower : followers) {
                        follower.getEntityData().set(FLYING, true);
                    }
                }

                return;
            }
        }
    }

    @Override
    public void jumpFromGround() {
        if(isFlying() && navigation == flyingPathNavigation) return;
        super.jumpFromGround();
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> fluidTag) {
        if(isFlying() && navigation == flyingPathNavigation) return;
        super.jumpInLiquid(fluidTag);
    }

    @Override
    public void setJumping(boolean jumping) {
        if(isFlying() && navigation == flyingPathNavigation) {
            super.setJumping(false);
            return;
        }
        super.setJumping(jumping);
    }

    @Override
    public float getJumpBoostPower() {
        if(isFlying() && navigation == flyingPathNavigation) return 0.0F;
        return super.getJumpBoostPower();
    }

    @Override
    protected float getJumpPower(float multiplier) {
        if(isFlying() && navigation == flyingPathNavigation) return 0.0F;
        return super.getJumpPower(multiplier);
    }

    @Override
    protected float getJumpPower() {
        if(isFlying() && navigation == flyingPathNavigation) return 0.0F;
        return super.getJumpPower();
    }

    @Override
    protected float getBlockJumpFactor() {
        if(isFlying() && navigation == flyingPathNavigation) return 0.0F;
        return super.getBlockJumpFactor();
    }

    public double downWardsPush() {
        return 0.0055;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setFlyTicks(compound.getInt("FlyTicks"));
        this.setVariant(compound.getInt("Variant"));
        this.setPanic(compound.getBoolean("Panic"));
        this.setFlying(compound.getBoolean("IsFlying"));
        this.entityData.set(WANTS_TO_LAND, compound.getBoolean("IsLanding"));
    }

    public boolean wantsToLand() {
        return this.entityData.get(WANTS_TO_LAND);
    }

    public int getFlyTicks() {
        return this.entityData.get(FLY_TICKS);
    }

    public void setFlyTicks(int ticks) {
        this.entityData.set(FLY_TICKS, ticks);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public boolean getPanic() {
        return this.entityData.get(PANIC);
    }

    public void setPanic(boolean panic) {
        this.setFlying(false);
        this.entityData.set(PANIC, panic);
    }

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    public void setFlying(boolean flight) {
        this.entityData.set(FLYING, flight);
    }



    @Override
    public String getVariantName() {

        return switch (getVariant()) {
            case 1 -> "white";
            case 2 -> "red";
            default -> "grey";
        };
    }

    public void startFollowing(NewPigeonEntity leader) {
        if (!this.hasFollowers()){
            this.leader = leader;
            leader.addFollower(this);
        }
    }

    public void stopFollowing() {
        assert this.leader != null;
        this.leader.removeFollower(this);
        this.leader = null;
    }

    private void addFollower(NewPigeonEntity entity) {
        this.followers.add(entity);
        ++this.schoolSize;
    }

    public void addFollowers(Stream<? extends NewPigeonEntity> followers) {
        followers.limit(this.getMaxSchoolSize() - this.followers.size())
                .filter(p_27538_ -> p_27538_ != this)
                .forEach(p_27536_ -> p_27536_.startFollowing(this));
    }


    private void removeFollower(NewPigeonEntity entity) {
        this.followers.remove(entity);
        --this.schoolSize;
    }

    public boolean canBeFollowed() {
        return !this.isFollower() && this.hasFollowers() && this.schoolSize < this.getMaxSchoolSize() && !this.isBaby();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.FLYING_SPEED, 0.1F)
                .add(Attributes.MOVEMENT_SPEED, 0.19F).add(Attributes.MAX_HEALTH, 5);
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, @NotNull BlockState pState, @NotNull BlockPos pPos) {}

    @Override
    public boolean onClimbable() {
        return super.onClimbable() && !isFlying();
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if(isFlying() && navigation == flyingPathNavigation) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
            } else if (this.isInLava()) {
                this.moveRelative(0.02F, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            } else {
                float speed = getSpeed();
                this.moveRelative(speed, travelVector);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.91F, 0.91F, 0.91F));
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public float getSpeed() {
        return isFlying() ? (float) getAttributes().getBaseValue(Attributes.FLYING_SPEED) : super.getSpeed();
    }

    @Override
    public int getMaxHeadYRot() {
        return 9;
    }

    @Override
    public int getMaxHeadXRot() {
        return 9;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && this.isDeadOrDying() && this.isFollower()){
            if (this.leader != null){
                this.leader.removeFollower(this);
            }
        }
        super.remove(reason);
    }

    public int getMaxSchoolSize() {
        return 10;
    }

    public boolean hasFollowers() {
        return this.schoolSize > 1;
    }

    public boolean inRangeOfLeader() {
        return this.distanceToSqr(this.leader) <= 200.0;
    }

    public boolean isTooCloseToLeader() {
        return this.distanceToSqr(this.leader) <= 3;
    }

    public boolean shouldMoveToLeader() {
        if (this.leader == null){
            return false;
        }else {
            if (!this.inRangeOfLeader()) return false;
            return !this.isTooCloseToLeader();
        }
    }

    public void pathToLeader() {
        if (this.isFollower()) {
            this.getNavigation().moveTo(this.leader, 1.0);
        }
    }

    public boolean leaderPathValid() {
        if(getNavigation().getPath() == null) return this.distanceToSqr(this.leader) <= 200;
        return this.leader.distanceToSqr(getNavigation().getPath().getTarget().getCenter()) < 200;
    }

    public boolean isFollower() {
        return this.leader != null && this.leader.isAlive();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CCSounds.PIGEON_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return CCSounds.PIGEON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CCSounds.PIGEON_DEATH.get();
    }

    protected boolean isFlapping() {
        return !this.onGround() && !this.isInWaterOrBubble() && this.getRandom().nextInt(10) == 0 ;
    }

    protected void onFlap() {
        this.playSound(CCSounds.PIGEON_FLAP.get(), 0.15F, 1.0F);
    }

    public static class SchoolSpawnGroupData extends AgeableMobGroupData {
        public final NewPigeonEntity leader;
        public SchoolSpawnGroupData(boolean shouldSpawnBaby, NewPigeonEntity pigeonEntity) {
            super(shouldSpawnBaby);
            this.leader = pigeonEntity;
        }
    }

    private static class PigeonFly extends Goal {
        public static final int DEFAULT_INTERVAL = 120;
        protected double wantedX;
        protected double wantedY;
        protected double wantedZ;
        protected final double speedModifier;
        /**
         * -- SETTER --
         *  Changes task random possibility for execution
         */
        @Setter
        protected int interval;
        private final NewPigeonEntity mob;
        private int wanderXz;
        private int wanderY;

        public PigeonFly(NewPigeonEntity mob, double speedModifier) {
            this(mob, speedModifier, 120);
        }

        public PigeonFly(NewPigeonEntity mob, double speedModifier, int interval) {
            this(mob, speedModifier, interval, true);
        }

        public PigeonFly(NewPigeonEntity mob, double speedModifier, int interval, boolean checkNoActionTime) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.interval = interval;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if(!this.mob.getNavigation().isDone()) {
                return false;
            }
            if (!this.mob.hasControllingPassenger()) {
                Vec3 vec3 = this.getPosition();
                if (vec3 != null) {
                    if (mob.wantsToLand() || checkIfDistanceToGround(16, vec3)) {
                        this.wantedX = vec3.x;
                        this.wantedY = vec3.y;
                        this.wantedZ = vec3.z;
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean checkIfDistanceToGround(int distance, Vec3 vec3) {
            Level level = mob.level();
            BlockHitResult result = level.clip(new ClipContext(vec3, vec3.add(0, -distance, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, mob));
            if(!result.isInside()) {
                return true;
            }

            BlockPos.MutableBlockPos mutableBlockPos = mob.blockPosition().mutable();
            int curBlock = 0;
            boolean found = false;
            while (!found && curBlock < 100) {
                curBlock++;
                BlockPos pos = mutableBlockPos.above(curBlock);
                BlockState state = level.getBlockState(pos);
                if(state.isAir() || state.isSolidRender(level, pos)) {
                    found = true;
                }
            }

            return false;
        }

        protected Vec3 getPosition() {
            if(mob.wantsToLand()) return LandRandomPos.getPos(this.mob, wanderXz, wanderXz);
            boolean bool = mob.getRandom().nextBoolean();
            int value = bool ? -1 : 1;
            return AirAndWaterRandomPos.getPos(this.mob, wanderXz, wanderY, value,value,value, 2);
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, 0, this.speedModifier);
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
            super.stop();
        }

        public PigeonFly setWanderRange(int i, int i1) {
            this.wanderXz = i;
            this.wanderY = i1;
            return this;
        }
    }

    private static class PigeonRoam extends Goal {
        protected final NewPigeonEntity mob;
        protected double wantedX;
        protected double wantedY;
        protected double wantedZ;
        protected final double speedModifier;
        protected int interval;
        protected boolean forceTrigger;
        private final boolean checkNoActionTime;

        public PigeonRoam(NewPigeonEntity mob, double speedModifier) {
            this(mob, speedModifier, 120);
        }

        public PigeonRoam(NewPigeonEntity mob, double speedModifier, int interval) {
            this(mob, speedModifier, interval, true);
        }

        public PigeonRoam(NewPigeonEntity mob, double speedModifier, int interval, boolean checkNoActionTime) {
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.interval = interval;
            this.checkNoActionTime = checkNoActionTime;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        protected int adjustedTickDelay(int adjustment) {
            return super.adjustedTickDelay(adjustment);
        }

        private int xRange = 6;
        private int yRange = 6;
        public PigeonRoam setWanderRange(int x, int y) {
            this.xRange = x;
            this.yRange = y;
            return this;
        }

        @Override
        public boolean canUse() {
            if (this.mob.hasControllingPassenger()) {
                return false;
            } else {
                if (!this.forceTrigger) {
                    if (this.checkNoActionTime && this.mob.getNoActionTime() >= 15) {
                        return false;
                    }

                    if (this.mob.getRandom().nextInt(reducedTickDelay(this.interval)) != 0) {
                        return false;
                    }
                }

                Vec3 vec3 = this.getPosition();
                if (vec3 == null) {
                    return false;
                } else {
                    this.wantedX = vec3.x;
                    this.wantedY = vec3.y;
                    this.wantedZ = vec3.z;
                    this.forceTrigger = false;
                    return true;
                }
            }
        }
        
        protected Vec3 getPosition() {
            return LandRandomPos.getPos(this.mob, xRange, yRange);
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone() && !this.mob.hasControllingPassenger();
        }

        @Override
        public void start() {
            this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, 0, this.speedModifier);
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
            super.stop();
        }

        public void trigger() {
            this.forceTrigger = true;
        }
    }

    private static class PigeonFlock extends Goal {
        private final NewPigeonEntity mob;
        private int timeToRecalcPath;
        private int nextStartTick = 20;
        private float searchRadius = 32.0F;

        public PigeonFlock(NewPigeonEntity mob) {
            this.mob = mob;
        }

        public PigeonFlock setRadius(float radius) {
            searchRadius = radius;
            return this;
        }

        protected int nextStartTick() {
            return 20;
        }

        @Override
        public boolean canUse() {
            if (this.mob.hasFollowers()) {
                return false;
            } else if (this.mob.isFollower()) {
                return true;
            } else if (this.nextStartTick > 0) {
                this.nextStartTick--;
                return false;
            } else {
                this.nextStartTick = this.nextStartTick();
                Predicate<NewPigeonEntity> predicate = p_25258_ -> p_25258_.canBeFollowed() || !p_25258_.isFollower();
                List<? extends NewPigeonEntity> list = this.mob
                        .level()
                        .getEntitiesOfClass((Class<? extends NewPigeonEntity>)this.mob.getClass(), this.mob.getBoundingBox().inflate(searchRadius, searchRadius, searchRadius), predicate);
                NewPigeonEntity abstractschoolingfish = DataFixUtils.orElse(list.stream().filter(NewPigeonEntity::canBeFollowed).findAny(), this.mob);
                abstractschoolingfish.addFollowers(list.stream().filter(p_25255_ -> !p_25255_.isFollower()));
                return this.mob.isFollower();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.mob.isFollower() && this.mob.inRangeOfLeader();
        }

        @Override
        public void start() {
            this.mob.pathToLeader();
        }

        @Override
        public void stop() {
            this.mob.stopFollowing();
        }

        @Override
        public void tick() {
            if (!this.mob.leaderPathValid()) {
                this.mob.pathToLeader();
            }
        }
    }
}
