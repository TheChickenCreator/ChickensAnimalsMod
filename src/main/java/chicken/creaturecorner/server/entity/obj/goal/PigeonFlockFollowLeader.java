package chicken.creaturecorner.server.entity.obj.goal;

import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import com.mojang.datafixers.DataFixUtils;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.List;
import java.util.function.Predicate;

public class PigeonFlockFollowLeader extends Goal {
    private static final int INTERVAL_TICKS = 200;
    private final PigeonEntity mob;
    private int timeToRecalcPath;
    private int nextStartTick;

    public PigeonFlockFollowLeader(PigeonEntity fish) {
        this.mob = fish;
        this.nextStartTick = this.nextStartTick(fish);
    }

    protected int nextStartTick(PigeonEntity taskOwner) {
        return reducedTickDelay(200 + taskOwner.getRandom().nextInt(200) % 20);
    }

    @Override
    public boolean canUse() {
        if (this.mob.hasFollowers()) {
            return false;
        } else if (this.mob.isFollower()) {
            return true;
        } else if (this.nextStartTick > 0) {
            this.nextStartTick--;
            return false;
        } else {
            this.nextStartTick = this.nextStartTick(this.mob);
            Predicate<PigeonEntity> predicate = p_25258_ -> p_25258_.canBeFollowed() || !p_25258_.isFollower();
            List<? extends PigeonEntity> list = this.mob
                    .level()
                    .getEntitiesOfClass((Class<? extends PigeonEntity>)this.mob.getClass(), this.mob.getBoundingBox().inflate(8.0, 8.0, 8.0), predicate);
            PigeonEntity abstractschoolingfish = DataFixUtils.orElse(list.stream().filter(PigeonEntity::canBeFollowed).findAny(), this.mob);
            abstractschoolingfish.addFollowers(list.stream().filter(p_25255_ -> !p_25255_.isFollower()));
            return this.mob.isFollower();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.isFollower() && this.mob.inRangeOfLeader();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.mob.stopFollowing();
    }

    @Override
    public void tick() {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.mob.pathToLeader();
        }
    }
}