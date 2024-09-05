package net.voidarkana.ratswithwings.common.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.voidarkana.ratswithwings.RatsWithWings;
import net.voidarkana.ratswithwings.common.entity.ModEntities;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RatsWithWings.MOD_ID);

    public static final RegistryObject<Item> PIGEON_SPAWN_EGG = ITEMS.register("pigeon_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.PIGEON, 0x4e5869, 0x458876, new Item.Properties()));
    public static final RegistryObject<Item> COYOTE_SPAWN_EGG = ITEMS.register("coyote_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.COYOTE, 0xA3582A, 0x3E333F, new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
