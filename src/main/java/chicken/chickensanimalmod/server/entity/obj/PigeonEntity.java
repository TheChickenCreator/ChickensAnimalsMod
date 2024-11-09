package chicken.chickensanimalmod.server.entity.obj;

import chicken.chickensanimalmod.server.entity.AnimalModEntities;
import chicken.chickensanimalmod.server.entity.obj.geo.GeoEntityBase;
import chicken.chickensanimalmod.server.entity.obj.goal.PigeonFlockFollowLeader;
import chicken.chickensanimalmod.server.entity.obj.goal.PigeonFlyGoal;
import chicken.chickensanimalmod.server.entity.obj.goal.PigeonPanicGoal;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PigeonEntity extends GeoEntityBase {
    @javax.annotation.Nullable
    private PigeonEntity leader;
    private int schoolSize = 1;

    protected static final RawAnimation PIGEON_IDLE = RawAnimation.begin().thenLoop("animation.pigeon.idle");
    protected static final RawAnimation PIGEON_WALK = RawAnimation.begin().thenLoop("animation.pigeon.walk");
    protected static final RawAnimation PIGEON_PANIC = RawAnimation.begin().thenLoop("animation.pigeon.run");
    protected static final RawAnimation PIGEON_FLY = RawAnimation.begin().thenLoop("animation.pigeon.fly");

    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PANIC = SynchedEntityData.defineId(PigeonEntity.class, EntityDataSerializers.BOOLEAN);

    protected final FlyingPathNavigation flyingPathNavigation;
    protected final FlyingMoveControl flyingMoveControl;

    protected final GroundPathNavigation groundPathNavigation;
    protected final MoveControl groundMoveControl;

    public PigeonEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0);
        this.flyingPathNavigation = createNavigation(level);
        this.flyingMoveControl = new FlyingMoveControl(this, 32, false);
        this.groundPathNavigation = new GroundPathNavigation(this, level);
        this.groundMoveControl = new MoveControl(this);

        setFlying(false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(PANIC, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("animalmod.pigeon.variantid", getVariant());
        compound.putBoolean("animalmod.pigeon.panic", getPanic());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(compound.getInt("animalmod.pigeon.variantid"));
        this.setPanic(compound.getBoolean("animalmod.pigeon.panic"));
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

    @Override
    public String getVariantName() {
        return switch (getVariant()) {
            case 1 -> "white";
            case 2 -> "red";
            case 3 -> "end";
            default -> "";
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
        return this.hasFollowers() && this.schoolSize < this.getMaxSchoolSize();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.FLYING_SPEED, 0.4F).add(Attributes.MOVEMENT_SPEED, 0.2F);
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

    private boolean isFlying() {
        return this.navigation == flyingPathNavigation;
    }

    public boolean hasFollowers() {
        return this.schoolSize > 1;
    }

    public boolean inRangeOfLeader() {
        assert this.leader != null;
        return this.distanceToSqr(this.leader) <= 121.0;
    }

    public boolean shouldMoveToLeader() {
        if (!inRangeOfLeader()) return false;
        assert this.leader != null;
        return this.distanceToSqr(this.leader) >= 2;
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
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new PigeonPanicGoal(this));
        this.goalSelector.addGoal(1, new PigeonFlockFollowLeader(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1, PigeonEntity.class));
        this.goalSelector.addGoal(3, new PigeonFlyGoal(this, 1.0F));
        this.goalSelector.addGoal(5, new AvoidEntityGoal<>(this, LivingEntity.class, 20, 1, 1));
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
}
