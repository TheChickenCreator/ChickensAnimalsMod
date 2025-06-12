package chicken.creaturecorner.server.entity.obj.base.goal;

import chicken.creaturecorner.server.entity.obj.base.GeoTamableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;

public class GeoOwnerHurtByTargetGoal extends TargetGoal {
    private final GeoTamableEntity tameAnimal;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public GeoOwnerHurtByTargetGoal(GeoTamableEntity tameAnimal) {
        super(tameAnimal, false);
        this.tameAnimal = tameAnimal;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
//        if (this.tameAnimal.isTame() && !this.tameAnimal.isOrderedToSit()) {
//            LivingEntity livingentity = this.tameAnimal.getOwner();
//            if (livingentity == null) {
//                return false;
//            } else {
//                this.ownerLastHurtBy = livingentity.getLastHurtByMob();
//                int i = livingentity.getLastHurtByMobTimestamp();
//                return i != this.timestamp
//                    && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT)
//                    && this.tameAnimal.wantsToAttack(this.ownerLastHurtBy, livingentity);
//            }
//        } else {
            return false;
//        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity livingentity = this.tameAnimal.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
