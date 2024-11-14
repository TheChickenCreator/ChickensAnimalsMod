package chicken.creaturecorner.server.entity.obj;

import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.entity.obj.geo.GeoEntityBase;
import chicken.creaturecorner.server.entity.obj.goal.PigeonFlockFollowLeader;
import chicken.creaturecorner.server.entity.obj.goal.PigeonPanicGoal;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.*;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PigeonEntity extends GeoEntityBase {
    @javax.annotation.Nullable
    private PigeonEntity leader;
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
        this.goalSelector.addGoal(0, new PigeonPanicGoal(this));
        this.goalSelector.addGoal(5, new PigeonFlockFollowLeader(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0F){
            @Override
            public boolean canUse() {
                return !PigeonEntity.this.isFlying() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !PigeonEntity.this.isFlying() && super.canContinueToUse();
            }
        });

        this.goalSelector.addGoal(2, new BreedGoal(this, 1, PigeonEntity.class));
        this.goalSelector.addGoal(8, new PigeonFlyGoal());

        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, CoyoteEntity.class, 20F, 1.0, 1.2));
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
            case 3 -> "end";
            default -> "grey";
        };
    }

    public void startFollowing(PigeonEntity leader) {
        this.leader = leader;
        leader.addFollower();
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
        return this.hasFollowers() && this.schoolSize < this.getMaxSchoolSize() && !this.isBaby();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.FLYING_SPEED, 0.4F).add(Attributes.MOVEMENT_SPEED, 0.2F);
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
    public boolean isSensitiveToWater() {
        return getVariant() == 3;
    }

    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        if(getVariant() != 3) return super.hurt(pSource, pAmount);
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            boolean flag = pSource.getDirectEntity() instanceof ThrownPotion;
            if (!pSource.is(DamageTypeTags.IS_PROJECTILE) && !flag) {
                boolean flag2 = super.hurt(pSource, pAmount);
                if (!this.level().isClientSide() && !(pSource.getEntity() instanceof LivingEntity) && this.random.nextInt(10) != 0) {
                    this.teleport();
                }

                return flag2;
            } else {
                boolean flag1 = flag && this.hurtWithCleanWater(pSource, (ThrownPotion)pSource.getDirectEntity(), pAmount);

                for(int i = 0; i < 64; ++i) {
                    if (this.teleport()) {
                        return true;
                    }
                }

                return flag1;
            }
        }
    }

    private boolean hurtWithCleanWater(DamageSource source, ThrownPotion potion, float amount) {
        ItemStack itemstack = potion.getItem();
        PotionContents potioncontents = itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return potioncontents.is(Potions.WATER) && super.hurt(source, amount);
    }


    @Override
    public void aiStep() {
        if(this.getVariant() == 3) {
            if (this.level().isClientSide) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5), this.getRandomY() - 0.25, this.getRandomZ(0.5), (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
            }
        }
        if(this.getLastDamageSource() != null) {
            if(!this.getPanic()) {
                if(this.getVariant() == 3) {
                    teleport();
                }
                this.getNavigation().stop();
                this.setPanic(true);
            }
        }
        super.aiStep();
    }

    protected boolean teleport() {
        if (!this.level().isClientSide() && this.isAlive()) {
            double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * 64.0D;
            double d1 = this.getY() + (double)(this.random.nextInt(64) - 32);
            double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * 64.0D;
            return this.teleport(d0, d1, d2);
        } else {
            return false;
        }
    }

    private boolean teleport(double pX, double pY, double pZ) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(pX, pY, pZ);

        while(blockpos$mutableblockpos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(blockpos$mutableblockpos).blocksMotion()) {
            blockpos$mutableblockpos.move(Direction.DOWN);
        }

        BlockState blockstate = this.level().getBlockState(blockpos$mutableblockpos);
        boolean flag = blockstate.blocksMotion();
        boolean flag1 = blockstate.getFluidState().is(FluidTags.WATER);
        if (flag && !flag1) {
            Vec3 vec3 = this.position();
            boolean flag2 = this.randomTeleport(pX, pY, pZ, true);
            if (flag2) {
                this.level().gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(this));
                if (!this.isSilent()) {
                    this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                }
            }

            return flag2;
        } else {
            return false;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isFlying() && !this.onGround() && this.isFollower()){
            if (this.random.nextInt(10)==0){
                if (!findGroundPosition()){
                    this.moveDown();
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
            List<? extends PigeonEntity> list = this.level().getEntitiesOfClass(this.getClass(), this.getBoundingBox().inflate(8.0, 8.0, 8.0));
            if (list.size() <= 1) {
                this.schoolSize = 1;
            }
            if(list.size() >= this.schoolSize) {
                for (PigeonEntity entity : list) {
                    if(entity != this) {
                        if(entity.leader != this) {
                            if (entity.hasFollowers()) {
                                entity.schoolSize = 1;
                            }
                            if(entity.leader != null) {
                                entity.leader.schoolSize -=1;
                            }
                            entity.leader = this;
                            this.schoolSize += 1;
                        }
                    }
                }
            }
        }
    }


    public boolean hasFollowers() {
        return this.schoolSize > 1;
    }

    public boolean inRangeOfLeader() {
        return this.distanceToSqr(this.leader) <= 75.0;
    }

    public boolean isTooCloseToLeader() {
        return this.distanceToSqr(this.leader) >= 3;
    }

    public boolean shouldMoveToLeader() {
        if (!inRangeOfLeader()) return false;
        assert this.leader != null;
        return this.distanceToSqr(this.leader) >= 3;
    }

    public void pathToLeader() {
        if (this.isFollower()) {
            assert this.leader != null;
            this.getNavigation().moveTo(this.leader, 1.0);
        }

    }
    public boolean isFollower() {
        return this.leader != null && this.leader.isAlive();
    }

    public void addFollowers(Stream<? extends PigeonEntity> followers) {
        followers.limit(this.getMaxSchoolSize() - this.schoolSize).filter((p_27538_) -> p_27538_ != this).forEach((p_27536_) -> {
            p_27536_.startFollowing(this);
        });
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        if (spawnGroupData == null) {
            spawnGroupData = new SchoolSpawnGroupData(true, this);
        } else {
            this.startFollowing(((PigeonEntity.SchoolSpawnGroupData)spawnGroupData).leader);
        }
        if(level().dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
            this.setVariant(3);
        } else {
            this.setVariant(this.random.nextInt(3));
        }

        return spawnGroupData;
    }

    public int getMaxSchoolSize() {
        return 1000;
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

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        PigeonEntity entity = AnimalModEntities.PIGEON_TYPE.create(serverLevel);
        if(entity != null) {
            if(ageableMob instanceof PigeonEntity entity1) {
                if(entity1.getVariant() == 3) {
                    entity.setVariant(3);
                }
            }

            entity.setBaby(true);
        }
        return entity;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "moveFlyIdleController", 1, this::moveFlyController));
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

        @javax.annotation.Nullable
        private Vec3 findPos() {
            Vec3 vec3;
//            if (PigeonEntity.this.isHiveValid() && !PigeonEntity.this.closerThan(PigeonEntity.this.hivePos, 22)) {
//                Vec3 vec31 = Vec3.atCenterOf(PigeonEntity.this.hivePos);
//                vec3 = vec31.subtract(PigeonEntity.this.position()).normalize();
//            } else {
                vec3 = PigeonEntity.this.getViewVector(0.0F);
            //}

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
}
