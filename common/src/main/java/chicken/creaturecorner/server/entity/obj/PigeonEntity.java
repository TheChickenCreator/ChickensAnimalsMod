package chicken.creaturecorner.server.entity.obj;


import chicken.creaturecorner.server.entity.CCEntities;
import chicken.creaturecorner.server.entity.obj.control.AnimalFlyingMoveControl;
import chicken.creaturecorner.server.entity.obj.control.AnimalMoveControl;
import chicken.creaturecorner.server.entity.obj.control.GroundAnimalNavigation;
import chicken.creaturecorner.server.entity.obj.geo.GeoEntityBase;
import chicken.creaturecorner.server.entity.obj.goal.PigeonFlockFollowLeader;
import chicken.creaturecorner.server.entity.obj.goal.PigeonPanicGoal;
import chicken.creaturecorner.server.sound.CCSounds;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PigeonEntity extends GeoEntityBase {
    public PigeonEntity leader;
    private int schoolSize = 1;
    private boolean wantsToFly;

    protected static final RawAnimation PIGEON_IDLE = RawAnimation.begin().thenLoop("animation.pigeon.idle");
    protected static final RawAnimation PIGEON_WALK = RawAnimation.begin().thenLoop("animation.pigeon.walk");
    protected static final RawAnimation PIGEON_PANIC = RawAnimation.begin().thenLoop("animation.pigeon.run");
    protected static final RawAnimation PIGEON_FLY = RawAnimation.begin().thenLoop("animation.pigeon.fly");

    private static final EntityDataAccessor<Integer> FLY_TICKS = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PANIC = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.BOOLEAN);

    public PigeonEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0);
        setFlying(false);
        this.wantsToFly = false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PigeonPanicGoal(this));
        this.goalSelector.addGoal(3, new PigeonFlockFollowLeader(this));
        this.goalSelector.addGoal(4, new PigeonWaterAvoidingRandomStrollGoal(this, 1.0F));

        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0, 1.2));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, CoyoteEntity.class, 6.0F, 1.0, 1.2));

        this.goalSelector.addGoal(2, new BreedGoal(this, 1, PigeonEntity.class));
        this.goalSelector.addGoal(8, new PigeonFlyGoal());
    }

    class PigeonWaterAvoidingRandomStrollGoal extends RandomStrollGoal{

        public static final float PROBABILITY = 0.001F;
        protected final float probability;

        public PigeonWaterAvoidingRandomStrollGoal(PathfinderMob mob, double speedModifier) {
            this(mob, speedModifier, 0.001F);
        }

        public PigeonWaterAvoidingRandomStrollGoal(PathfinderMob mob, double speedModifier, float probability) {
            super(mob, speedModifier, 120, false);
            this.probability = probability;
        }

        
        protected Vec3 getPosition() {
            if (this.mob.isInWaterOrBubble()) {
                Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
                return vec3 == null ? super.getPosition() : vec3;
            } else {
                return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
            }
        }

        public boolean canUse() {
            return (!PigeonEntity.this.isFlying() || !PigeonEntity.this.wantsToFly) && super.canUse();
        }

        public boolean canContinueToUse() {
            return (!PigeonEntity.this.isFlying() || !PigeonEntity.this.wantsToFly) && super.canContinueToUse();
        }

    }

    private void switchNavigator(boolean onLand) {
        if (onLand || this.isBaby()) {
            this.moveControl = new MoveControl(this);
            this.navigation = new GroundPathNavigation(this, level());
        } else {
            this.moveControl = new FlyingMoveControl(this, 32, false); //new FlightMoveController(this, 0.7F, false);
            this.navigation = new FlyingPathNavigation(this, level());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLY_TICKS, 0);
        builder.define(VARIANT, 0);
        builder.define(PANIC, false);
        builder.define(FLYING, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("FlyTicks", getFlyTicks());
        compound.putInt("Variant", getVariant());
        compound.putBoolean("Panic", getPanic());
        compound.putBoolean("IsFlying", isFlying());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setFlyTicks(compound.getInt("FlyTicks"));
        this.setVariant(compound.getInt("Variant"));
        this.setPanic(compound.getBoolean("Panic"));
        this.setFlying(compound.getBoolean("IsFlying"));
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
        switchNavigator(!flight);
    }

    @Override
    public String getVariantName() {

        return switch (getVariant()) {
            case 1 -> "white";
            case 2 -> "red";
            default -> "grey";
        };
    }

    public void startFollowing(PigeonEntity leader) {
        if (!this.hasFollowers()){
            this.leader = leader;
            leader.addFollower();
        }
    }

    public void stopFollowing() {
        assert this.leader != null;
        this.leader.removeFollower();
        this.leader = null;
    }

    private void addFollower() {
        ++this.schoolSize;
    }

    private void removeFollower() {
        --this.schoolSize;
    }

    public boolean canBeFollowed() {
        return !this.isFollower() && this.hasFollowers() && this.schoolSize < this.getMaxSchoolSize() && !this.isBaby();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.FLYING_SPEED, 0.4F)
                .add(Attributes.MOVEMENT_SPEED, 0.2F).add(Attributes.MAX_HEALTH, 5);
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
    public void aiStep() {
        if(this.getLastDamageSource() != null) {
            if(!this.getPanic()) {
                this.getNavigation().stop();
                this.setPanic(true);
            }
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isFlying() && !this.onGround()){
            if (this.random.nextInt(10)==0){
                if (!findGroundPosition()){
                    this.setFlyTicks(0);
                }
            }
        }

        if (this.isFollower() && this.leader!=null && this.random.nextInt(50)==0){

            if (!this.isTooCloseToLeader()){
                if (this.leader.wantsToFly && this.getFlyTicks()<500){
                    this.setFlyTicks(500);
                }

                if (!this.leader.wantsToFly && this.getFlyTicks()>0){
                    this.setFlyTicks(0);
                }
            }

        }

        int prevFlyTicks = this.getFlyTicks();

        if (!this.wantsToFly && this.isFlying()){
            this.setFlying(false);
        }

        if (this.getFlyTicks()<=0){
            this.wantsToFly = false;
        }else if (this.getFlyTicks()>=500){
            this.wantsToFly = true;
        }

        if (this.wantsToFly && !this.isFlying()){
            this.setFlyTicks(prevFlyTicks-1);
        }else if (!this.wantsToFly){
            this.setFlyTicks(prevFlyTicks+1);
        }

        if(!this.onGround() && !this.isFlying() && this.getDeltaMovement().y<0) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.75F, 1));
        }

        if (this.hasFollowers() && this.level().random.nextInt(200) == 1) {
            List<? extends PigeonEntity> list = this.level()
                    .getEntitiesOfClass((Class<? extends PigeonEntity>)this.getClass(), this.getBoundingBox().inflate(8.0, 8.0, 8.0));
            if (list.size() <= 1) {
                this.schoolSize = 1;
            }
        }
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
    public boolean isFollower() {
        return this.leader != null && this.leader.isAlive();
    }


    public void addFollowers(Stream<? extends PigeonEntity> followers) {
        followers.limit((long)(this.getMaxSchoolSize() - this.schoolSize))
                .filter(p_27538_ -> p_27538_ != this)
                .forEach(p_27536_ -> p_27536_.startFollowing(this));
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
    public int getMaxSpawnClusterSize() {
        return this.getMaxSchoolSize();
    }

    public int getMaxSchoolSize() {
        return 10;
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
    protected float getJumpPower() {
        return this.getJumpPower(1F);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
    }

    
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        PigeonEntity entity = CCEntities.PIGEON_TYPE.get().create(serverLevel);
        if(entity != null) {

            //entity.setVariant(this.random.nextInt(3));

            if(ageableMob instanceof PigeonEntity otherParent) {
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "moveFlyIdleController", 2, this::moveFlyController));
    }

    private PlayState moveFlyController(AnimationState<PigeonEntity> state) {
        PigeonEntity entity = state.getAnimatable();
        if (entity.onGround()) {
            if(state.isMoving()) return state.setAndContinue(entity.getPanic() ? PIGEON_PANIC : PIGEON_WALK);
        } else if (!entity.isInWater() && !entity.onGround()) {
            return state.setAndContinue(PIGEON_FLY);
        }
        return state.setAndContinue(PIGEON_IDLE);
    }

    public static boolean spawnRules(EntityType<PigeonEntity> pigeonEntityEntityType, ServerLevelAccessor serverLevelAccessor, MobSpawnType mobSpawnType, BlockPos pos, RandomSource randomSource) {
        boolean canContinue = serverLevelAccessor.isEmptyBlock(pos);

        boolean isAir = serverLevelAccessor.getBlockState(pos.below()).isAir();

        boolean flag = MobSpawnType.ignoresLightRequirements(mobSpawnType) || isBrightEnoughToSpawn(serverLevelAccessor, pos);

        if(canContinue) {
            Optional<ResourceKey<Biome>> biomeKey = serverLevelAccessor.getBiome(pos).unwrapKey();
            if(biomeKey.isPresent()) {
                ResourceKey<Biome> resourceKey = biomeKey.get();
                if(resourceKey == Biomes.END_MIDLANDS || resourceKey == Biomes.END_BARRENS || resourceKey == Biomes.END_HIGHLANDS || resourceKey == Biomes.SMALL_END_ISLANDS) {
                    StructureManager manager = serverLevelAccessor.getLevel().structureManager();
                    if (manager.hasAnyStructureAt(pos)) {
                        Map<Structure, LongSet> structures = manager.getAllStructuresAt(pos);
                        for (Structure structure : structures.keySet()) {
                            if (structure.type() == StructureType.END_CITY) {
                                return !isAir;
                            }
                        }
                    }
                    return false;
                } else if(serverLevelAccessor.getBiomeManager().getBiome(pos).is(BiomeTags.IS_MOUNTAIN)) {
                    return (serverLevelAccessor.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && flag);

                } else {
                    return serverLevelAccessor.getLevel().isVillage(pos);
                }
            }
        }
        return false;
    }

    public static class SchoolSpawnGroupData extends AgeableMobGroupData {
        public final PigeonEntity leader;
        public SchoolSpawnGroupData(boolean shouldSpawnBaby, PigeonEntity pigeonEntity) {
            super(shouldSpawnBaby);
            this.leader = pigeonEntity;
        }
    }

    class PigeonFlyGoal extends Goal {
        private static final int WANDER_THRESHOLD = 22;

        PigeonFlyGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return PigeonEntity.this.wantsToFly && PigeonEntity.this.navigation.isDone() && PigeonEntity.this.random.nextInt(10) == 0;
        }

        public boolean canContinueToUse() {
            return PigeonEntity.this.isFlying() && PigeonEntity.this.navigation.isInProgress();
        }

        public void start() {
            PigeonEntity.this.setFlying(true);
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                PigeonEntity.this.navigation.moveTo(PigeonEntity.this.navigation.createPath(BlockPos.containing(vec3), 1), 1.0);
            }

        }

        
        private Vec3 findPos() {
            Vec3 vec3;

            vec3 = PigeonEntity.this.getViewVector(0.0F);

            Vec3 vec32 = HoverRandomPos.getPos(PigeonEntity.this, 8, 7, vec3.x, vec3.z, 1.5707964F, 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(PigeonEntity.this, 8, 4, -2, vec3.x, vec3.z, 1.5707963705062866);
        }

        @Override
        public void stop() {
            super.stop();
            PigeonEntity.this.setFlying(false);
        }
    }

    private Boolean findGroundPosition() {
        BlockPos blockpos = null;

        for(int i = 0; i < 10; i++) {
            blockpos = new BlockPos((int) this.getX(), (int) (this.getY()-i), (int) this.getZ());
            if (!this.isAir(this.level(), blockpos)) {
                return true;
            }
        }

        return false;
    }

    private void moveDown(){
        this.getNavigation().moveTo(this.getX(), this.getY() - 8.0D, this.getZ(), 1.1D);
    }

    private boolean isAir(LevelReader pLevel, BlockPos pPos) {
        BlockState blockstate = pLevel.getBlockState(pPos);
        return (blockstate.is(Blocks.AIR));
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && this.isDeadOrDying() && this.isFollower()){
            if (this.leader != null){
                this.leader.removeFollower();
            }
        }
        super.remove(reason);
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

}
