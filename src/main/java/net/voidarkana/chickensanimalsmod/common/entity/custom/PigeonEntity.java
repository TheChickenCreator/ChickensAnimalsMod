package net.voidarkana.chickensanimalsmod.common.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.voidarkana.chickensanimalsmod.common.entity.ModEntities;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class PigeonEntity extends AbstractFlyingAnimal implements FlyingAnimal, GeoEntity {

    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);

    protected static final RawAnimation PIGEON_IDLE = RawAnimation.begin().thenLoop("animation.pigeon.idle");
    protected static final RawAnimation PIGEON_WALK = RawAnimation.begin().thenLoop("animation.pigeon.walk");
    protected static final RawAnimation PIGEON_PANIC = RawAnimation.begin().thenLoop("animation.pigeon.run");
    protected static final RawAnimation PIGEON_FLY = RawAnimation.begin().thenLoop("animation.pigeon.fly");

    private static final EntityDataAccessor<Boolean> IS_FALLING = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_PANIC = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.INT);

    public PigeonEntity(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.10F);
    }
    BirdPanicGoal panic;
    @Override
    protected void registerGoals() {
        wanderGoal = new BirdWanderGoal(this, 1, 1){
            @Override
            public boolean canUse() {
                return super.canUse() && !PigeonEntity.this.getPanic();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !PigeonEntity.this.getPanic();
            }
        };
        landGoal = new LandGoal(this, 1);

        panic = new BirdPanicGoal(this, 2D);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(1, panic);
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.goalSelector.addGoal(6, wanderGoal);
        this.goalSelector.addGoal(2, landGoal);

        super.registerGoals();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
        this.entityData.define(IS_PANIC, false);
        this.entityData.define(IS_FALLING, false);
    }

    //variants
    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    //panic
    public Boolean getPanic() {
        return this.entityData.get(IS_PANIC);
    }

    public void setPanic(Boolean panic) {
        this.entityData.set(IS_PANIC, panic);
    }

    public boolean isFalling() {
        return this.entityData.get(IS_FALLING);
    }

    public void setisFalling(Boolean flying) {
        this.entityData.set(IS_FALLING, flying);
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant());
        compound.putBoolean("Panic", this.getPanic());
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(compound.getInt("Variant"));
        this.setPanic(compound.getBoolean("Panic"));
    }

    public void aiStep() {
        super.aiStep();
        //makes it fall slowly when it's falling
        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < (-0.1D)) {
            this.setDeltaMovement(vec3.multiply(1.0D, 0.6D, 1.0D));
        }
    }

    public boolean isFood(ItemStack pStack) {
        return FOOD_ITEMS.test(pStack);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getPanic()){
            switchNavigator(true);
        }
    }

//    @Override
//    public boolean hurt(DamageSource pSource, float pAmount) {
//        return super.hurt(pSource, pAmount);
//    }

    //    @Override
//    public void tick() {
//        super.tick();
//
//        if (!this.level().isClientSide) {
//            final boolean isFlying = isFlying();
//            if (isFlying && this.isLandNavigator) {
//                switchNavigator(false);
//            }
//            if (!isFlying && !this.isLandNavigator) {
//                switchNavigator(true);
//            }
//            if (isFlying) {
//                timeFlying++;
//                this.setNoGravity(true);
//                if (this.isInLove() || this.getPanic()) {
//                    this.setFlying(false);
//                }
//            } else {
//                timeFlying = 0;
//                this.setNoGravity(false);
//            }
//        }
//    }

    @Override
    @org.jetbrains.annotations.Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @org.jetbrains.annotations.Nullable SpawnGroupData spawnDataIn, @org.jetbrains.annotations.Nullable CompoundTag dataTag) {
        spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);

        this.setVariant(this.random.nextInt(4));
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        PigeonEntity baby = ModEntities.PIGEON.get().create(pLevel);
        if (baby != null){
            baby.setVariant(this.random.nextInt(4));
        }
        return baby;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if (pFallDistance > 1.0F) {
            this.playSound(SoundEvents.CHICKEN_STEP, 0F, 1.0F);
        }
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movementController", 5, this::Controller));
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    protected PlayState Controller(AnimationState<PigeonEntity> state) {
        PigeonEntity entity = state.getAnimatable();
        if (entity.onGround()) {
            if(state.isMoving()) return state.setAndContinue(entity.getPanic() ? PIGEON_PANIC : PIGEON_WALK);
        } else if (!entity.isInWater()) {
            return state.setAndContinue(PIGEON_FLY);
        }
        return state.setAndContinue(PIGEON_IDLE);
    }

    public class BirdPanicGoal extends Goal {
        public static final int WATER_CHECK_DISTANCE_VERTICAL = 1;
        protected final PigeonEntity mob;
        protected final double speedModifier;
        protected double posX;
        protected double posY;
        protected double posZ;
        protected boolean isRunning;

        public BirdPanicGoal(PigeonEntity pMob, double pSpeedModifier) {
            this.mob = pMob;
            this.speedModifier = pSpeedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            if (!this.shouldPanic()) {
                return false;
            } else {
                if (this.mob.isOnFire()) {
                    BlockPos blockpos = this.lookForWater(this.mob.level(), this.mob, 5);
                    if (blockpos != null) {
                        this.posX = (double)blockpos.getX();
                        this.posY = (double)blockpos.getY();
                        this.posZ = (double)blockpos.getZ();
                        return true;
                    }
                }

                return this.findRandomPosition();
            }
        }

        protected boolean shouldPanic() {
            return this.mob.getLastHurtByMob() != null || this.mob.isFreezing() || this.mob.isOnFire();
        }

        protected boolean findRandomPosition() {
            Vec3 vec3 = DefaultRandomPos.getPos(this.mob, 5, 4);
            if (vec3 == null) {
                return false;
            } else {
                this.posX = vec3.x;
                this.posY = vec3.y;
                this.posZ = vec3.z;
                return true;
            }
        }

        public boolean isRunning() {
            return this.isRunning;
        }

        public void start() {
            this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
            this.isRunning = true;
            if (this.mob.getPanic()){
                this.mob.setFlightTicks(0);
            }else{
                this.mob.setFlightTicks(400);
            }
            this.mob.setPanic(true);
        }

        public void stop() {
            this.isRunning = false;
            this.mob.setPanic(false);
        }

        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone();
        }

        @javax.annotation.Nullable
        protected BlockPos lookForWater(BlockGetter pLevel, Entity pEntity, int pRange) {
            BlockPos blockpos = pEntity.blockPosition();
            return !pLevel.getBlockState(blockpos).getCollisionShape(pLevel, blockpos).isEmpty() ? null : BlockPos.findClosestMatch(pEntity.blockPosition(), pRange, 1, (p_196649_) -> pLevel.getFluidState(p_196649_).is(FluidTags.WATER)).orElse(null);
        }
    }

    public class PigeonPanicGoal extends Goal {
        public static final int WATER_CHECK_DISTANCE_VERTICAL = 1;
        protected final PigeonEntity mob;
        protected final double speedModifier;
        protected double posX;
        protected double posY;
        protected double posZ;
        protected boolean isRunning;

        public PigeonPanicGoal(PigeonEntity pMob, double pSpeedModifier) {
            this.mob = pMob;
            this.speedModifier = pSpeedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            if (!this.shouldPanic()) {
                return false;
            } else {
                if (this.mob.isOnFire()) {
                    BlockPos blockpos = this.lookForWater(this.mob.level(), this.mob, 5);
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

        protected boolean shouldPanic() {
            return (this.mob.getLastHurtByMob() != null || this.mob.isFreezing() || this.mob.isOnFire()) && !this.mob.isFlying();
        }

        protected boolean findRandomPosition() {
            Vec3 vec3 = DefaultRandomPos.getPos(this.mob, 5, 4);
            if (vec3 == null) {
                return false;
            } else {
                this.posX = vec3.x;
                this.posY = vec3.y;
                this.posZ = vec3.z;
                return true;
            }
        }

        public boolean isRunning() {
            return this.isRunning;
        }

        public void start() {
            this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
            if (this.mob.getPanic()){
                this.mob.setFlightTicks(0);
            }else{
                this.mob.setFlightTicks(400);
            }
            this.mob.setPanic(true);
            this.isRunning = true;
        }

        public void stop() {
            this.mob.setPanic(false);
            this.isRunning = false;
        }

        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone();
        }

        @javax.annotation.Nullable
        protected BlockPos lookForWater(BlockGetter pLevel, Entity pEntity, int pRange) {
            BlockPos blockpos = pEntity.blockPosition();
            return !pLevel.getBlockState(blockpos).getCollisionShape(pLevel, blockpos).isEmpty() ? null : BlockPos.findClosestMatch(pEntity.blockPosition(), pRange, 1, (p_196649_) -> pLevel.getFluidState(p_196649_).is(FluidTags.WATER)).orElse(null);
        }
    }

    protected void playStepSound(BlockPos p_28301_, BlockState p_28302_) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

}
