package chicken.creaturecorner.server.entity.obj.base;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Optional;
import java.util.UUID;

public abstract class GeoTamableEntity extends GeoEntityBase implements OwnableEntity {
    public static final int TELEPORT_WHEN_DISTANCE_IS_SQ = 144;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 1;
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID;
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID;
    public boolean orderedToSit;

    public GeoTamableEntity(EntityType<? extends GeoTamableEntity> entityType, Level level) {
        super(entityType, level);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte)0);
        builder.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getOwnerUUID() != null) {
            compound.putUUID("Owner", this.getOwnerUUID());
        }

        compound.putBoolean("Sitting", this.orderedToSit);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        UUID uuid;
        if (compound.hasUUID("Owner")) {
            uuid = compound.getUUID("Owner");
        } else {
            String s = compound.getString("Owner");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
                this.setTame(true, false);
            } catch (Throwable var4) {
                this.setTame(false, true);
            }
        }

        this.orderedToSit = compound.getBoolean("Sitting");
        this.setInSittingPose(this.orderedToSit);
    }

    public boolean isOrderedToSit() {
        return this.orderedToSit;
    }

    public void setOrderedToSit(boolean orderedToSit) {
        this.orderedToSit = orderedToSit;
    }

    public boolean canBeLeashed() {
        return true;
    }

    public boolean handleLeashAtDistance(Entity leashHolder, float distance) {
        if (this.isInSittingPose()) {
            if (distance > 10.0F) {
                this.dropLeash(true, true);
            }

            return false;
        } else {
            return super.handleLeashAtDistance(leashHolder, distance);
        }
    }

    protected void spawnTamingParticles(boolean tamed) {
        ParticleOptions particleoptions = ParticleTypes.HEART;
        if (!tamed) {
            particleoptions = ParticleTypes.SMOKE;
        }

        for(int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleoptions, this.getRandomX((double)1.0F), this.getRandomY() + (double)0.5F, this.getRandomZ((double)1.0F), d0, d1, d2);
        }

    }

    public void handleEntityEvent(byte id) {
        if (id == 7) {
            this.spawnTamingParticles(true);
        } else if (id == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(id);
        }

    }

    public boolean isTame() {
        return ((Byte)this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
    }

    public void setTame(boolean tame, boolean applyTamingSideEffects) {
        byte b0 = (Byte)this.entityData.get(DATA_FLAGS_ID);
        if (tame) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 4));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -5));
        }

        if (applyTamingSideEffects) {
            this.applyTamingSideEffects();
        }

    }

    protected void applyTamingSideEffects() {
    }

    public boolean isInSittingPose() {
        return ((Byte)this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setInSittingPose(boolean sitting) {
        byte b0 = (Byte)this.entityData.get(DATA_FLAGS_ID);
        if (sitting) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -2));
        }

    }

    public UUID getOwnerUUID() {
        return (UUID)((Optional)this.entityData.get(DATA_OWNERUUID_ID)).orElse((Object)null);
    }

    public void setOwnerUUID(UUID uuid) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
    }

    public void tame(Player player) {
        this.setTame(true, true);
        this.setOwnerUUID(player.getUUID());
        if (player instanceof ServerPlayer serverplayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger(serverplayer, this);
        }

    }

    public boolean canAttack(LivingEntity target) {
        return this.isOwnedBy(target) ? false : super.canAttack(target);
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity == this.getOwner();
    }

    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        return true;
    }

    public PlayerTeam getTeam() {
        if (this.isTame()) {
            LivingEntity livingentity = this.getOwner();
            if (livingentity != null) {
                return livingentity.getTeam();
            }
        }

        return super.getTeam();
    }

    public boolean isAlliedTo(Entity entity) {
        if (this.isTame()) {
            LivingEntity livingentity = this.getOwner();
            if (entity == livingentity) {
                return true;
            }

            if (livingentity != null) {
                return livingentity.isAlliedTo(entity);
            }
        }

        return super.isAlliedTo(entity);
    }

    public void die(DamageSource cause) {
        Component deathMessage = this.getCombatTracker().getDeathMessage();
        super.die(cause);
        if (this.dead && !this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
            this.getOwner().sendSystemMessage(deathMessage);
        }

    }

    public void tryToTeleportToOwner() {
        LivingEntity livingentity = this.getOwner();
        if (livingentity != null) {
            this.teleportToAroundBlockPos(livingentity.blockPosition());
        }

    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity livingentity = this.getOwner();
        return livingentity != null && this.distanceToSqr(this.getOwner()) >= (double)144.0F;
    }

    private void teleportToAroundBlockPos(BlockPos pos) {
        for(int i = 0; i < 10; ++i) {
            int j = this.random.nextIntBetweenInclusive(-3, 3);
            int k = this.random.nextIntBetweenInclusive(-3, 3);
            if (Math.abs(j) >= 2 || Math.abs(k) >= 2) {
                int l = this.random.nextIntBetweenInclusive(-1, 1);
                if (this.maybeTeleportTo(pos.getX() + j, pos.getY() + l, pos.getZ() + k)) {
                    return;
                }
            }
        }

    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.moveTo((double)x + (double)0.5F, (double)y, (double)z + (double)0.5F, this.getYRot(), this.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        PathType pathtype = WalkNodeEvaluator.getPathTypeStatic(this, pos);
        if (pathtype != PathType.WALKABLE) {
            return false;
        } else {
            BlockState blockstate = this.level().getBlockState(pos.below());
            if (!this.canFlyToOwner() && blockstate.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockpos = pos.subtract(this.blockPosition());
                return this.level().noCollision(this, this.getBoundingBox().move(blockpos));
            }
        }
    }

    public final boolean unableToMoveToOwner() {
        return this.isOrderedToSit() || this.isPassenger() || this.mayBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
    }

    protected boolean canFlyToOwner() {
        return false;
    }

    static {
        DATA_FLAGS_ID = SynchedEntityData.defineId(GeoTamableEntity.class, EntityDataSerializers.BYTE);
        DATA_OWNERUUID_ID = SynchedEntityData.defineId(GeoTamableEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    }

    public class TamableAnimalPanicGoal extends PanicGoal {
        public TamableAnimalPanicGoal(double speedModifier, TagKey<DamageType> panicCausingDamageTypes) {
            super(GeoTamableEntity.this, speedModifier, panicCausingDamageTypes);
        }

        public TamableAnimalPanicGoal(double speedModifier) {
            super(GeoTamableEntity.this, speedModifier);
        }

        public void tick() {
            if (!GeoTamableEntity.this.unableToMoveToOwner() && GeoTamableEntity.this.shouldTryTeleportToOwner()) {
                GeoTamableEntity.this.tryToTeleportToOwner();
            }

            super.tick();
        }
    }
}
