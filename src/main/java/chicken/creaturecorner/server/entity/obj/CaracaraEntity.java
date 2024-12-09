package chicken.creaturecorner.server.entity.obj;

import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.entity.obj.geo.GeoEntityBase;
import chicken.creaturecorner.server.entity.obj.goal.CaracaraFlyGoal;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CaracaraEntity extends GeoEntityBase {

//    protected final FlyingPathNavigation flyingPathNavigation;
//    protected final CaracaraEntityMoveControl flyingMoveControl;

    public boolean isSwooping;

    public boolean wantsToFly;

    private static final EntityDataAccessor<Integer> FLY_TICKS = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.BOOLEAN);

//    protected final GroundPathNavigation groundPathNavigation;
//    protected final MoveControl groundMoveControl;

    CaracaraEntity.AttackPhase attackPhase = CaracaraEntity.AttackPhase.CIRCLE;

    Vec3 moveTargetPoint;
    BlockPos anchorPoint;

    public CaracaraEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveTargetPoint = Vec3.ZERO;
        this.anchorPoint = BlockPos.ZERO;
        this.setPathfindingMalus(PathType.WATER, 0);
//        this.flyingPathNavigation = createNavigation(level);
//        this.flyingMoveControl = new CaracaraEntityMoveControl(this);
//        this.groundPathNavigation = new GroundPathNavigation(this, level);
        //this.moveControl = new MoveControl(this);
        //this.moveControl = new CaracaraEntity.CaracaraEntityMoveControl(this);
        this.lookControl = new CaracaraEntity.CaracaraLookControl(this);

        setFlying(false);
        this.wantsToFly = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLY_TICKS, 0);
        builder.define(FLYING, false);
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

    private void switchNavigator(boolean flying) {
        if (!flying || this.isBaby()) {
            this.moveControl = new MoveControl(this);
            //this.navigation = new GroundPathNavigation(this, level());
        } else {
            this.moveControl = new CaracaraEntity.CaracaraEntityMoveControl(this); //new FlightMoveController(this, 0.7F, false);
            //this.navigation = new FlyingPathNavigation(this, level());
        }
    }

    protected BodyRotationControl createBodyControl() {
        return new CaracaraEntity.CaracaraEntityBodyRotationControl(this);
    }

    @Override
    public boolean isHungry() {
        return super.isHungry();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.FLYING_SPEED, 0.4F).add(Attributes.ATTACK_DAMAGE, 4F).add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

//    protected @NotNull FlyingPathNavigation createNavigation(@NotNull Level pLevel) {
//        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
//        flyingpathnavigation.setCanOpenDoors(false);
//        flyingpathnavigation.setCanFloat(false);
//        flyingpathnavigation.setCanPassDoors(true);
//        return flyingpathnavigation;
//    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, @NotNull BlockState pState, @NotNull BlockPos pPos) {}

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

        if (!this.wantsToFly && this.isFlying()){
            this.setFlying(false);
        }

        if (this.getFlyTicks()<=0){
            this.wantsToFly = false;
        }else if (this.getFlyTicks()>=500){
            this.wantsToFly = true;
        }

        if (this.wantsToFly && this.isFlying() && this.getTarget() == null){
            this.setFlyTicks(prevFlyTicks-1);
        }else if (!this.wantsToFly){
            this.setFlyTicks(prevFlyTicks+1);
        }

        if(!this.onGround() && !this.isFlying() && this.getDeltaMovement().y<0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.75F, 1));
        }

        if(!this.isBaby() && isFlying() && this.navigation instanceof FlyingPathNavigation navigator) {
            Path path = navigator.getPath();
            if (path != null) {
                if (!path.isDone()) {
                    BlockPos nextPos = path.getNextNodePos();
                    int y = nextPos.getY();
                    int difference = y - getBlockY();
                    if (y > getBlockY()) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, difference > 1 ? 0.0075 : 0.003, 0.0));
                    } else if (y < getBlockY()) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, difference > -1 ? -0.0125 : -0.006, 0.0));
                    }

                    if (navigator.isStuck()) {
                        navigator.stop();
                    }
                }
            }
        }
    }

    @Override
    public void move(MoverType type, Vec3 pos) {
        super.move(type, pos);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new CaracaraEntity.CaracaraAttackStrategyGoal());
        this.goalSelector.addGoal(0, new CaracaraEntitySweepAttackGoal(this));
        this.goalSelector.addGoal(1, new CaracaraEntityCircleAroundAnchorGoal());
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, PigeonEntity.class, false));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Pig.class, false));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Sheep.class, false));

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new CaracaraEntity.CaracaraStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1, CaracaraEntity.class));
        this.goalSelector.addGoal(12, new CaracaraFlyGoal(this, 1.0F));
    }

//    @Override
//    protected float getJumpPower() {
//        return this.getJumpPower(1F);
//    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        CaracaraEntity entity = AnimalModEntities.CARACARA_TYPE.create(serverLevel);
        if(entity != null) {
            entity.setBaby(true);
        }
        return entity;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "moveFlyIdleController", 1, this::moveFlyController));
    }

    public void travel(Vec3 travelVector) {
        if (this.isFlying() && !this.isBaby()){
            if (this.isControlledByLocalInstance()) {
                if (this.isInWater()) {
                    this.moveRelative(0.02F, travelVector);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.800000011920929));
                } else if (this.isInLava()) {
                    this.moveRelative(0.02F, travelVector);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
                } else {
                    BlockPos ground = this.getBlockPosBelowThatAffectsMyMovement();
                    float f = 0.91F;
                    if (this.onGround()) {
                        f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
                    }

                    float f1 = 0.16277137F / (f * f * f);
                    f = 0.91F;
                    if (this.onGround()) {
                        f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
                    }

                    this.moveRelative(this.onGround() ? 0.1F * f1 : 0.02F, travelVector);
                    this.move(MoverType.SELF, this.getDeltaMovement());
                    this.setDeltaMovement(this.getDeltaMovement().scale((double)f));
                }
            }

            this.calculateEntityAnimation(false);
        }
        else super.travel(travelVector);
    }

    private PlayState moveFlyController(AnimationState<CaracaraEntity> state) {
        CaracaraEntity entity = state.getAnimatable();
        if (entity.onGround()) {
            if(state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenPlay("walk"));
        } else if (!entity.isInWater() && !entity.onGround()) {
            if (this.isSwooping){
                return state.setAndContinue(RawAnimation.begin().thenPlay("dive"));
            }else{
                return state.setAndContinue(RawAnimation.begin().thenPlay("fly"));
            }
        }
        return state.setAndContinue(RawAnimation.begin().thenPlay("idle"));
    }

    public static boolean spawnRules(EntityType<CaracaraEntity> pigeonEntityEntityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos pos, RandomSource randomSource) {
        boolean canContinue = serverLevelAccessor.isEmptyBlock(pos);
        if(canContinue) {
            Optional<ResourceKey<Biome>> biomeKey = serverLevelAccessor.getBiome(pos).unwrapKey();
            if(biomeKey.isPresent()) {
                ResourceKey<Biome> resourceKey = biomeKey.get();
                if(resourceKey == Biomes.END_BARRENS || resourceKey == Biomes.END_HIGHLANDS || resourceKey == Biomes.THE_END || resourceKey == Biomes.SMALL_END_ISLANDS) {
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
                } else if(serverLevelAccessor.getBiomeManager().getBiome(pos).is(BiomeTags.IS_MOUNTAIN)) {
                    return true;
                } else {
                    return serverLevelAccessor.getLevel().isVillage(pos);
                }
            }
        }
        return false;
    }

    class CaracaraEntityMoveControl extends MoveControl {
        private float speed = 0.1F;

        public CaracaraEntityMoveControl(Mob mob) {
            super(mob);
        }

        public void tick() {
            if (CaracaraEntity.this.horizontalCollision) {
                CaracaraEntity.this.setYRot(CaracaraEntity.this.getYRot() + 180.0F);
                this.speed = 0.1F;
            }

            double d0 = CaracaraEntity.this.moveTargetPoint.x - CaracaraEntity.this.getX();
            double d1 = CaracaraEntity.this.moveTargetPoint.y - CaracaraEntity.this.getY();
            double d2 = CaracaraEntity.this.moveTargetPoint.z - CaracaraEntity.this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            if (Math.abs(d3) > 9.999999747378752E-6) {
                double d4 = 1.0 - Math.abs(d1 * 0.699999988079071) / d3;
                d0 *= d4;
                d2 *= d4;
                d3 = Math.sqrt(d0 * d0 + d2 * d2);
                double d5 = Math.sqrt(d0 * d0 + d2 * d2 + d1 * d1);
                float f = CaracaraEntity.this.getYRot();
                float f1 = (float)Mth.atan2(d2, d0);
                float f2 = Mth.wrapDegrees(CaracaraEntity.this.getYRot() + 90.0F);
                float f3 = Mth.wrapDegrees(f1 * 57.295776F);
                CaracaraEntity.this.setYRot(Mth.approachDegrees(f2, f3, 4.0F) - 90.0F);
                CaracaraEntity.this.yBodyRot = CaracaraEntity.this.getYRot();
                if (Mth.degreesDifferenceAbs(f, CaracaraEntity.this.getYRot()) < 3.0F) {
                    this.speed = Mth.approach(this.speed, 1.8F, 0.005F * (1.8F / this.speed));
                } else {
                    this.speed = Mth.approach(this.speed, 0.2F, 0.025F);
                }

                float f4 = (float)(-(Mth.atan2(-d1, d3) * 180.0 / 3.1415927410125732));
                CaracaraEntity.this.setXRot(f4);
                float f5 = CaracaraEntity.this.getYRot() + 90.0F;
                double d6 = (double)(this.speed * Mth.cos(f5 * 0.017453292F)) * Math.abs(d0 / d5);
                double d7 = (double)(this.speed * Mth.sin(f5 * 0.017453292F)) * Math.abs(d2 / d5);
                double d8 = (double)(this.speed * Mth.sin(f4 * 0.017453292F)) * Math.abs(d1 / d5);
                Vec3 vec3 = CaracaraEntity.this.getDeltaMovement();
                CaracaraEntity.this.setDeltaMovement(vec3.add((new Vec3(d6, d8, d7)).subtract(vec3).scale(0.2)));
            }

        }
    }

    class CaracaraEntityBodyRotationControl extends BodyRotationControl {
        public CaracaraEntityBodyRotationControl(Mob mob) {
            super(mob);
        }

        public void clientTick() {
            CaracaraEntity.this.yHeadRot = CaracaraEntity.this.yBodyRot;
            CaracaraEntity.this.yBodyRot = CaracaraEntity.this.getYRot();
        }
    }

    class CaracaraEntitySweepAttackGoal extends CaracaraMoveTargetGoal {
        private boolean isScaredOfCat;
        private int catSearchTick;
        CaracaraEntity caracara;

        CaracaraEntitySweepAttackGoal(CaracaraEntity pCaracara) {
            super();
            this.caracara = pCaracara;
        }

        public boolean canUse() {
            return caracara.getTarget() != null && caracara.attackPhase == CaracaraEntity.AttackPhase.SWOOP;
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = caracara.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                if (livingentity instanceof Player player) {
                    if (livingentity.isSpectator() || player.isCreative()) {
                        return false;
                    }
                }

                if (!this.canUse()) {
                    return false;
                } else {
                    if (caracara.tickCount > this.catSearchTick) {
                        this.catSearchTick = caracara.tickCount + 20;
                        List<Cat> list = caracara.level().getEntitiesOfClass(Cat.class, caracara.getBoundingBox().inflate(16.0), EntitySelector.ENTITY_STILL_ALIVE);

                        this.isScaredOfCat = !list.isEmpty();
                    }

                    return !this.isScaredOfCat;
                }
            }
        }

        public void start() {
            caracara.setFlying(true);
            caracara.isSwooping = true;
        }

        public void stop() {

            //setFlying(false);
            caracara.setTarget(null);
            caracara.attackPhase = CaracaraEntity.AttackPhase.CIRCLE;
            caracara.isSwooping = false;
        }

        @Override
        public void tick() {
            LivingEntity livingentity = caracara.getTarget();
            if (livingentity != null) {
                caracara.moveTargetPoint = new Vec3(livingentity.getX(), livingentity.getY(0.5), livingentity.getZ());
                if (caracara.getBoundingBox().inflate(0.2F).intersects(livingentity.getBoundingBox())) {
                    caracara.doHurtTarget(livingentity);
                    caracara.attackPhase = CaracaraEntity.AttackPhase.CIRCLE;

                } else if (caracara.horizontalCollision || caracara.hurtTime > 0) {
                    caracara.attackPhase = CaracaraEntity.AttackPhase.CIRCLE;
                }
            }
        }
    }

    class CaracaraStrollGoal extends WaterAvoidingRandomStrollGoal{

        CaracaraEntity caracara;

        public CaracaraStrollGoal(CaracaraEntity mob, double speedModifier) {
            super(mob, speedModifier);
            this.caracara = mob;
        }

        @Override
        public boolean canUse() {
            return !caracara.isFlying() && !caracara.wantsToFly && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !caracara.isFlying() && !caracara.wantsToFly && super.canContinueToUse();
        }
    }


    class CaracaraAttackStrategyGoal extends Goal {
        private int nextSweepTick;

        @Override
        public boolean canUse() {
            LivingEntity livingentity = CaracaraEntity.this.getTarget();
            return livingentity != null && CaracaraEntity.this.canAttack(livingentity, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            CaracaraEntity.this.attackPhase = CaracaraEntity.AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
            CaracaraEntity.this.wantsToFly = true;
            CaracaraEntity.this.setFlyTicks(500);
        }

        @Override
        public void stop() {
            CaracaraEntity.this.anchorPoint = CaracaraEntity.this.level()
                    .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, CaracaraEntity.this.anchorPoint)
                    .above(10 + CaracaraEntity.this.random.nextInt(20));
            CaracaraEntity.this.wantsToFly = false;
        }

        @Override
        public void tick() {
            if (CaracaraEntity.this.attackPhase == CaracaraEntity.AttackPhase.CIRCLE) {
                this.nextSweepTick--;
                if (this.nextSweepTick <= 0) {
                    CaracaraEntity.this.attackPhase = CaracaraEntity.AttackPhase.SWOOP;
                    this.setAnchorAboveTarget();
                    this.nextSweepTick = this.adjustedTickDelay((8 + CaracaraEntity.this.random.nextInt(4)) * 20);
                    //CaracaraEntity.this.playSound(SoundEvents.PHANTOM_SWOOP, 10.0F, 0.95F + CaracaraEntity.this.random.nextFloat() * 0.1F);
                }
            }
        }

        private void setAnchorAboveTarget() {
            CaracaraEntity.this.anchorPoint = CaracaraEntity.this.getTarget().blockPosition().above(20 + CaracaraEntity.this.random.nextInt(20));
            if (CaracaraEntity.this.anchorPoint.getY() < CaracaraEntity.this.level().getSeaLevel()) {
                CaracaraEntity.this.anchorPoint = new BlockPos(
                        CaracaraEntity.this.anchorPoint.getX(), CaracaraEntity.this.level().getSeaLevel() + 1, CaracaraEntity.this.anchorPoint.getZ()
                );
            }
        }
    }

    static enum AttackPhase {
        CIRCLE,
        SWOOP;
    }

    class CaracaraEntityCircleAroundAnchorGoal extends CaracaraMoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        CaracaraEntityCircleAroundAnchorGoal() {
            super();
        }

        public boolean canUse() {
            return (CaracaraEntity.this.wantsToFly && CaracaraEntity.this.getTarget() == null)
                    || CaracaraEntity.this.attackPhase == CaracaraEntity.AttackPhase.CIRCLE;
        }

        public void start() {
            this.distance = 5.0F + CaracaraEntity.this.random.nextFloat() * 10.0F;
            this.height = -4.0F + CaracaraEntity.this.random.nextFloat() * 9.0F;
            this.clockwise = CaracaraEntity.this.random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNext();
            setFlying(true);
        }

        public void tick() {
            if (CaracaraEntity.this.random.nextInt(this.adjustedTickDelay(350)) == 0) {
                this.height = -4.0F + CaracaraEntity.this.random.nextFloat() * 9.0F;
            }

            if (CaracaraEntity.this.random.nextInt(this.adjustedTickDelay(250)) == 0) {
                ++this.distance;
                if (this.distance > 15.0F) {
                    this.distance = 5.0F;
                    this.clockwise = -this.clockwise;
                }
            }

            if (CaracaraEntity.this.random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = CaracaraEntity.this.random.nextFloat() * 2.0F * 3.1415927F;
                this.selectNext();
            }

            if (this.touchingTarget()) {
                this.selectNext();
            }

            if (CaracaraEntity.this.moveTargetPoint.y < CaracaraEntity.this.getY() && !CaracaraEntity.this.level().isEmptyBlock(CaracaraEntity.this.blockPosition().below(1))) {
                this.height = Math.max(1.0F, this.height);
                this.selectNext();
            }

            if (CaracaraEntity.this.moveTargetPoint.y > CaracaraEntity.this.getY() && !CaracaraEntity.this.level().isEmptyBlock(CaracaraEntity.this.blockPosition().above(1))) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNext();
            }

        }

        private void selectNext() {
            if (BlockPos.ZERO.equals(CaracaraEntity.this.anchorPoint)) {
                CaracaraEntity.this.anchorPoint = CaracaraEntity.this.blockPosition();
            }

            this.angle += this.clockwise * 15.0F * 0.017453292F;
            CaracaraEntity.this.moveTargetPoint = Vec3.atLowerCornerOf(CaracaraEntity.this.anchorPoint).add(this.distance * Mth.cos(this.angle), -4.0F + this.height, this.distance * Mth.sin(this.angle));
        }

        protected boolean touchingTarget() {
            return CaracaraEntity.this.moveTargetPoint.distanceToSqr(CaracaraEntity.this.getX(), CaracaraEntity.this.getY(), CaracaraEntity.this.getZ()) < 4.0;
        }

        @Override
        public void stop() {
            setFlying(false);
        }
    }

    abstract class CaracaraMoveTargetGoal extends Goal {
        public CaracaraMoveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return CaracaraEntity.this.moveTargetPoint.distanceToSqr(CaracaraEntity.this.getX(), CaracaraEntity.this.getY(), CaracaraEntity.this.getZ()) < 4.0;
        }
    }

    class CaracaraLookControl extends LookControl {
        public CaracaraLookControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
        }
    }
}
