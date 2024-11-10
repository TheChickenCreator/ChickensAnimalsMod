package chicken.creaturecorner.server.entity.obj;

import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.entity.obj.geo.GeoTamableEntity;
import chicken.creaturecorner.server.entity.obj.geo.goal.*;
import chicken.creaturecorner.server.entity.obj.goal.CoyoteHurtByTargetGoal;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class CoyoteEntity extends GeoTamableEntity implements NeutralMob {
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    @javax.annotation.Nullable
    private UUID persistentAngerTarget;
    @Getter @Setter
    private boolean attackedOnce;
    @Getter @Setter
    private boolean shouldAttackOnce;

    private LookForFoodGoal forFoodGoal;

    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(CoyoteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_VARIANT = SynchedEntityData.defineId(CoyoteEntity.class, EntityDataSerializers.BOOLEAN);
    public CoyoteEntity(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setPathfindingMalus(PathType.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 0.0F);
    }

    public static AttributeSupplier.Builder  createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.157);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.CHICKEN);
    }

    @Override
    public boolean hasChildModel() {
        return true;
    }

    @Override
    public boolean childVariants() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
//        if(this.isOrderedToSit() && random.nextInt(0, 250) == 50) {
//            this.setOrderedToSit(false);
//        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_VARIANT, false);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setVariant(pCompound.getBoolean("IsVariant"));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("IsVariant", getVariant());
    }

    private void setVariant(boolean isVariant) {
        this.entityData.set(IS_VARIANT, isVariant);
    }

    public boolean getVariant() {
        return this.entityData.get(IS_VARIANT);
    }

    @Override
    public void swing(InteractionHand hand) {
        super.swing(hand);
        if(shouldAttackOnce) {
            this.shouldAttackOnce = false;
            this.attackedOnce = true;
            this.forgetCurrentTargetAndRefreshUniversalAnger();
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!this.level().isClientSide || this.isBaby() && this.isFood(itemstack)) {
            if (this.isTame()) {
                if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                    FoodProperties foodproperties = itemstack.getFoodProperties(this);
                    float f = foodproperties != null ? (float)foodproperties.nutrition() : 1.0F;
                    this.heal(2.0F * f);
                    itemstack.consume(1, player);
                    this.gameEvent(GameEvent.EAT); // Neo: add EAT game event
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                } else {
                    InteractionResult interactionresult = super.mobInteract(player, hand);
                    if (!interactionresult.consumesAction() && this.isOwnedBy(player)) {
//                        this.setOrderedToSit(!this.isOrderedToSit());
                        this.jumping = false;
                        this.navigation.stop();
                        this.setTarget(null);
                        return InteractionResult.SUCCESS_NO_ITEM_USED;
                    } else {
                        return interactionresult;
                    }
                }
            } else {
                return super.mobInteract(player, hand);
            }
        } else {
            boolean flag = this.isOwnedBy(player) || this.isTame() && !this.isTame() && !this.isAngry();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
    }

    @Override
    protected void registerGoals() {
        this.forFoodGoal = new LookForFoodGoal(this, ItemTags.MEAT);
        this.goalSelector.addGoal(0, forFoodGoal);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GeoTamableEntity.TamableAnimalPanicGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(2, new GeoSitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.6, true));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new TemptGoal(this, 1.1F, (itemstack) -> itemstack.is(Items.CHICKEN), false));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(3, (new CoyoteHurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, PigeonEntity.class, false));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Sheep.class, false, (entity -> entity.isBaby() && isHungry())));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Pig.class, false, (entity -> entity.isBaby() && isHungry())));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Cow.class, false, (entity -> entity.isBaby() && isHungry())));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Chicken.class, false, (living) -> isHungry()));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public boolean canPickUpLoot() {
        return isHungry();
    }

    @Override
    protected void starve() {
        super.starve();
        triggerFoodSearch();
    }

    private void triggerFoodSearch() {
        if(forFoodGoal != null) {
            forFoodGoal.trigger();
        } else {
            navigation.stop();
            Predicate<ItemEntity> predicate = (p_25258_) -> p_25258_.getItem().is(ItemTags.MEAT);
            List<? extends ItemEntity> list = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(32, 8.0, 32), predicate);
            if(!list.isEmpty()) {
                navigation.moveTo(list.getFirst(), 1.1);
            }
        }
    }

    @Override
    public int maxFood() {
        return 1000;
    }

    @Override
    public void aiStep() {
        ItemStack stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if(stack != null) {
            if(stack.is(ItemTags.MEAT)) {
                if(isHungry()) {
                    setFoodLevel(getFoodLevel()+50);
                    level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), getX(), getY(), getZ(), 0, 0, 0);
                    level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EAT, SoundSource.AMBIENT);
                    stack.setCount(0);
                    this.setItemInHand(InteractionHand.MAIN_HAND, Items.AIR.getDefaultInstance());
                }
            }
        }
        if(isAlmostStarving() && random.nextFloat() <= 0.2) {
            triggerFoodSearch();
        }
        super.aiStep();
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        if(item.is(ItemTags.MEAT)) {
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

    @Override
    public boolean shouldTryTeleportToOwner() {
        return false;
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        AgeableMob ageablemob = this.getBreedOffspring(level, mate);
        BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(this, mate, ageablemob);
        boolean cancelled = NeoForge.EVENT_BUS.post(event).isCanceled();
        ageablemob = event.getChild();
        if (cancelled) {
            this.setAge(6000);
            mate.setAge(6000);
            this.resetLove();
            mate.resetLove();
        } else {
            if (ageablemob != null) {
                ageablemob.setBaby(true);
                ageablemob.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                if(ageablemob instanceof GeoTamableEntity entity) {
                    Player matePlayer = mate.getLoveCause();
                    Player myPlayer = getLoveCause();
                    if (matePlayer != null && myPlayer != null) {
                        if (matePlayer == myPlayer) {
                            entity.tame(myPlayer);
                        }
                    }
                }
                this.finalizeSpawnChildFromBreeding(level, mate, ageablemob);
                level.addFreshEntityWithPassengers(ageablemob);
            }
        }
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        return target.canBeSeenAsEnemy();
    }

    @Override
    public boolean isAlliedTo(@NotNull Entity entity) {
        if(this.getLastHurtMob() == entity) {
            return false;
        }
        return super.isAlliedTo(entity);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        CoyoteEntity entity = AnimalModEntities.COYOTE_TYPE.create(serverLevel);
        if(entity != null) entity.setVariant(random.nextFloat() <= 0.15F);
        return entity;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(this.random.nextBoolean());
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public String getVariantName() {
        return getVariant() ? "white" : "orange";
    }

    @Override
    public int getMaxHeadXRot() {
        return 16;
    }

    @Override
    public int getMaxHeadYRot() {
        return 16;
    }

    @Override
    public double getBoneResetTime() {
        return 8;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "tameController", 4, this::tameController));
        controllers.add(new AnimationController<>(this, "moveIdleController", 8, this::moveIdleController));
    }

    private PlayState tameController(AnimationState<CoyoteEntity> state) {
        if(!state.getAnimatable().isDeadOrDying()) {
            if(state.getAnimatable().isTame() && !state.getAnimatable().isBaby()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("tamed"));
            }
        } else return PlayState.STOP;
        return PlayState.CONTINUE;
    }

    private PlayState moveIdleController(AnimationState<CoyoteEntity> state) {
        if(!state.getAnimatable().isDeadOrDying()) {
            if(state.getAnimatable().isInSittingPose()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("sit"));
            } else if(state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            } else {
                return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
        } else return PlayState.STOP;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID uuid) {
        this.persistentAngerTarget = uuid;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public void checkOwnerHurt() {
        if(this.getLastHurtMob() != null) {
            if(this.isOwnedBy(this.getLastHurtMob())) {
                this.attackedOnce = false;
            }
        }
    }
}
