package chicken.creaturecorner.server.entity.obj.control;

import chicken.creaturecorner.server.entity.obj.geo.GeoEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class AnimalMoveControl extends MoveControl {
    protected final int MAX_TURN;
    protected Operation operation = Operation.WAIT;

    public AnimalMoveControl(Mob pMob, int turnRate) {
        super(pMob);
        this.MAX_TURN = turnRate;
    }

    /**
     * Sets the speed and location to move to
     */
    public void setWantedPosition(double pX, double pY, double pZ, double pSpeed) {
        this.wantedX = pX;
        this.wantedY = pY;
        this.wantedZ = pZ;
        this.speedModifier = pSpeed;
        if (this.operation != Operation.JUMPING) {
            this.operation = Operation.MOVE_TO;
        }
    }

    public void strafe(float pForward, float pStrafe) {
        this.operation = Operation.STRAFE;
        this.strafeForwards = pForward;
        this.strafeRight = pStrafe;
        this.speedModifier = 0.25;
    }

    public void tick() {
        switch (this.operation) {
            case STRAFE:
                handleStrafing();
                break;
            case MOVE_TO:
                handleMoveTo();
                break;
            case JUMPING:
                handleJumping();
                break;
            default:
                this.mob.setZza(0.0F);
        }

        avoidEntityCollision();
    }

    private void handleStrafing() {
        float speed = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * (float) this.speedModifier;
        float forward = this.strafeForwards;
        float right = this.strafeRight;
        float norm = Mth.sqrt(forward * forward + right * right);
        if (norm < 1.0F) {
            norm = 1.0F;
        }
        norm = speed / norm;
        forward *= norm;
        right *= norm;

        float sin = Mth.sin(this.mob.getYRot() * (float) (Math.PI / 180.0));
        float cos = Mth.cos(this.mob.getYRot() * (float) (Math.PI / 180.0));
        float dX = forward * cos - right * sin;
        float dZ = right * cos + forward * sin;

        if (!isWalkable(dX, dZ)) {
            adjustStrafing();
        }

        this.mob.setSpeed(speed);
        this.mob.setZza(this.strafeForwards);
        this.mob.setXxa(this.strafeRight);
        this.operation = Operation.WAIT;
    }

    private void handleMoveTo() {
        this.operation = Operation.WAIT;
        double dX = this.wantedX - this.mob.getX();
        double dY = this.wantedY - this.mob.getY();
        double dZ = this.wantedZ - this.mob.getZ();
        double distanceSquared = dX * dX + dY * dY + dZ * dZ;
        if (distanceSquared < 2.5000003E-7F) {
            this.mob.setZza(0.0F);
            return;
        }

        float targetAngle = (float) (Mth.atan2(dZ, dX) * 180.0F / Math.PI) - 90.0F;
        this.mob.setYRot(this.rotlerp(this.mob.getYRot(), targetAngle, MAX_TURN));
        this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));

        if (shouldJump(dY, dX, dZ)) {
            this.mob.getLookControl().setLookAt(this.wantedX, this.wantedY, this.wantedZ);
            this.mob.getJumpControl().jump();
            this.operation = Operation.JUMPING;
        }
    }

    private void handleJumping() {
        this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
        if (this.mob.onGround()) {
            this.operation = Operation.WAIT;
        }
    }

    private boolean shouldJump(double dY, double dX, double dZ) {
        BlockPos blockPos = this.mob.blockPosition();
        BlockState blockState = this.mob.level().getBlockState(blockPos);
        VoxelShape shape = blockState.getCollisionShape(this.mob.level(), blockPos);
        GeoEntityBase base = (GeoEntityBase) mob;
        double size = base.getDimensions(null).width() + 0.1;
        return (dY > this.mob.maxUpStep() && dX *dX + dZ * dZ <= size)
                || (!shape.isEmpty() && this.mob.getY() < shape.max(Direction.Axis.Y) + blockPos.getY() && !blockState.is(BlockTags.DOORS) && !blockState.is(BlockTags.FENCES) && !blockState.is(BlockTags.FENCE_GATES));
    }

    /**
     * Adjusts strafing behavior to avoid obstacles and continue smooth movement.
     */
    private void adjustStrafing() {
        // Slightly modify directions to try different paths and avoid getting stuck.
        this.strafeForwards = 0.8F;
        this.strafeRight = 0.2F;
    }

    /**
     * Avoids collisions with other entities by diverting path if too close.
     */
    private void avoidEntityCollision() {
        List<Entity> nearbyEntities = this.mob.level().getEntities(this.mob, this.mob.getBoundingBox().inflate(this.mob.getBoundingBox().getXsize()), entity -> entity != this.mob);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Mob) {
                double dX = this.mob.getX() - entity.getX();
                double dZ = this.mob.getZ() - entity.getZ();
                double distanceSquared = dX * dX + dZ * dZ;

                if (distanceSquared < 1.0D) {
                    float targetAngle = (float) (Mth.atan2(dZ, dX) * 180.0F / Math.PI) - 90.0F;
                    this.mob.setYRot(this.rotlerp(this.mob.getYRot(), targetAngle, MAX_TURN));
                    this.mob.setSpeed(0.2F);
                }
            }
        }
    }

    /**
     * @return true if the mob can walk successfully to a given X and Z
     */
    private boolean isWalkable(float pRelativeX, float pRelativeZ) {
        PathNavigation pathNavigation = this.mob.getNavigation();
        NodeEvaluator nodeEvaluator = pathNavigation.getNodeEvaluator();
        BlockPos targetPos = BlockPos.containing(this.mob.getX() + pRelativeX, this.mob.getBlockY(), this.mob.getZ() + pRelativeZ);
        PathType pathType = nodeEvaluator.getPathType(this.mob, targetPos);

        // Adjust path check to account for the mob's hitbox size to avoid tight areas
        if (pathType != PathType.WALKABLE || !isValidPath(targetPos)) {
            return false;
        }

        AABB boundingBox = this.mob.getBoundingBox();
        AABB targetBox = new AABB(targetPos).inflate(boundingBox.getXsize() / 2.0, boundingBox.getYsize() / 2.0, boundingBox.getZsize() / 2.0);

        // Ensure enough space at the target position for the mob's bounding box
        if (!this.mob.level().noCollision(this.mob, targetBox)) {
            return false;
        }

        return !isBlockedByObstacle(targetPos);
    }

    private boolean isValidPath(BlockPos targetPos) {
        BlockState blockState = this.mob.level().getBlockState(targetPos);
        return !blockState.is(BlockTags.FENCES) && !blockState.is(BlockTags.FENCE_GATES);
    }

    private boolean isBlockedByObstacle(BlockPos targetPos) {
        BlockState blockState = this.mob.level().getBlockState(targetPos);
        return blockState.isSolid();
    }

    /**
     * Attempt to rotate the first angle to become the second angle, but only allow overall direction change to at max be third parameter
     */
    protected float rotlerp(float pSourceAngle, float pTargetAngle, float pMaximumChange) {
        float delta = Mth.wrapDegrees(pTargetAngle - pSourceAngle);
        delta = Mth.clamp(delta, -pMaximumChange, pMaximumChange);
        return Mth.wrapDegrees(pSourceAngle + delta);
    }
}
