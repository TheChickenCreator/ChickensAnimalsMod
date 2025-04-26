package chicken.creaturecorner.server.entity.obj;

import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.entity.obj.geo.GeoEntityBase;
import chicken.creaturecorner.server.entity.obj.geo.goal.LookForFoodGoal;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;

import java.util.*;
import java.util.function.Predicate;

//todo: make caracaras not touch the floor while moving and flying, if their navigation is done then don't make them not touch the floor.

public class CaracaraEntity extends GeoEntityBase implements NeutralMob {
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    @javax.annotation.Nullable
    private UUID persistentAngerTarget;
    public boolean wantsToFly;
    private LookForFoodGoal forFoodGoal;
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME;
    private static final EntityDataAccessor<Integer> FLY_TICKS;
    private static final EntityDataAccessor<Boolean> FLYING;
    private static final EntityDataAccessor<Boolean> DIVING;
    public AttackPhase attackPhase;
    public float currentRoll;
    public float currentPitch;
    public static final float STARTING_ANGLE = 0.015F;

    public CaracaraEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.attackPhase = CaracaraEntity.AttackPhase.CIRCLE;
        this.currentRoll = 0.0F;
        this.currentPitch = 0.0F;
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
        this.moveControl = new CaracaraMoveControl(this, 1.0F, false);
        this.navigation = new CaracaraPathNavigation(this, this.level());
        this.lookControl = new SmoothSwimmingLookControl(this, 2);
        this.wantsToFly = false;
    }

    public boolean canFly() {
        return this.wantsToFly && !this.isBaby();
    }

    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        if (!(entity instanceof Player)) {
            this.killed(entity);
        }

        return super.killedEntity(level, entity);
    }

    public void killed(LivingEntity entity) {
        int food = this.getFoodLevel();
        if (!(entity instanceof Sheep) && !(entity instanceof Pig)) {
            this.setFoodLevel(Math.min(60, food + 20));
        } else {
            this.setFoodLevel(Math.min(60, food + 50));
        }

    }

    public int maxFood() {
        return 60;
    }

    protected void registerGoals() {
        this.forFoodGoal = new LookForFoodGoal(this, ItemTags.MEAT);
        this.goalSelector.addGoal(3, this.forFoodGoal);
        this.goalSelector.addGoal(2, new CaracaraFlyMelee(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0F, true) {
            public boolean canUse() {
                return !CaracaraEntity.this.canFly() && super.canUse();
            }

            public boolean canContinueToUse() {
                return !CaracaraEntity.this.canFly() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(2, new CaracaraStalkPrey(this, (double)1.0F));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new CaracaraStrollGoal(this, (double)0.5F));
        this.goalSelector.addGoal(7, new AIFlyIdle());
        this.goalSelector.addGoal(4, new BreedGoal(this, (double)1.0F, CaracaraEntity.class));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PigeonEntity.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Rabbit.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Pig.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Sheep.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
    }

    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLY_TICKS, 0);
        builder.define(FLYING, false);
        builder.define(DIVING, false);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("FlyTicks", this.getFlyTicks());
        compound.putBoolean("IsFlying", this.isFlying());
    }

    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setFlyTicks(compound.getInt("FlyTicks"));
        this.setFlying(compound.getBoolean("IsFlying"));
    }

    public int getFlyTicks() {
        return (Integer)this.entityData.get(FLY_TICKS);
    }

    public void setFlyTicks(int ticks) {
        this.entityData.set(FLY_TICKS, ticks);
    }

    public void setFlying(boolean flight) {
        this.entityData.set(FLYING, flight);
    }

    public boolean isFlying() {
        return (Boolean)this.entityData.get(FLYING);
    }

    public void setDiving(boolean flight) {
        this.entityData.set(DIVING, flight);
    }

    public boolean isDiving() {
        return (Boolean)this.entityData.get(DIVING);
    }

    public boolean canPickUpLoot() {
        return this.isHungry();
    }

    public boolean isHungry() {
        return super.isHungry();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.STEP_HEIGHT, 1.0F)
                .add(Attributes.FLYING_SPEED, 1.0F)
                .add(Attributes.ATTACK_DAMAGE, 4.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.35F)
                .add(Attributes.FOLLOW_RANGE, 64.0F);
    }

    protected void checkFallDamage(double pY, boolean pOnGround, @NotNull BlockState pState, @NotNull BlockPos pPos) {
        if (this.isBaby()) {
            super.checkFallDamage(pY, pOnGround, pState, pPos);
        }

    }

    public int getMaxHeadYRot() {
        return 9;
    }

    public int getMaxHeadXRot() {
        return 9;
    }

    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        if (item.is(ItemTags.MEAT)) {
            ItemStack itemstack = itemEntity.getItem();
            ItemStack itemstack1 = this.equipItemIfPossible(itemstack.copy());
            if (!itemstack1.isEmpty()) {
                this.onItemPickup(itemEntity);
                this.take(itemEntity, 1);
                itemstack.shrink(1);
                if (itemstack.isEmpty()) {
                    itemEntity.discard();
                }
            }
        }

    }

    private void triggerFoodSearch() {
        if (this.forFoodGoal != null) {
            this.forFoodGoal.trigger();
        } else {
            this.navigation.stop();
            Predicate<ItemEntity> predicate = (p_25258_) -> p_25258_.getItem().is(ItemTags.MEAT);
            List<? extends ItemEntity> list = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate((double)32.0F, (double)8.0F, (double)32.0F), predicate);
            if (!list.isEmpty()) {
                this.navigation.moveTo((Entity)list.getFirst(), 1.1);
            }
        }

    }

    public void aiStep() {
        ItemStack stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (stack != null && stack.is(ItemTags.MEAT) && this.isHungry()) {
            this.setFoodLevel(this.getFoodLevel() + 50);
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), this.getX(), this.getY(), this.getZ(), (double)0.0F, (double)0.0F, (double)0.0F);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EAT, SoundSource.AMBIENT);
            stack.setCount(0);
            this.setItemInHand(InteractionHand.MAIN_HAND, Items.AIR.getDefaultInstance());
        }

        if (this.isAlmostStarving() && (double)this.random.nextFloat() <= 0.2) {
            this.triggerFoodSearch();
        }

        super.aiStep();

        if (this.isFlying() && !this.onGround()) {
            float prevPitch = this.currentPitch;
            float targetPitch = (float)Math.max((double)-0.75F, Math.min((double)0.75F, (this.getY() - this.yOld) * (double)10.0F));
            targetPitch = -targetPitch;
            this.currentPitch = prevPitch + (targetPitch - prevPitch) * 0.05F;

            float prevRoll = this.currentRoll;
            float targetRoll = Math.max(-0.45F, Math.min(0.45F, (this.getYRot() - this.yRotO) * 0.1F));
            targetRoll = -targetRoll;
            this.currentRoll = prevRoll + (targetRoll - prevRoll) * 0.05F;
        } else {
            this.currentPitch = 0.0F;

            this.currentRoll = 0.0F;
        }

    }

    public void tick() {
        super.tick();
        int prevFlyTicks = this.getFlyTicks();
        if (!this.level().isClientSide) {
            if (this.isDiving() && (this.getTarget() == null || this.attackPhase == CaracaraEntity.AttackPhase.CIRCLE || this.isBaby())) {
                this.setDiving(false);
            }

            if (this.isFlying() && this.canFly()) {
                this.setNoGravity(true);
                if (this.isPassenger() || this.isInLove()) {
                    this.setFlying(false);
                }
            } else {
                this.setNoGravity(false);
            }
        }

        if (!this.canFly() && this.onGround()) {
            this.setFlying(false);
        }

        if (this.getFlyTicks() <= 0) {
            this.wantsToFly = false;
        } else if (this.getFlyTicks() >= 1000) {
            this.wantsToFly = true;
        }

        if (!this.isAggressive()) {
            if (this.canFly() && this.isFlying()) {
                this.setFlyTicks(prevFlyTicks - 1);
            } else if (!this.canFly() && this.onGround()) {
                this.setFlyTicks(prevFlyTicks + 3);
            }
        }

        LivingEntity var3 = this.getTarget();
        if (var3 instanceof PigeonEntity pigeon) {
            if (this.canFly() && pigeon.isFlying() && this.random.nextInt(10) == 0) {
                this.setFlyTicks(500);
                this.setFlying(true);
            }
        }

        if (!this.onGround() && !this.isFlying() && this.getDeltaMovement().y < (double)0.0F && !this.isBaby()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply((double)1.0F, (double)0.5F, (double)1.0F));
        }

    }

    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
    }

    public @org.jetbrains.annotations.Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        CaracaraEntity entity = (CaracaraEntity)AnimalModEntities.CARACARA_TYPE.create(serverLevel);
        if (entity != null) {
            entity.setBaby(true);
        }

        return entity;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController(this, "moveFlyIdleController", 3, this::moveFlyController));
    }

    private PlayState moveFlyController(AnimationState<CaracaraEntity> state) {
        CaracaraEntity entity = (CaracaraEntity)state.getAnimatable();
        if (!entity.isInWater() && !entity.onGround()) {
            return entity.isDiving() ? state.setAndContinue(RawAnimation.begin().thenLoop("dive")) : state.setAndContinue(RawAnimation.begin().thenLoop("fly"));
        } else if (entity.onGround() && entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4) {
            return entity.isAggressive() ? state.setAndContinue(RawAnimation.begin().thenLoop("run")) : state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
        } else {
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }
    }

    public static boolean spawnRules(EntityType<CaracaraEntity> pigeonEntityEntityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos pos, RandomSource randomSource) {
        boolean canContinue = serverLevelAccessor.isEmptyBlock(pos);
        if (canContinue) {
            Optional<ResourceKey<Biome>> biomeKey = serverLevelAccessor.getBiome(pos).unwrapKey();
            if (biomeKey.isPresent()) {
                ResourceKey<Biome> resourceKey = (ResourceKey)biomeKey.get();
                if (resourceKey != Biomes.END_BARRENS && resourceKey != Biomes.END_HIGHLANDS && resourceKey != Biomes.THE_END && resourceKey != Biomes.SMALL_END_ISLANDS) {
                    if (serverLevelAccessor.getBiomeManager().getBiome(pos).is(BiomeTags.IS_MOUNTAIN)) {
                        return true;
                    }

                    return serverLevelAccessor.getLevel().isVillage(pos);
                }

                StructureManager manager = serverLevelAccessor.getLevel().structureManager();
                if (manager.hasAnyStructureAt(pos)) {
                    Map<Structure, LongSet> structures = manager.getAllStructuresAt(pos);

                    for(Structure structure : structures.keySet()) {
                        if (structure.type() == StructureType.END_CITY) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        return false;
    }

    public int getRemainingPersistentAngerTime() {
        return (Integer)this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }

    public @org.jetbrains.annotations.Nullable UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void setPersistentAngerTarget(@org.jetbrains.annotations.Nullable UUID uuid) {
        this.persistentAngerTarget = uuid;
    }

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target) && (this.getLastHurtByMob() != null && this.getLastHurtByMob() == target || this.isHungry() && (!this.isBaby() || this.isBaby() && (target instanceof PigeonEntity || target instanceof Rabbit || target instanceof Chicken)));
    }


    public BlockPos getBirdGround(BlockPos in) {
        BlockPos position;
        for(position = new BlockPos(in.getX(), (int)this.getY(), in.getZ()); position.getY() < 320 && !this.level().getFluidState(position).isEmpty(); position = position.above()) {
        }

        while(position.getY() > -64 && !this.level().getBlockState(position).isSolid() && this.level().getFluidState(position).isEmpty()) {
            position = position.below();
        }

        return position;
    }

    public Vec3 getBlockGrounding(Vec3 fleePos) {
        float radius = (float)(10 + this.getRandom().nextInt(15));
        float neg = this.getRandom().nextBoolean() ? 1.0F : -1.0F;
        float renderYawOffset = this.yBodyRot;
        float angle = 0.015F * renderYawOffset + 3.15F + this.getRandom().nextFloat() * neg;
        double extraX = (double)(radius * Mth.sin((float)Math.PI + angle));
        double extraZ = (double)(radius * Mth.cos(angle));
        BlockPos radialPos = new BlockPos((int)(fleePos.x() + extraX), (int)this.getY(), (int)(fleePos.z() + extraZ));
        BlockPos ground = this.getBirdGround(radialPos);
        if (ground.getY() < -64) {
            return null;
        } else {
            for(ground = this.blockPosition(); ground.getY() > -64 && !this.level().getBlockState(ground).isSolid(); ground = ground.below()) {
            }

            return !this.isTargetBlocked(Vec3.atCenterOf(ground.above())) ? Vec3.atCenterOf(ground.below()) : null;
        }
    }

    public Vec3 getBlockInViewAway(Vec3 fleePos, float radiusAdd) {
        float radius = 5.0F + radiusAdd + (float)this.getRandom().nextInt(5);
        float neg = this.getRandom().nextBoolean() ? 1.0F : -1.0F;
        float renderYawOffset = this.yBodyRot;
        float angle = 0.015F * renderYawOffset + 3.15F + this.getRandom().nextFloat() * neg;
        double extraX = (double)(radius * Mth.sin((float)Math.PI + angle));
        double extraZ = (double)(radius * Mth.cos(angle));
        BlockPos radialPos = new BlockPos((int)(fleePos.x() + extraX), 0, (int)(fleePos.z() + extraZ));
        BlockPos ground = this.getBirdGround(radialPos);
        int distFromGround = (int)this.getY() - ground.getY();
        int flightHeight = 5 + this.getRandom().nextInt(5);
        int j = this.getRandom().nextInt(5) + 5;
        BlockPos newPos = ground.above(distFromGround > 5 ? flightHeight : j);
        if (this.level().getBlockState(ground).is(BlockTags.LEAVES)) {
            newPos = ground.above(1 + this.getRandom().nextInt(3));
        }

        return !this.isTargetBlocked(Vec3.atCenterOf(newPos)) && this.distanceToSqr(Vec3.atCenterOf(newPos)) > (double)1.0F ? Vec3.atCenterOf(newPos) : null;
    }

    public boolean isTargetBlocked(Vec3 target) {
        Vec3 Vector3d = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        return this.level().clip(new ClipContext(Vector3d, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS;
    }

    private boolean isOverWaterOrVoid() {
        BlockPos position;
        for(position = this.blockPosition(); position.getY() > -65 && this.level().isEmptyBlock(position); position = position.below()) {
        }

        return !this.level().getFluidState(position).isEmpty() || this.level().getBlockState(position).is(Blocks.VINE) || position.getY() <= -65;
    }

    static {
        DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.INT);
        FLY_TICKS = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.INT);
        FLYING = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.BOOLEAN);
        DIVING = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.BOOLEAN);
    }

    public class CaracaraPathNavigation extends GroundPathNavigation {
        private final CaracaraEntity mob;
        private float yMobOffset;

        public CaracaraPathNavigation(CaracaraEntity mob, Level world) {
            this(mob, world, 0.0F);
        }

        public CaracaraPathNavigation(CaracaraEntity mob, Level world, float yMobOffset) {
            super(mob, world);
            this.yMobOffset = 0.0F;
            this.mob = mob;
            this.yMobOffset = yMobOffset;
        }

        public void tick() {
            if (this.mob.canFly()) {
                ++this.tick;
            } else {
                super.tick();
            }

        }

        public boolean moveTo(double x, double y, double z, double speedIn) {
            if (this.mob.canFly()) {
                this.mob.getMoveControl().setWantedPosition(x, y, z, speedIn);
                return true;
            } else {
                return super.moveTo(x, y, z, speedIn);
            }
        }

        public boolean moveTo(Entity entityIn, double speedIn) {
            if (this.mob.canFly()) {
                this.mob.getMoveControl().setWantedPosition(entityIn.getX(), entityIn.getY() + (double)this.yMobOffset, entityIn.getZ(), speedIn);
                return true;
            } else {
                return super.moveTo(entityIn, speedIn);
            }
        }
    }

    public class CaracaraMoveControl extends MoveControl {
        private final CaracaraEntity parentEntity;
        private final float speedGeneral;
        private final boolean shouldLookAtTarget;
        private final boolean needsYSupport;

        public CaracaraMoveControl(CaracaraEntity bird, float speedGeneral, boolean shouldLookAtTarget, boolean needsYSupport) {
            super(bird);
            this.parentEntity = bird;
            this.shouldLookAtTarget = shouldLookAtTarget;
            this.speedGeneral = speedGeneral;
            this.needsYSupport = needsYSupport;
        }

        public CaracaraMoveControl(CaracaraEntity bird, float speedGeneral, boolean shouldLookAtTarget) {
            this(bird, speedGeneral, shouldLookAtTarget, false);
        }

        public void tick() {
            if (this.parentEntity.canFly()) {
                if (this.operation == Operation.MOVE_TO) {
                    Vec3 vector3d = new Vec3(this.wantedX - this.parentEntity.getX(), this.wantedY - this.parentEntity.getY(), this.wantedZ - this.parentEntity.getZ());
                    double d0 = vector3d.length();
                    if (d0 < this.parentEntity.getBoundingBox().getSize()) {
                        this.operation = Operation.WAIT;
                        this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().scale((double)0.5F));
                    } else {
                        this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().add(vector3d.scale(this.speedModifier * (double)this.speedGeneral * 0.05 / d0)));
                        if (this.needsYSupport) {
                            double d1 = this.wantedY - this.parentEntity.getY();
                            this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().add((double)0.0F, (double)this.parentEntity.getSpeed() * (double)this.speedGeneral * Mth.clamp(d1, (double)-1.0F, (double)1.0F) * (double)0.6F, (double)0.0F));
                        }

                        if (this.parentEntity.getTarget() != null && this.shouldLookAtTarget) {
                            double d2 = this.parentEntity.getTarget().getX() - this.parentEntity.getX();
                            double d1 = this.parentEntity.getTarget().getZ() - this.parentEntity.getZ();
                            this.parentEntity.setYRot(-((float)Mth.atan2(d2, d1)) * (180F / (float)Math.PI));
                            this.parentEntity.yBodyRot = this.parentEntity.getYRot();
                        } else {
                            Vec3 vector3d1 = this.parentEntity.getDeltaMovement();
                            this.parentEntity.setYRot(-((float)Mth.atan2(vector3d1.x, vector3d1.z)) * (180F / (float)Math.PI));
                            this.parentEntity.yBodyRot = this.parentEntity.getYRot();
                        }
                    }
                } else if (this.operation == Operation.STRAFE) {
                    this.operation = Operation.WAIT;
                }
            } else {
                super.tick();
            }

        }

        private boolean canReach(Vec3 p_220673_1_, int p_220673_2_) {
            AABB axisalignedbb = this.parentEntity.getBoundingBox();

            for(int i = 1; i < p_220673_2_; ++i) {
                axisalignedbb = axisalignedbb.move(p_220673_1_);
                if (!this.parentEntity.level().noCollision(this.parentEntity, axisalignedbb)) {
                    return false;
                }
            }

            return true;
        }
    }

    class CaracaraStrollGoal extends RandomStrollGoal {

        public static final float PROBABILITY = 0.001F;
        protected final float probability;

        public CaracaraStrollGoal(PathfinderMob mob, double speedModifier) {
            this(mob, speedModifier, 0.001F);
        }

        public CaracaraStrollGoal(PathfinderMob mob, double speedModifier, float probability) {
            super(mob, speedModifier, 120, false);
            this.probability = probability;
        }

        @javax.annotation.Nullable
        protected Vec3 getPosition() {
            if (this.mob.isInWaterOrBubble()) {
                Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
                return vec3 == null ? super.getPosition() : vec3;
            } else {
                return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 15, 7) : super.getPosition();
            }
        }

        public boolean canUse() {
            return !CaracaraEntity.this.isFlying() && !CaracaraEntity.this.canFly() && CaracaraEntity.this.onGround() && super.canUse();
        }

        public boolean canContinueToUse() {
            return !CaracaraEntity.this.isFlying() && !CaracaraEntity.this.canFly() && CaracaraEntity.this.onGround() && super.canContinueToUse();
        }
    }

    public static enum AttackPhase {
        CIRCLE,
        SWOOP;

        private AttackPhase() {
        }
    }

    static class CaracaraStalkPrey extends Goal {
        private final CaracaraEntity bird;
        private final double speedModifier;
        @javax.annotation.Nullable
        private LivingEntity prey;
        int stalkingTicks;

        CaracaraStalkPrey(CaracaraEntity pBird, double pSpeedModifier) {
            this.bird = pBird;
            this.speedModifier = pSpeedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        public boolean canUse() {
            this.prey = this.bird.getTarget();
            return this.prey != null && this.bird.attackPhase == CaracaraEntity.AttackPhase.CIRCLE && this.bird.canFly();
        }

        public boolean canContinueToUse() {
            return this.bird.canFly() && this.prey != null && this.bird.distanceToSqr(this.prey.getX(), this.prey.getY() + (double)8.0F, this.prey.getZ()) < (double)256.0F && this.bird.attackPhase == CaracaraEntity.AttackPhase.CIRCLE;
        }

        public void start() {
            this.bird.setAggressive(true);
            this.bird.setDiving(false);
            this.stalkingTicks = 5;
        }

        public void stop() {
            if (this.prey == null) {
                this.bird.getNavigation().stop();
            }

        }

        public void tick() {
            this.bird.getLookControl().setLookAt(this.prey.getX(), this.prey.getY() + (double)8.0F, this.prey.getZ(), (float)this.bird.getMaxHeadYRot(), (float)this.bird.getMaxHeadXRot());
            if (this.bird.distanceToSqr(this.prey.getX(), this.prey.getY() + (double)8.0F, this.prey.getZ()) < 0.75F) {
                --this.stalkingTicks;
                if (this.stalkingTicks < 0) {
                    this.bird.attackPhase = CaracaraEntity.AttackPhase.SWOOP;
                    this.bird.setDiving(true);
                }
            } else {
                this.bird.getMoveControl().setWantedPosition(this.prey.getX(), this.prey.getY() + (double)8.0F, this.prey.getZ(), this.speedModifier);
            }

        }
    }

    public class CaracaraFlyMelee extends Goal {
        private final CaracaraEntity bird;
        float circleDistance = 1.0F;
        float yLevel = 2.0F;

        public CaracaraFlyMelee(CaracaraEntity pBird) {
            this.bird = pBird;
        }

        public boolean canUse() {
            Entity entity = this.bird.getTarget();
            return entity != null && entity.isAlive() && this.bird.canFly() && this.bird.attackPhase == CaracaraEntity.AttackPhase.SWOOP;
        }

        public void start() {
            this.yLevel = (float)this.bird.getRandom().nextInt(2);
            this.bird.setDiving(true);
        }

        public void stop() {
            this.yLevel = (float)this.bird.getRandom().nextInt(2);
            if (this.bird.onGround()) {
                this.bird.setFlying(false);
            }

            this.bird.setDiving(false);
        }

        public void tick() {
            LivingEntity target = this.bird.getTarget();
            if (target != null) {
                if (this.bird.distanceTo(target) < 3.0F) {
                    this.bird.doHurtTarget(target);
                    this.bird.attackPhase = CaracaraEntity.AttackPhase.CIRCLE;
                    this.bird.setDiving(false);
                    this.stop();
                }

                this.bird.getMoveControl().setWantedPosition(target.getX(), target.getY() + (double)(target.getEyeHeight() / 2.0F), target.getZ(), (double)1.0F);
            }

        }
    }

    private class AIFlyIdle extends Goal {
        protected double x;
        protected double y;
        protected double z;
        private boolean flightTarget;

        public AIFlyIdle() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            if (CaracaraEntity.this.isOverWaterOrVoid() && !CaracaraEntity.this.isPassenger()) {
                this.flightTarget = true;
                Vec3 lvt_1_1_ = this.getPosition();
                if (lvt_1_1_ == null) {
                    return false;
                } else {
                    this.x = lvt_1_1_.x;
                    this.y = lvt_1_1_.y;
                    this.z = lvt_1_1_.z;
                    return true;
                }
            } else if (CaracaraEntity.this.canFly() && !CaracaraEntity.this.isVehicle() && (CaracaraEntity.this.getTarget() == null || !CaracaraEntity.this.getTarget().isAlive()) && !CaracaraEntity.this.isPassenger()) {
                if (CaracaraEntity.this.getRandom().nextInt(45) != 0 && !CaracaraEntity.this.isFlying()) {
                    return false;
                } else {
                    this.flightTarget = CaracaraEntity.this.canFly();
                    Vec3 lvt_1_1_ = this.getPosition();
                    if (lvt_1_1_ == null) {
                        return false;
                    } else {
                        this.x = lvt_1_1_.x;
                        this.y = lvt_1_1_.y;
                        this.z = lvt_1_1_.z;
                        return true;
                    }
                }
            } else {
                return false;
            }
        }

        public void tick() {
            CaracaraEntity.this.getMoveControl().setWantedPosition(this.x, this.y, this.z, (double)0.8F);
            if (!this.flightTarget && CaracaraEntity.this.isFlying() && CaracaraEntity.this.onGround()) {
                CaracaraEntity.this.setFlying(false);
            }

            if (CaracaraEntity.this.isFlying() && CaracaraEntity.this.onGround() && !CaracaraEntity.this.canFly()) {
                CaracaraEntity.this.setFlying(false);
            }

        }

        @javax.annotation.Nullable
        protected Vec3 getPosition() {
            Vec3 vector3d = CaracaraEntity.this.position();
            return !CaracaraEntity.this.canFly() && !CaracaraEntity.this.isOverWaterOrVoid() ? CaracaraEntity.this.getBlockGrounding(vector3d) : CaracaraEntity.this.getBlockInViewAway(vector3d, 10.0F);
        }

        public boolean canContinueToUse() {
            return CaracaraEntity.this.isFlying() && CaracaraEntity.this.distanceToSqr(this.x, this.y, this.z) > (double)5.0F;
        }

        public void start() {
            CaracaraEntity.this.setFlying(true);
            CaracaraEntity.this.getMoveControl().setWantedPosition(this.x, this.y, this.z, (double)0.8F);
        }

        public void stop() {
            CaracaraEntity.this.getNavigation().stop();
            this.x = (double)0.0F;
            this.y = (double)0.0F;
            this.z = (double)0.0F;
            super.stop();
        }
    }
}
