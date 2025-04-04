package chicken.creaturecorner.server.entity.obj.geo.goal;

import chicken.creaturecorner.server.entity.obj.geo.GeoTamableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class GeoNonTameRandomTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final GeoTamableEntity tamableMob;

    public GeoNonTameRandomTargetGoal(GeoTamableEntity tamableMob, Class<T> targetType, boolean mustSee, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(tamableMob, targetType, 10, mustSee, false, targetPredicate);
        this.tamableMob = tamableMob;
    }

    @Override
    public boolean canUse() {
        return !this.tamableMob.isTame() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetConditions.test(this.mob, this.target);
    }
}
