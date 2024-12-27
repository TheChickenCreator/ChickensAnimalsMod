package chicken.creaturecorner.server.entity.obj;

import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.entity.obj.geo.GeoEntityBase;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;

import java.util.*;

//todo: make caracaras not touch the floor while moving and flying, if their navigation is done then don't make them not touch the floor.

public class CaracaraEntity extends GeoEntityBase {

    public boolean wantsToFly;

    private static final EntityDataAccessor<Integer> FLY_TICKS = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DIVING = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.BOOLEAN);

    public CaracaraEntity.AttackPhase attackPhase = CaracaraEntity.AttackPhase.CIRCLE;

    public CaracaraEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0);

        this.moveControl = new CaracaraMoveControl(this); //new FlightMoveController(this, 0.7F, false);
        this.navigation = new FlyingPathNavigation(this, this.level());
        this.lookControl = new SmoothSwimmingLookControl(this, 2);
        this.jumpControl = new CaracaraJumpControl(this);
        this.wantsToFly = false;
    }

    public class CaracaraMoveControl extends MoveControl {
        private final float maxTurn = 0.5f;
        CaracaraEntity caracara;

        public CaracaraMoveControl(CaracaraEntity mob) {
            super(mob);
            this.caracara = mob;
        }

        public void tick() {
            if (this.caracara.canFly()){
                if (this.operation == Operation.MOVE_TO) {
                    this.operation = Operation.WAIT;
                    this.mob.setNoGravity(true);
                    double d0 = this.wantedX - this.mob.getX();
                    double d1 = this.wantedY - this.mob.getY();
                    double d2 = this.wantedZ - this.mob.getZ();
                    double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                    if (d3 < 2.500000277905201E-7) {
                        this.mob.setYya(0.0F);
                        this.mob.setZza(0.0F);
                        return;
                    }

                    float f = (float)(Mth.atan2(d2, d0) * 180.0 / 3.1415927410125732) - 90.0F;
                    this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, 45.0F));
                    float f1;
                    if (this.mob.onGround() && this.caracara.isDiving()){
                        f1 = 0;
                        this.mob.addDeltaMovement(new Vec3(0, 0.75, 0));
                    }else if (this.mob.onGround()) {
                        f1 = (float)(0.1 * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                        this.mob.addDeltaMovement(new Vec3(0, 0.5, 0));
                    }else {
                        f1 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
                    }

                    this.mob.setSpeed(f1);
                    double d4 = Math.sqrt(d0 * d0 + d2 * d2);
                    if (Math.abs(d1) > 9.999999747378752E-6 || Math.abs(d4) > 9.999999747378752E-6) {
                        float f2 = (float)(-(Mth.atan2(d1, d4) * 180.0 / 3.1415927410125732));
                        this.mob.setXRot(this.rotlerp(this.mob.getXRot(), f2, this.maxTurn));
                        this.mob.setYya(d1 > 0.0 ? f1 : -f1);
                    }
                }
            }else {
                this.mob.setNoGravity(false);
                super.tick();
            }
        }
    }

    public boolean canFly(){
        return this.wantsToFly && !this.isBaby();
    }

    static class CaracaraJumpControl extends JumpControl {

        CaracaraEntity mob;
        public CaracaraJumpControl(CaracaraEntity caracara) {
            super(caracara);
            mob = caracara;
        }

        @Override
        public void jump() {
            if (mob.isFlying() && !this.mob.onGround()){
                super.jump();
            }
        }
    }

    @Override
    protected void registerGoals() {
        //this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 2, true));

        this.goalSelector.addGoal(2, new CaracaraAttackGoal(this, 2, 3, true));
        this.goalSelector.addGoal(2, new CaracaraStalkPrey(this, 2));

        //this.goalSelector.addGoal(2, new AITackle());

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PigeonEntity.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Rabbit.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Pig.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Sheep.class, false));

        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(4, new CaracaraStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(8, new CaracaraFlyGoal());

        //this.goalSelector.addGoal(4, new AIWanderIdle());

        this.goalSelector.addGoal(4, new BreedGoal(this, 1, CaracaraEntity.class));
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLY_TICKS, 0);
        builder.define(FLYING, false);
        builder.define(DIVING, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("FlyTicks", getFlyTicks());
        compound.putBoolean("IsFlying", isFlying());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setFlyTicks(compound.getInt("FlyTicks"));
        this.setFlying(compound.getBoolean("IsFlying"));
    }

    public int getFlyTicks() {
        return this.entityData.get(FLY_TICKS);
    }

    public void setFlyTicks(int ticks) {
        this.entityData.set(FLY_TICKS, ticks);
    }

    public void setFlying(boolean flight) {
        this.entityData.set(FLYING, flight);
        switchNavigator(flight);
    }

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    public void setDiving(boolean flight) {
        this.entityData.set(DIVING, flight);
    }

    public boolean isDiving() {
        return this.entityData.get(DIVING);
    }

    private void switchNavigator(boolean flying) {

        if (flying && this.canFly() && this.navigation instanceof GroundPathNavigation){
            this.navigation = new FlyingPathNavigation(this, this.level());
        }else if (!flying && this.navigation instanceof FlyingPathNavigation){
            this.navigation = new GroundPathNavigation(this, this.level());
        }
    }

    @Override
    public boolean isHungry() {
        return super.isHungry();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.STEP_HEIGHT, 1F).add(Attributes.FLYING_SPEED, 1.5F).add(Attributes.ATTACK_DAMAGE, 4F).add(Attributes.MOVEMENT_SPEED, 0.1F).add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, @NotNull BlockState pState, @NotNull BlockPos pPos) {
        if (this.isBaby())
            super.checkFallDamage(pY, pOnGround, pState, pPos);
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
    public void tick() {
        super.tick();

        int prevFlyTicks = this.getFlyTicks();

        if (!this.canFly() && this.onGround()) {
            this.setFlying(false);
        }

        if (this.getFlyTicks() <= 0) {
            this.wantsToFly = false;
        } else if (this.getFlyTicks() >= 1000) {
            this.wantsToFly = true;
        }

        if (!this.isAggressive()){
            if (this.canFly() && this.isFlying()) {
                this.setFlyTicks(prevFlyTicks - 1);
            } else if (!this.canFly() && this.onGround()) {
                this.setFlyTicks(prevFlyTicks + 3);
            }
        }

        if (this.getTarget() instanceof PigeonEntity pigeon && this.canFly()){
            if (pigeon.isFlying() && this.random.nextInt(10) == 0){
                this.setFlyTicks(500);
                this.setFlying(true);
            }

//            else if (!pigeon.isFlying() && this.random.nextInt(10) == 0){
//                this.setFlyTicks(0);
//            }
        }


        if (!this.onGround() && !this.isFlying() && this.getDeltaMovement().y < 0 && !this.isBaby()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.5F, 1));
        }

//        if (!this.isBaby() && isFlying()) {
//            Path path = this.navigation.getPath();
//            if (path != null) {
//                if (!path.isDone()) {
//                    BlockPos nextPos = path.getNextNodePos();
//                    int y = nextPos.getY();
//                    int difference = y - getBlockY();
//                    if (y > getBlockY()) {
//                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, difference > 1 ? 0.0075 : 0.003, 0.0));
//                    } else if (y < getBlockY()) {
//                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, difference > -1 ? -0.0125 : -0.006, 0.0));
//                    }
//
//                    if (this.navigation.isStuck()) {
//                        this.navigation.stop();
//                    }
//                }
//            }
//        }
    }


    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        CaracaraEntity entity = AnimalModEntities.CARACARA_TYPE.create(serverLevel);
        if (entity != null) {
            entity.setBaby(true);
        }
        return entity;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "moveFlyIdleController", 3, this::moveFlyController));
    }

    boolean isSwooping(){
        return this.attackPhase == AttackPhase.SWOOP && this.isFlying() && this.getTarget() != null;
    }

    private PlayState moveFlyController(AnimationState<CaracaraEntity> state) {
        CaracaraEntity entity = state.getAnimatable();

        if (!entity.isInWater() && !entity.onGround()) {
            if (entity.isDiving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("dive"));
            } else {
                return state.setAndContinue(RawAnimation.begin().thenLoop("fly"));
            }
        }else if (entity.onGround() && entity.getDeltaMovement().horizontalDistanceSqr() > 0.0001) {
            if (entity.isAggressive()){
                return state.setAndContinue(RawAnimation.begin().thenLoop("run"));
            }else {

                return state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
        }

        return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
    }

    public static boolean spawnRules(EntityType<CaracaraEntity> pigeonEntityEntityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos pos, RandomSource randomSource) {
        boolean canContinue = serverLevelAccessor.isEmptyBlock(pos);
        if (canContinue) {
            Optional<ResourceKey<Biome>> biomeKey = serverLevelAccessor.getBiome(pos).unwrapKey();
            if (biomeKey.isPresent()) {
                ResourceKey<Biome> resourceKey = biomeKey.get();
                if (resourceKey == Biomes.END_BARRENS || resourceKey == Biomes.END_HIGHLANDS || resourceKey == Biomes.THE_END || resourceKey == Biomes.SMALL_END_ISLANDS) {
                    StructureManager manager = serverLevelAccessor.getLevel().structureManager();
                    if (manager.hasAnyStructureAt(pos)) {
                        Map<Structure, LongSet> structures = manager.getAllStructuresAt(pos);
                        for (Structure structure : structures.keySet()) {
                            if (structure.type() == StructureType.END_CITY) {
                                return true;
                            }
                        }
                    }
                    return false;
                } else if (serverLevelAccessor.getBiomeManager().getBiome(pos).is(BiomeTags.IS_MOUNTAIN)) {
                    return true;
                } else {
                    return serverLevelAccessor.getLevel().isVillage(pos);
                }
            }
        }
        return false;
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

    @Override
    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target) &&
                (!this.isBaby() || (this.isBaby() && (target instanceof PigeonEntity || target instanceof Rabbit || target instanceof Chicken)));
    }

    public enum AttackPhase {
        CIRCLE,
        SWOOP;
    }

//    @Override
//    public void move(@NotNull MoverType type, @NotNull Vec3 pos) {
//        super.move(type, pos);
//    }

//    @Override
//    protected float getJumpPower() {
//        return this.getJumpPower(1F);
//    }

//    boolean isGroundSafeToLand(){
//        return this.get
//    }

    class CaracaraFlyGoal extends Goal {
        private static final int WANDER_THRESHOLD = 22;

        CaracaraFlyGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return CaracaraEntity.this.canFly() && CaracaraEntity.this.navigation.isDone() && CaracaraEntity.this.random.nextInt(10) == 0;
        }

        public boolean canContinueToUse() {
            return !CaracaraEntity.this.isBaby() && CaracaraEntity.this.isFlying() && CaracaraEntity.this.navigation.isInProgress();
        }

        public void start() {
            CaracaraEntity.this.setFlying(true);
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                CaracaraEntity.this.navigation.moveTo(CaracaraEntity.this.navigation.createPath(BlockPos.containing(vec3), 1), 1.0);
            }

        }

        @javax.annotation.Nullable
        private Vec3 findPos() {
            Vec3 vec3= CaracaraEntity.this.getViewVector(0.0F);

            Vec3 vec32 = HoverRandomPos.getPos(CaracaraEntity.this, 8, 7, vec3.x, vec3.z, (float) (Math.PI / 2), 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(CaracaraEntity.this, 8, 4, -2, vec3.x, vec3.z, (float) (Math.PI / 2));
        }

        @Override
        public void stop() {
            if (CaracaraEntity.this.getTarget()==null){
                CaracaraEntity.this.setFlying(false);
            }
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        //this.anchorPoint = this.blockPosition().above(5);

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public class CaracaraAttackGoal extends Goal {
        protected final CaracaraEntity mob;
        private final double flySpeedModifier;
        private final double runSpeedModifier;
        private final boolean followingTargetEvenIfNotSeen;
        private Path path;
        private double pathedTargetX;
        private double pathedTargetY;
        private double pathedTargetZ;
        private int ticksUntilNextPathRecalculation;
        private int ticksUntilNextAttack;
        private final int attackInterval = 20;
        private long lastCanUseCheck;
        private static final long COOLDOWN_BETWEEN_CAN_USE_CHECKS = 20L;
        private int failedPathFindingPenalty = 0;
        private boolean canPenalize = false;

        public CaracaraAttackGoal(CaracaraEntity mob, double flyingSpeedModifier, double runningSpeedModifier, boolean followingTargetEvenIfNotSeen) {
            this.mob = mob;
            this.flySpeedModifier = flyingSpeedModifier;
            this.runSpeedModifier = runningSpeedModifier;
            this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            if (this.mob.attackPhase == AttackPhase.CIRCLE && this.mob.isFlying()){
                return false;
            }
            long i = this.mob.level().getGameTime();
            if (i - this.lastCanUseCheck < 20L) {
                return false;
            } else {
                this.lastCanUseCheck = i;
                LivingEntity livingentity = this.mob.getTarget();
                if (livingentity == null) {
                    return false;
                } else if (!livingentity.isAlive()) {
                    return false;
                } else if (this.canPenalize) {
                    if (--this.ticksUntilNextPathRecalculation <= 0) {
                        this.path = this.mob.getNavigation().createPath(livingentity, 0);
                        this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                        return this.path != null;
                    } else {
                        return true;
                    }
                } else {
                    this.path = this.mob.getNavigation().createPath(livingentity, 0);
                    return this.path != null ? true : this.mob.isWithinMeleeAttackRange(livingentity);
                }
            }
        }

        public boolean canContinueToUse() {
            if (this.mob.attackPhase == AttackPhase.CIRCLE && this.mob.isFlying()){
                return false;
            }
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else if (!this.followingTargetEvenIfNotSeen) {
                return !this.mob.getNavigation().isDone();
            } else {
                return !this.mob.isWithinRestriction(livingentity.blockPosition()) ? false : !(livingentity instanceof Player)
                        || !livingentity.isSpectator() && !((Player)livingentity).isCreative();
            }
        }

        public void start() {
            if (this.mob.isFlying())
                this.mob.setDiving(true);
            this.mob.getNavigation().moveTo(this.path, this.mob.isFlying() && !this.mob.onGround() ? this.flySpeedModifier : this.runSpeedModifier);
            this.mob.setAggressive(true);
            this.ticksUntilNextPathRecalculation = 0;
            this.ticksUntilNextAttack = 0;
        }

        public void stop() {
            if (this.mob.isDiving())
                this.mob.setDiving(false);
            LivingEntity livingentity = this.mob.getTarget();
            if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
                this.mob.setTarget(null);
            }
            if (livingentity == null || livingentity.isDeadOrDying()){
                this.mob.setAggressive(false);
                this.mob.stopInPlace();
            }
            //this.mob.getNavigation().stop();
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = this.mob.getTarget();
            if (livingentity != null) {
                this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
                this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
                if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0 || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0 || this.mob.getRandom().nextFloat() < 0.05F)) {
                    this.pathedTargetX = livingentity.getX();
                    this.pathedTargetY = livingentity.getY();
                    this.pathedTargetZ = livingentity.getZ();
                    this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                    double d0 = this.mob.distanceToSqr(livingentity);
                    if (this.canPenalize) {
                        this.ticksUntilNextPathRecalculation += this.failedPathFindingPenalty;
                        if (this.mob.getNavigation().getPath() != null) {
                            Node finalPathPoint = this.mob.getNavigation().getPath().getEndNode();
                            if (finalPathPoint != null && livingentity.distanceToSqr((double)finalPathPoint.x, (double)finalPathPoint.y, (double)finalPathPoint.z) < 1.0) {
                                this.failedPathFindingPenalty = 0;
                            } else {
                                this.failedPathFindingPenalty += 10;
                            }
                        } else {
                            this.failedPathFindingPenalty += 10;
                        }
                    }

                    if (d0 > 1024.0) {
                        this.ticksUntilNextPathRecalculation += 10;
                    } else if (d0 > 256.0) {
                        this.ticksUntilNextPathRecalculation += 5;
                    }

                    if (!this.mob.getNavigation().moveTo(livingentity, this.mob.isFlying() && !this.mob.onGround() ? this.flySpeedModifier : this.runSpeedModifier)) {
                        this.ticksUntilNextPathRecalculation += 15;
                    }

                    this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
                }

                if (!this.mob.isFlying())
                    this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
                this.checkAndPerformAttack(livingentity);
            }

        }

        protected void checkAndPerformAttack(LivingEntity target) {
            if (this.canPerformAttack(target)) {

                if (!this.mob.isFlying())
                    this.resetAttackCooldown();

                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(target);

                if (this.mob.isFlying())
                    this.mob.attackPhase = AttackPhase.CIRCLE;
            }

        }

        protected void resetAttackCooldown() {
            this.ticksUntilNextAttack = this.adjustedTickDelay(20);
        }

        protected boolean isTimeToAttack() {
            return this.ticksUntilNextAttack <= 0;
        }

        protected boolean canPerformAttack(LivingEntity entity) {
            return this.isTimeToAttack() && this.mob.isWithinMeleeAttackRange(entity) && this.mob.getSensing().hasLineOfSight(entity);
        }

        protected int getTicksUntilNextAttack() {
            return this.ticksUntilNextAttack;
        }

        protected int getAttackInterval() {
            return this.adjustedTickDelay(20);
        }
    }

    static class CaracaraStalkPrey extends Goal {
        private final CaracaraEntity bird;
        private final double speedModifier;
        @javax.annotation.Nullable
        private LivingEntity prey;

        CaracaraStalkPrey(CaracaraEntity pBird, double pSpeedModifier) {
            this.bird = pBird;
            this.speedModifier = pSpeedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            this.prey = this.bird.getTarget();
            return this.prey != null && this.bird.attackPhase == AttackPhase.CIRCLE && this.bird.canFly();
        }

        public boolean canContinueToUse() {
            return this.bird.canFly() && this.prey != null && this.bird.distanceToSqr(this.prey.getX(), this.prey.getY()+8, this.prey.getZ()) < 256.0D && this.bird.attackPhase == AttackPhase.CIRCLE;
        }

        @Override
        public void start() {
            this.bird.setAggressive(true);
        }

        public void stop() {
            //this.prey = null;
            if (this.prey == null)
                this.bird.getNavigation().stop();
        }

        public void tick() {
            this.bird.getLookControl().setLookAt(this.prey.getX(), this.prey.getY()+8, this.prey.getZ(), (float)(this.bird.getMaxHeadYRot()), (float)this.bird.getMaxHeadXRot());
            if (this.bird.distanceToSqr(this.prey.getX(), this.prey.getY()+8, this.prey.getZ()) < 1D) {
                //this.bird.getNavigation().stop();
                this.bird.attackPhase = AttackPhase.SWOOP;
            } else {
                this.bird.getNavigation().moveTo(this.prey.getX(), this.prey.getY()+8, this.prey.getZ(), this.speedModifier);
            }

        }
    }
}
