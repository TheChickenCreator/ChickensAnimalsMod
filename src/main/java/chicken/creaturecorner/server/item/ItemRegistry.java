package chicken.creaturecorner.server.item;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.sound.CCSounds;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AnimalMod.MODID);


    public static final DeferredItem<Item> GALLIAN_GROTTO_DISC = ITEMS.register("gallian_grotto_disc", () ->
            new Item(new Item.Properties().jukeboxPlayable(CCSounds.GALLIAN_GROTTO_KEY).stacksTo(1)));


    public static final DeferredItem<Item> PIGEON_EGG = ITEMS.register("pigeon_spawn_egg", () ->
            new DeferredSpawnEggItem(() -> AnimalModEntities.PIGEON_TYPE, 0x434A5E, 0x448675, new Item.Properties()));

    public static final DeferredItem<Item> ENDOVE_SPAWN_EGG = ITEMS.register("endove_spawn_egg", () ->
            new DeferredSpawnEggItem(() -> AnimalModEntities.ENDOVE_TYPE, 0x54365c, 0xb863a7, new Item.Properties()));


    public static final DeferredItem<Item> COYOTE_SPAWN_EGG = ITEMS.register("coyote_spawn_egg",
            ()-> new DeferredSpawnEggItem(() -> AnimalModEntities.COYOTE_TYPE, 0xA3582A, 0x3E333F, new Item.Properties()));

    public static final DeferredItem<Item> CARACARA_SPAWN_EGG = ITEMS.register("caracara_spawn_egg",
            ()-> new DeferredSpawnEggItem(() -> AnimalModEntities.CARACARA_TYPE, 0x3c2726, 0xead8be, new Item.Properties()));

}
