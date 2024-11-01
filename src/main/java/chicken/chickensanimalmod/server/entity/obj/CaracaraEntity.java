package chicken.chickensanimalmod.server.entity.obj;

import chicken.chickensanimalmod.server.entity.AnimalModEntities;
import chicken.chickensanimalmod.server.entity.obj.geo.GeoEntityBase;
import chicken.chickensanimalmod.server.entity.obj.goal.CaracaraFlyGoal;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CaracaraEntity extends GeoEntityBase {
    protected final FlyingPathNavigation flyingPathNavigation;
    protected final FlyingMoveControl flyingMoveControl;

    protected final GroundPathNavigation groundPathNavigation;
    protected final MoveControl groundMoveControl;


    Vec3 moveTargetPoint;
    BlockPos anchorPoint;

    public CaracaraEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveTargetPoint = Vec3.ZERO;
        this.anchorPoint = BlockPos.ZERO;
        this.setPathfindingMalus(PathType.WATER, 0);
        this.flyingPathNavigation = createNavigation(level);
        this.flyingMoveControl = new FlyingMoveControl(this, 32, false);
        this.groundPathNavigation = new GroundPathNavigation(this, level);
        this.groundMoveControl = new MoveControl(this);
        this.moveControl = new CaracaraEntity.CaracaraEntityMoveControl(this);
        this.lookControl = new CaracaraEntity.CaracaraEntityLookControl(this, this);

        setFlying(false);
    }

    protected BodyRotationControl createBodyControl() {
        return new CaracaraEntity.CaracaraEntityBodyRotationControl(this);
    }

    @Override
    public boolean isHungry() {
        return super.isHungry();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.FLYING_SPEED, 0.4F).add(Attributes.ATTACK_DAMAGE, 1F).add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    protected @NotNull FlyingPathNavigation createNavigation(@NotNull Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(false);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

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
        if(!this.isBaby() && isFlying()) {
            FlyingPathNavigation navigator = (FlyingPathNavigation) this.getNavigation();
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
        } else {
            if(!this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.9F, 1));
            }
        }
    }

    private boolean isFlying() {
        return this.navigation == flyingPathNavigation;
    }

    @Override
    public boolean hasChildModel() {
        return true;
    }

    @Override
    public void move(@NotNull MoverType type, @NotNull Vec3 pos) {
        super.move(type, pos);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new CaracaraEntitySweepAttackGoal());
        this.goalSelector.addGoal(1, new CaracaraEntityCircleAroundAnchorGoal());
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, PigeonEntity.class, false));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Pig.class, false));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Sheep.class, false));

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1, CaracaraEntity.class));
        this.goalSelector.addGoal(12, new CaracaraFlyGoal(this, 1.0F));
    }

    @Override
    protected float getJumpPower() {
        return this.getJumpPower(1F);
    }

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

    private PlayState moveFlyController(AnimationState<CaracaraEntity> state) {
        CaracaraEntity entity = state.getAnimatable();
        if (entity.onGround()) {
            if(state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenPlay("walk"));
        } else if (!entity.isInWater() && !entity.onGround()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("fly"));
        }
        return state.setAndContinue(RawAnimation.begin().thenPlay("idle"));
    }

    public void setFlying(boolean b) {
        if(b && !isBaby()) {
            this.navigation = flyingPathNavigation;
            this.moveControl = flyingMoveControl;
            this.getNavigation().stop();
        } else {
            this.navigation = groundPathNavigation;
            this.moveControl = groundMoveControl;
            this.getNavigation().stop();
        }
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

    class CaracaraEntitySweepAttackGoal extends Goal {
        private boolean isScaredOfCat;
        private int catSearchTick;

        CaracaraEntitySweepAttackGoal() {
            super();
        }

        public boolean canUse() {
            return CaracaraEntity.this.getTarget() != null;
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = CaracaraEntity.this.getTarget();
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
                    if (CaracaraEntity.this.tickCount > this.catSearchTick) {
                        this.catSearchTick = CaracaraEntity.this.tickCount + 20;
                        List<Cat> list = CaracaraEntity.this.level().getEntitiesOfClass(Cat.class, CaracaraEntity.this.getBoundingBox().inflate(16.0), EntitySelector.ENTITY_STILL_ALIVE);

                        for (Cat cat : list) {
                            cat.hiss();
                        }

                        this.isScaredOfCat = !list.isEmpty();
                    }

                    return !this.isScaredOfCat;
                }
            }
        }

        public void start() {
            setFlying(true);
        }

        public void stop() {
            setFlying(false);
            CaracaraEntity.this.setTarget(null);
        }

        public void tick() {
            LivingEntity livingentity = CaracaraEntity.this.getTarget();
            if (livingentity != null) {
                CaracaraEntity.this.moveTargetPoint = new Vec3(livingentity.getX(), livingentity.getY(0.5), livingentity.getZ());
                if (CaracaraEntity.this.getBoundingBox().inflate(0.20000000298023224).intersects(livingentity.getBoundingBox())) {
                    CaracaraEntity.this.doHurtTarget(livingentity);
                    if (!CaracaraEntity.this.isSilent()) {
                        CaracaraEntity.this.level().levelEvent(1039, CaracaraEntity.this.blockPosition(), 0);
                    }
                }
            }

        }
    }

    class CaracaraEntityCircleAroundAnchorGoal extends Goal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        CaracaraEntityCircleAroundAnchorGoal() {
            super();
        }

        public boolean canUse() {
            return CaracaraEntity.this.getTarget() == null && random.nextFloat() <= 0.15;
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

    class CaracaraEntityLookControl extends LookControl {
        public CaracaraEntityLookControl(final CaracaraEntity this$0, Mob mob) {
            super(mob);
        }

        public void tick() {
        }
    }
}
