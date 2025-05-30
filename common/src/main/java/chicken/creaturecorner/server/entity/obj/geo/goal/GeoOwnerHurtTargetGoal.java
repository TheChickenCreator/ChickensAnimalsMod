package chicken.creaturecorner.server.entity.obj.geo.goal;

import chicken.creaturecorner.server.entity.obj.geo.GeoTamableEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;

public class GeoOwnerHurtTargetGoal extends TargetGoal {
    private final GeoTamableEntity tameAnimal;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public GeoOwnerHurtTargetGoal(GeoTamableEntity tameAnimal) {
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
//                this.ownerLastHurt = livingentity.getLastHurtMob();
//                int i = livingentity.getLastHurtMobTimestamp();
//                return i != this.timestamp
//                    && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT)
//                    && this.tameAnimal.wantsToAttack(this.ownerLastHurt, livingentity);
//            }
//        } else {
            return false;
        //}
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity livingentity = this.tameAnimal.getOwner();
        if (livingentity != null) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}
