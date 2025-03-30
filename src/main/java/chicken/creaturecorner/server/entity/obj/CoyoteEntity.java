package chicken.creaturecorner.server.entity.obj;

import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.entity.obj.geo.GeoTamableEntity;
import chicken.creaturecorner.server.entity.obj.geo.goal.*;
import chicken.creaturecorner.server.entity.obj.goal.CoyoteHurtByTargetGoal;
import chicken.creaturecorner.server.entity.obj.goal.ModSitWhenOrdererdGoal;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
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
    private boolean attackedOnce;
    private boolean shouldAttackOnce;
    public int prevScratchTime;
    private final int scratchAnimTime = 83;
    private LookForFoodGoal forFoodGoal;
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME;
    private static final EntityDataAccessor<Boolean> IS_VARIANT;
    private static final EntityDataAccessor<Integer> SCRATCHING_TIME;
    private static final EntityDataAccessor<Boolean> SCRATCHING;

    public CoyoteEntity(EntityType<? extends CoyoteEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setTame(false, false);
        this.lookControl = new CoyoteLookControl(this);
        this.moveControl = new CoyoteMoveControl(this);
        this.setPathfindingMalus(PathType.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 0.0F);
    }

    protected void registerGoals() {
        this.forFoodGoal = new LookForFoodGoal(this, ItemTags.MEAT);
        this.goalSelector.addGoal(3, this.forFoodGoal);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new GeoTamableEntity.TamableAnimalPanicGoal((double) 1.5F, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, (double) 2.0F, true));
        this.goalSelector.addGoal(7, new BreedGoal(this, (double) 1.0F));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, (double) 1.0F) {
            public boolean canUse() {
                return !CoyoteEntity.this.orderedToSit && !CoyoteEntity.this.isScratching() ? super.canUse() : false;
            }

            public boolean canContinueToUse() {
                return !CoyoteEntity.this.orderedToSit && !CoyoteEntity.this.isScratching() ? super.canContinueToUse() : false;
            }
        });
        this.goalSelector.addGoal(9, new TemptGoal(this, (double) 1.1F, Ingredient.of(new ItemLike[]{Items.CHICKEN}), false) {
            public boolean canUse() {
                return CoyoteEntity.this.canMove() && super.canUse();
            }

            public boolean canContinueToUse() {
                return CoyoteEntity.this.canMove() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(3, (new CoyoteHurtByTargetGoal(this, new Class[0])).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, PigeonEntity.class, false, (living) -> this.isHungry()));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Sheep.class, false, (entity) -> entity.isBaby() && this.isHungry()));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Chicken.class, false, (living) -> this.isHungry()));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Rabbit.class, false, (living) -> this.isHungry()));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
        this.goalSelector.addGoal(2, new ModSitWhenOrdererdGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<Player>(this, Player.class, 6.0F, (double) 1.0F, 1.2) {
            public boolean canUse() {
                return super.canUse() && !CoyoteEntity.this.isAggressive() && !CoyoteEntity.this.isTame();
            }

            public boolean canContinueToUse() {
                return super.canContinueToUse() && !CoyoteEntity.this.isAggressive() && !CoyoteEntity.this.isTame();
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, (double) 10.0F).add(Attributes.ATTACK_DAMAGE, (double) 3.0F).add(Attributes.MOVEMENT_SPEED, 0.157).add(Attributes.ATTACK_KNOCKBACK, 0.8);
    }

    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.CHICKEN);
    }

    boolean canMove() {
        return !this.isScratching() && !this.orderedToSit;
    }

    public void tick() {
        super.tick();
        if (!this.isBaby() && !this.isAggressive()) {
            if (this.getRandom().nextInt(5000) == 0 && !this.isScratching() && this.onGround() && !this.orderedToSit && this.navigation.isDone()) {
                this.setScratchingTime(83);
            }

            if (this.getScratchingTime() > 0 && !this.orderedToSit) {
                this.goalSelector.getAvailableGoals().forEach(WrappedGoal::stop);
                this.getNavigation().stop();
                if (!this.isScratching()) {
                    this.setIsScratching(true);
                }

                this.prevScratchTime = this.getScratchingTime();
                this.setScratchingTime(this.prevScratchTime - 1);
            } else if (this.isScratching()) {
                this.setIsScratching(false);
            }
        }

    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_VARIANT, false);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
        builder.define(SCRATCHING_TIME, 0);
        builder.define(SCRATCHING, false);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setVariant(pCompound.getBoolean("IsVariant"));
        this.setScratchingTime(pCompound.getInt("scratchingTime"));
        this.setIsScratching(pCompound.getBoolean("isScratching"));
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("IsVariant", this.getVariant());
        pCompound.putInt("scratchingTime", this.getScratchingTime());
        pCompound.putBoolean("isScratching", this.isScratching());
    }

    public int getScratchingTime() {
        return (Integer) this.entityData.get(SCRATCHING_TIME);
    }

    public void setScratchingTime(int scratchingTime) {
        this.entityData.set(SCRATCHING_TIME, scratchingTime);
    }

    public boolean isScratching() {
        return (Boolean) this.entityData.get(SCRATCHING);
    }

    public void setIsScratching(boolean isScratching) {
        this.entityData.set(SCRATCHING, isScratching);
    }

    private void setVariant(boolean isVariant) {
        this.entityData.set(IS_VARIANT, isVariant);
    }

    public boolean getVariant() {
        return (Boolean) this.entityData.get(IS_VARIANT);
    }

    public void travel(Vec3 pTravelVector) {
        if (this.isScratching()) {
            if (this.getNavigation().isDone()) {
                this.getNavigation().stop();
            }

            super.travel(Vec3.ZERO);
        } else {
            super.travel(pTravelVector);
        }

    }

    public void swing(InteractionHand hand) {
        super.swing(hand);
        if (this.shouldAttackOnce) {
            this.shouldAttackOnce = false;
            this.attackedOnce = true;
            this.forgetCurrentTargetAndRefreshUniversalAnger();
        }

    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.level().isClientSide && (!this.isBaby() || !this.isFood(itemstack))) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || this.isFood(itemstack) && !this.isTame() && !this.isAngry();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else if (this.isTame()) {
            if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
                FoodProperties foodproperties = itemstack.getFoodProperties(this);
                float f = foodproperties != null ? (float) foodproperties.nutrition() : 1.0F;
                this.heal(2.0F * f);
                itemstack.consume(1, player);
                this.gameEvent(GameEvent.EAT);
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            } else {
                InteractionResult interactionresult = super.mobInteract(player, hand);
                if (!interactionresult.consumesAction() && this.isOwnedBy(player)) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget((LivingEntity) null);
                    return InteractionResult.SUCCESS_NO_ITEM_USED;
                } else {
                    return interactionresult;
                }
            }
        } else {
            return super.mobInteract(player, hand);
        }
    }

    public boolean canPickUpLoot() {
        return this.isHungry();
    }

    protected void starve() {
        this.triggerFoodSearch();
    }

    public boolean hasHunger() {
        return !this.isTame();
    }

    private void triggerFoodSearch() {
        if (this.forFoodGoal != null) {
            this.forFoodGoal.trigger();
        } else {
            this.navigation.stop();
            Predicate<ItemEntity> predicate = (p_25258_) -> p_25258_.getItem().is(ItemTags.MEAT);
            List<? extends ItemEntity> list = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate((double) 32.0F, (double) 8.0F, (double) 32.0F), predicate);
            if (!list.isEmpty()) {
                this.navigation.moveTo((Entity) list.getFirst(), 1.1);
            }
        }

    }

    public int maxFood() {
        return 100;
    }

    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        if (!(entity instanceof Player)) {
            this.killed(entity);
        }

        return super.killedEntity(level, entity);
    }

    public void killed(LivingEntity entity) {
        int food = this.getFoodLevel();
        if (entity instanceof Sheep) {
            this.setFoodLevel(food + 75);
        } else {
            this.setFoodLevel(food + 25);
        }

    }

    public void aiStep() {
        ItemStack stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (stack != null && stack.is(ItemTags.MEAT) && this.isHungry()) {
            this.setFoodLevel(this.getFoodLevel() + 50);
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), this.getX(), this.getY(), this.getZ(), (double) 0.0F, (double) 0.0F, (double) 0.0F);
            this.level().playSound((Player) null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EAT, SoundSource.AMBIENT);
            stack.setCount(0);
            this.setItemInHand(InteractionHand.MAIN_HAND, Items.AIR.getDefaultInstance());
        }

        if (this.isAlmostStarving() && (double) this.random.nextFloat() <= 0.2) {
            this.triggerFoodSearch();
        }

        Vec3 vec3 = this.getDeltaMovement();
        if ((this.isOrderedToSit() || this.isScratching()) && !this.navigation.isDone()) {
            this.setDeltaMovement(vec3.multiply((double) 0.0F, (double) 1.0F, (double) 0.0F));
        }

        super.aiStep();
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

    public boolean shouldTryTeleportToOwner() {
        return false;
    }

    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        AgeableMob ageablemob = this.getBreedOffspring(level, mate);
        BabyEntitySpawnEvent event = new BabyEntitySpawnEvent(this, mate, ageablemob);
        boolean cancelled = ((BabyEntitySpawnEvent) NeoForge.EVENT_BUS.post(event)).isCanceled();
        ageablemob = event.getChild();
        if (cancelled) {
            this.setAge(6000);
            mate.setAge(6000);
            this.resetLove();
            mate.resetLove();
        } else if (ageablemob != null) {
            ageablemob.setBaby(true);
            ageablemob.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
            if (ageablemob instanceof GeoTamableEntity) {
                GeoTamableEntity entity = (GeoTamableEntity) ageablemob;
                Player matePlayer = mate.getLoveCause();
                Player myPlayer = this.getLoveCause();
                if (matePlayer != null && myPlayer != null && matePlayer == myPlayer) {
                    entity.tame(myPlayer);
                }
            }

            this.finalizeSpawnChildFromBreeding(level, mate, ageablemob);
            level.addFreshEntityWithPassengers(ageablemob);
        }

    }

    public boolean canAttack(LivingEntity target) {
        return target.canBeSeenAsEnemy() && super.canAttack(target);
    }

    public boolean isAlliedTo(Entity entity) {
        return this.getLastHurtMob() == entity ? false : super.isAlliedTo(entity);
    }

    public @org.jetbrains.annotations.Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        CoyoteEntity baby = (CoyoteEntity) AnimalModEntities.COYOTE_TYPE.create(serverLevel);
        if (baby != null && ageableMob instanceof CoyoteEntity otherParent) {
            if (!this.getVariant() == otherParent.getVariant()) {
                if (baby != null) {
                    baby.setVariant(this.random.nextBoolean());
                }
            } else if (this.getVariant() && otherParent.getVariant()) {
                if (baby != null) {
                    baby.setVariant(true);
                }
            } else if (baby != null) {
                baby.setVariant(this.random.nextFloat() <= 0.15F);
            }
        }

        return baby;
    }

    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData) {

        Holder<Biome> holder = level.getBiome(this.blockPosition());

        if (holder.is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
            this.setVariant(this.getRandom().nextInt(0, 10) > 8);
        } else {
            this.setVariant(this.getRandom().nextInt(0, 100) > 98);
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public String getVariantName() {
        return this.getVariant() ? "white" : "orange";
    }

    public int getMaxHeadXRot() {
        return 16;
    }

    public int getMaxHeadYRot() {
        return 16;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController(this, "moveIdleController", 2, this::moveIdleController));
        controllers.add(new AnimationController(this, "tameController", 0, this::tameController));
    }

    private PlayState tameController(AnimationState<CoyoteEntity> state) {
        return !((CoyoteEntity) state.getAnimatable()).isDeadOrDying() && ((CoyoteEntity) state.getAnimatable()).isTame() && !((CoyoteEntity) state.getAnimatable()).isBaby() ? state.setAndContinue(RawAnimation.begin().thenLoop("tamed")) : PlayState.STOP;
    }

    private PlayState moveIdleController(AnimationState<CoyoteEntity> state) {
        if (!((CoyoteEntity) state.getAnimatable()).isDeadOrDying()) {
            if (this.isScratching() && !this.isBaby()) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("ear_scratch"));
            } else if (((CoyoteEntity) state.getAnimatable()).isInSittingPose()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("sit"));
            } else if (state.isMoving()) {
                return this.isAggressive() ? state.setAndContinue(RawAnimation.begin().thenLoop("run")) : state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            } else {
                return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
        } else {
            return PlayState.STOP;
        }
    }

    public int getRemainingPersistentAngerTime() {
        return (Integer) this.entityData.get(DATA_REMAINING_ANGER_TIME);
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

    public void checkOwnerHurt() {
        if (this.getLastHurtMob() != null && this.isOwnedBy(this.getLastHurtMob())) {
            this.attackedOnce = false;
        }

    }

    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && this.isTame() && !source.is(DamageTypes.THORNS)) {
            Entity var4 = source.getDirectEntity();
            if (var4 instanceof Player) {
                Player owner = (Player) var4;
                if (this.getOwner() == owner) {
                    if (!owner.getAbilities().instabuild) {
                        this.playSound(SoundEvents.PANDA_BITE);
                    }

                    owner.hurt(this.damageSources().thorns(this), 2.0F);
                }
            }
        }

        return super.hurt(source, amount);
    }

    static {
        DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(CoyoteEntity.class, EntityDataSerializers.INT);
        IS_VARIANT = SynchedEntityData.defineId(CoyoteEntity.class, EntityDataSerializers.BOOLEAN);
        SCRATCHING_TIME = SynchedEntityData.defineId(CoyoteEntity.class, EntityDataSerializers.INT);
        SCRATCHING = SynchedEntityData.defineId(CoyoteEntity.class, EntityDataSerializers.BOOLEAN);
    }

    public static class CoyoteLookControl extends LookControl {
        CoyoteEntity coyote;

        public CoyoteLookControl(CoyoteEntity pCoyote) {
            super(pCoyote);
            this.coyote = pCoyote;
        }

        public void tick() {
            if (!this.coyote.isScratching()) {
                super.tick();
            }

        }
    }

    static class CoyoteMoveControl extends MoveControl {
        CoyoteEntity coyote;

        public CoyoteMoveControl(CoyoteEntity pCoyote) {
            super(pCoyote);
            this.coyote = pCoyote;
        }

        public void tick() {
            if (this.coyote.canMove()) {
                super.tick();
            }

        }
    }
}
