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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;

import java.util.*;

public class CaracaraEntity extends GeoEntityBase {

    public boolean isSwooping;

    public boolean wantsToFly;

    private static final EntityDataAccessor<Integer> FLY_TICKS = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(CaracaraEntity.class, EntityDataSerializers.BOOLEAN);

    CaracaraEntity.AttackPhase attackPhase = CaracaraEntity.AttackPhase.CIRCLE;

    public CaracaraEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0);

        this.moveControl = new CaracaraMoveControl(this, 5); //new FlightMoveController(this, 0.7F, false);
        this.navigation = new FlyingPathNavigation(this, this.level());
        this.jumpControl = new CaracaraJumpControl(this);
        this.wantsToFly = false;
    }

    public class CaracaraMoveControl extends MoveControl {
        private final int maxTurn;
        CaracaraEntity caracara;

        public CaracaraMoveControl(CaracaraEntity mob, int maxTurn) {
            super(mob);
            this.caracara = mob;
            this.maxTurn = maxTurn;
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
                    this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, 90.0F));
                    float f1;
                    if (this.mob.onGround()) {
                        f1 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    } else {
                        f1 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
                    }

                    this.mob.setSpeed(f1);
                    double d4 = Math.sqrt(d0 * d0 + d2 * d2);
                    if (Math.abs(d1) > 9.999999747378752E-6 || Math.abs(d4) > 9.999999747378752E-6) {
                        float f2 = (float)(-(Mth.atan2(d1, d4) * 180.0 / 3.1415927410125732));
                        this.mob.setXRot(this.rotlerp(this.mob.getXRot(), f2, (float)this.maxTurn));
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
            if (mob.canFly()){
                super.jump();
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 2, true));

        //this.goalSelector.addGoal(2, new AITackle());

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PigeonEntity.class, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Chicken.class, false));
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

        if (flying && this.canFly() && this.navigation instanceof GroundPathNavigation){
            this.navigation = new FlyingPathNavigation(this, this.level());
        }else if (this.navigation instanceof FlyingPathNavigation){
            this.navigation = new GroundPathNavigation(this, this.level());
        }

//        if (!flying || this.isBaby()) {
//            this.moveControl = new MoveControl(this);
//            this.navigation = new GroundPathNavigation(this, level());
//        } else {
//            //this.moveControl = new FlyingMoveControl(this, 20, false); //new FlightMoveController(this, 0.7F, false);
//            //this.navigation = new FlyingPathNavigation(this, this.level());
//
//            this.moveControl = new MoveHelper(this);
//            this.navigation = new DirectPathNavigator(this, level());
//        }
    }

    @Override
    public boolean isHungry() {
        return super.isHungry();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.STEP_HEIGHT, 1F).add(Attributes.FLYING_SPEED, 1F).add(Attributes.ATTACK_DAMAGE, 4F).add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, @NotNull BlockState pState, @NotNull BlockPos pPos) {
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

        if (!this.wantsToFly && this.isFlying()) {
            this.setFlying(false);
        }

        if (this.getFlyTicks() <= 0) {
            this.wantsToFly = false;
        } else if (this.getFlyTicks() >= 500) {
            this.wantsToFly = true;
        }

        if (!this.isAggressive()){
            if (this.wantsToFly && this.isFlying()) {
                this.setFlyTicks(prevFlyTicks - 1);
            } else if (!this.wantsToFly) {
                this.setFlyTicks(prevFlyTicks + 1);
            }
        }

        if (this.getTarget() instanceof PigeonEntity pigeon){
            if (pigeon.isFlying() && this.random.nextInt(10) == 0){
                this.setFlyTicks(500);
            }else if (!pigeon.isFlying() && this.random.nextInt(10) == 0){
                this.setFlyTicks(0);
            }
        }


        if (!this.onGround() && !this.isFlying() && this.getDeltaMovement().y < 0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.75F, 1));
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


    private PlayState moveFlyController(AnimationState<CaracaraEntity> state) {
        CaracaraEntity entity = state.getAnimatable();

        if (!entity.isInWater() && !entity.onGround()) {
            if (this.isSwooping) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("dive"));
            } else {
                return state.setAndContinue(RawAnimation.begin().thenPlay("fly"));
            }
        }else if (!entity.isFlying() && state.isMoving()) {
            if (entity.isAggressive()){

                return state.setAndContinue(RawAnimation.begin().thenPlay("run"));
            }else {

                return state.setAndContinue(RawAnimation.begin().thenPlay("walk"));
            }
        }

        return state.setAndContinue(RawAnimation.begin().thenPlay("idle"));
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
                return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
            }
        }

        public boolean canUse() {
            return (!CaracaraEntity.this.isFlying() || !CaracaraEntity.this.wantsToFly) && super.canUse();
        }

        public boolean canContinueToUse() {
            return (!CaracaraEntity.this.isFlying() || !CaracaraEntity.this.wantsToFly) && super.canContinueToUse();
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target) && !this.isBaby();
    }

    static enum AttackPhase {
        CIRCLE,
        SWOOP;
    }

    @Override
    public void move(@NotNull MoverType type, @NotNull Vec3 pos) {
        super.move(type, pos);
    }

    @Override
    protected float getJumpPower() {
        return this.getJumpPower(1F);
    }

    class CaracaraFlyGoal extends Goal {
        private static final int WANDER_THRESHOLD = 22;

        CaracaraFlyGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return CaracaraEntity.this.canFly() && CaracaraEntity.this.navigation.isDone() && CaracaraEntity.this.random.nextInt(10) == 0;
        }

        public boolean canContinueToUse() {
            return CaracaraEntity.this.canFly() && CaracaraEntity.this.isFlying() && CaracaraEntity.this.navigation.isInProgress();
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
            CaracaraEntity.this.setFlying(false);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        //this.anchorPoint = this.blockPosition().above(5);

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }
}
