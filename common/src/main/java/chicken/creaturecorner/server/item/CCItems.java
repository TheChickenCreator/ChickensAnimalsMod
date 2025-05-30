package chicken.creaturecorner.server.item;

import chicken.creaturecorner.platform.Services;
import chicken.creaturecorner.platform.services.AccessUtil;
import chicken.creaturecorner.server.entity.CCEntities;
import chicken.creaturecorner.server.sound.CCSounds;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class CCItems {
    public static final Supplier<Item> GALLIAN_GROTTO_DISC = register("gallian_grotto_disc", () ->
            new Item(new Item.Properties().jukeboxPlayable(CCSounds.GALLIAN_GROTTO_KEY).stacksTo(1)));

    public static final Supplier<Item> PIGEON_EGG = register("pigeon_spawn_egg", () ->
            Services.GENERIC.createSpawnEgg(CCEntities.PIGEON_TYPE, 0x434A5E, 0x448675, new Item.Properties()));

    public static final Supplier<Item> NEW_PIGEON_EGG = register("new_pigeon_spawn_egg", () ->
            Services.GENERIC.createSpawnEgg(CCEntities.NEW_PIGEON, 0x434A5E, 0x448675, new Item.Properties()));

    public static final Supplier<Item> ENDOVE_SPAWN_EGG = register("endove_spawn_egg", () ->
            Services.GENERIC.createSpawnEgg(CCEntities.ENDOVE_TYPE, 0x54365c, 0xb863a7, new Item.Properties()));

    public static final Supplier<Item> COYOTE_SPAWN_EGG = register("coyote_spawn_egg",
            ()-> Services.GENERIC.createSpawnEgg(CCEntities.COYOTE_TYPE, 0xA3582A, 0x3E333F, new Item.Properties()));

    public static final Supplier<Item> CARACARA_SPAWN_EGG = register("caracara_spawn_egg",
            ()-> Services.GENERIC.createSpawnEgg(CCEntities.CARACARA_TYPE, 0x3c2726, 0xead8be, new Item.Properties()));

    public static Supplier<Item> register(String s, Supplier<Item> item) {
        item = Suppliers.memoize(item);
        Services.PLATFORM.register(BuiltInRegistries.ITEM, s, item);
        return item;
    }
}
