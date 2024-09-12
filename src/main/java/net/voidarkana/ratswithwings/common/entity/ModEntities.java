package net.voidarkana.ratswithwings.common.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.voidarkana.ratswithwings.RatsWithWings;
import net.voidarkana.ratswithwings.common.entity.custom.CoyoteEntity;
import net.voidarkana.ratswithwings.common.entity.custom.PigeonEntity;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RatsWithWings.MOD_ID);

    public static final RegistryObject<EntityType<PigeonEntity>> PIGEON =
            ENTITY_TYPES.register("pigeon",
                    () -> EntityType.Builder.of(PigeonEntity::new, MobCategory.AMBIENT)
                            .sized(0.8f, 0.6f)
                            .build(new ResourceLocation(RatsWithWings.MOD_ID, "pigeon").toString()));

    public static final RegistryObject<EntityType<CoyoteEntity>> COYOTE =
            ENTITY_TYPES.register("coyote",
                    () -> EntityType.Builder.of(CoyoteEntity::new, MobCategory.CREATURE)
                            .sized(1f, 1f)
                            .build(new ResourceLocation(RatsWithWings.MOD_ID, "coyote").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
