package chicken.chickensanimalmod.server.entity.obj;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public record EntityHolder<T extends LivingEntity>(String name, EntityType<T> type) {
}
