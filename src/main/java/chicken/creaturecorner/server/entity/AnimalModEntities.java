package chicken.creaturecorner.server.entity;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.server.entity.obj.*;
import chicken.creaturecorner.server.entity.obj.geo.GeoEntityBase;
import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

public class AnimalModEntities {
//    @Getter
//    private static final ArrayList<EntityHolder<? extends Animal>> TYPES = new ArrayList<>();


    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, AnimalMod.MODID);


    public static final Supplier<EntityType<PigeonEntity>> PIGEON_TYPE =
            ENTITY_TYPES.register("pigeon",
                    () -> EntityType.Builder.of(PigeonEntity::new, MobCategory.CREATURE)
                            .sized(0.7f, 0.9f)
                            .build(ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "pigeon").toString()));


    public static final Supplier<EntityType<EndoveEntity>> ENDOVE_TYPE =
            ENTITY_TYPES.register("endove",
                    () -> EntityType.Builder.of(EndoveEntity::new, MobCategory.CREATURE)
                            .sized(0.7f, 0.9f)
                            .build(ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "endove").toString()));


    public static final Supplier<EntityType<CaracaraEntity>> CARACARA_TYPE =
            ENTITY_TYPES.register("caracara",
                    () -> EntityType.Builder.of(CaracaraEntity::new, MobCategory.CREATURE)
                            .sized(1f, 1f)
                            .build(ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "caracara").toString()));


    public static final Supplier<EntityType<CoyoteEntity>> COYOTE_TYPE =
            ENTITY_TYPES.register("coyote",
                    () -> EntityType.Builder.of(CoyoteEntity::new, MobCategory.CREATURE)
                            .sized(0.7F, 0.8F)
                            .build(ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "coyote").toString()));

//    public static final EntityType<PigeonEntity> PIGEON_TYPE = registerType("pigeon",
//            EntityType.Builder.of(PigeonEntity::new, MobCategory.CREATURE)
//                    .sized(0.5F, 0.5F)
//    );

//    public static final EntityType<EndoveEntity> ENDOVE_TYPE = registerType("endove",
//            EntityType.Builder.of(EndoveEntity::new, MobCategory.CREATURE)
//                    .sized(0.5F, 0.5F)
//    );

//    public static final EntityType<CaracaraEntity> CARACARA_TYPE = registerType("caracara",
//            EntityType.Builder.of(CaracaraEntity::new, MobCategory.CREATURE)
//                    .sized(1F, 1F)
//    );

//    public static final EntityType<CoyoteEntity> COYOTE_TYPE = registerType("coyote",
//            EntityType.Builder.of(CoyoteEntity::new, MobCategory.CREATURE)
//                    .sized(0.7F, 0.8F)
//    );

//    @SubscribeEvent
//    public static void registerEvent(RegisterEvent event) {
//        for (EntityHolder<?> type : TYPES) {
//            event.register(BuiltInRegistries.ENTITY_TYPE.key(), (r) -> {
//                r.register(ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, type.name()), type.type());
//            });
//        }
//    }


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

//    protected static <T extends Animal> EntityType<T> registerType(String name, EntityType.Builder<T> entity) {
//        EntityType<T> entityType = entity.build(name);
//        TYPES.add(new EntityHolder<>(name, entityType));
//        return entityType;
//    }
}
