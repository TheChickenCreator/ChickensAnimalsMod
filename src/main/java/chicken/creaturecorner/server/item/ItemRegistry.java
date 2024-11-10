package chicken.creaturecorner.server.item;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.server.entity.AnimalModEntities;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AnimalMod.MODID);

    public static final DeferredItem<Item> PIGEON_EGG = ITEMS.register("pigeon_spawn_egg", () -> new DeferredSpawnEggItem(() -> AnimalModEntities.PIGEON_TYPE, 0x434A5E, 0x448675, new Item.Properties()));
    public static final DeferredItem<Item> COYOTE_SPAWN_EGG = ITEMS.register("coyote_spawn_egg", ()-> new DeferredSpawnEggItem(() -> AnimalModEntities.COYOTE_TYPE, 0xA3582A, 0x3E333F, new Item.Properties()));
}
