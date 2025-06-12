package chicken.creaturecorner.server.entity.obj.goal;

import chicken.creaturecorner.server.entity.obj.base.GeoEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;

import java.util.EnumSet;
import java.util.function.Predicate;

public class EatGrassGoal extends Goal {

    private static final Predicate<BlockState> IS_TALL_GRASS = BlockStatePredicate.forBlock(Blocks.SHORT_GRASS);
    private final GeoEntityBase mob;
    private final Level level;
    private int eatAnimationTick;
    private int eatDuration;

    public EatGrassGoal(GeoEntityBase mob, int duration) {
        this.mob = mob;
        this.level = mob.level();
        this.eatDuration = duration;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    public boolean canUse() {

        if (this.mob.isHungry()) {
            BlockPos blockPos = this.mob.blockPosition();
            if (IS_TALL_GRASS.test(this.level.getBlockState(blockPos))) {
                return true;
            } else {
                return this.level.getBlockState(blockPos.below()).is(Blocks.GRASS_BLOCK);
            }
        }

        return false;
    }

    public void start() {
        this.eatAnimationTick = this.adjustedTickDelay(eatDuration);
        this.level.broadcastEntityEvent(this.mob, (byte)10);
        this.mob.getNavigation().stop();
    }

    public void stop() {
        this.eatAnimationTick = 0;
    }

    public boolean canContinueToUse() {
        return this.eatAnimationTick > 0;
    }

    public int getEatAnimationTick() {
        return this.eatAnimationTick;
    }

    public int getTickForAnim(){
        return this.eatDuration - this.getEatAnimationTick();
    }

    public void tick() {
        this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        if (this.eatAnimationTick == this.adjustedTickDelay(4)) {
            BlockPos blockPos = this.mob.blockPosition();
            if (IS_TALL_GRASS.test(this.level.getBlockState(blockPos))) {
                if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    this.level.destroyBlock(blockPos, false);
                }

                this.mob.ate();
                this.mob.setFoodLevel(this.mob.maxFood()/4);

            } else {
                BlockPos blockPos2 = blockPos.below();
                if (this.level.getBlockState(blockPos2).is(Blocks.GRASS_BLOCK)) {
                    if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        this.level.levelEvent(2001, blockPos2, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                        this.level.setBlock(blockPos2, Blocks.DIRT.defaultBlockState(), 2);
                    }

                    this.mob.ate();
                    this.mob.setFoodLevel(this.mob.maxFood()/4);
                }
            }

        }
    }

}
