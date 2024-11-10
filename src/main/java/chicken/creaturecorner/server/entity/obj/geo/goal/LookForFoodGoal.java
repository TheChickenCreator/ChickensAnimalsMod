package chicken.creaturecorner.server.entity.obj.geo.goal;

import chicken.creaturecorner.server.entity.obj.geo.GeoEntityBase;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Predicate;

public class LookForFoodGoal extends Goal {
    private final GeoEntityBase base;
    private final TagKey<Item> tags;
    private ItemEntity itemPos;
    public LookForFoodGoal(GeoEntityBase base, TagKey<Item> tags) {
        this.base = base;
        this.tags = tags;
    }

    @Override
    public boolean canUse() {
        if(base.isHungry()) {
            ItemEntity pos = itemPos();
            if (pos != null) {
                itemPos = pos;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.base.getNavigation().moveTo(itemPos, 1.1);
    }

    public ItemEntity itemPos() {
        Predicate<ItemEntity> predicate = (p_25258_) -> p_25258_.getItem().is(tags);
        List<? extends ItemEntity> list = this.base.level().getEntitiesOfClass(ItemEntity.class, this.base.getBoundingBox().inflate(32, 8.0, 32), predicate);
        if(!list.isEmpty()) {
            return list.getFirst();
        } else {
            return null;
        }
    }

    public void trigger() {
        if(canUse()) {
            this.base.getNavigation().stop();
            start();
        }
    }
}
