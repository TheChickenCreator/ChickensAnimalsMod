package chicken.chickensanimalmod.server.entity;

import chicken.chickensanimalmod.AnimalMod;
import chicken.chickensanimalmod.server.entity.obj.CaracaraEntity;
import chicken.chickensanimalmod.server.entity.obj.CoyoteEntity;
import chicken.chickensanimalmod.server.entity.obj.EntityHolder;
import chicken.chickensanimalmod.server.entity.obj.PigeonEntity;
import chicken.chickensanimalmod.server.entity.obj.geo.GeoEntityBase;
import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;

public class AnimalModEntities {
    @Getter
    private static final ArrayList<EntityHolder<? extends GeoEntityBase>> TYPES = new ArrayList<>();

    public static final EntityType<PigeonEntity> PIGEON_TYPE = registerType("pigeon",
            EntityType.Builder.of(PigeonEntity::new, MobCategory.AMBIENT)
                    .sized(0.5F, 0.5F)
    );

    public static final EntityType<CaracaraEntity> CARACARA_TYPE = registerType("caracara",
            EntityType.Builder.of(CaracaraEntity::new, MobCategory.AMBIENT)
                    .sized(1F, 1F)
    );

    public static final EntityType<CoyoteEntity> COYOTE_TYPE = registerType("coyote",
            EntityType.Builder.of(CoyoteEntity::new, MobCategory.AMBIENT)
                    .sized(0.5F, 0.5F)
    );

    @SubscribeEvent
    public static void registerAdditionalSpawns(RegisterSpawnPlacementsEvent e) {
        e.register(PIGEON_TYPE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.WORLD_SURFACE, PigeonEntity::spawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void registerEvent(RegisterEvent event) {
        for (EntityHolder<?> type : TYPES) {
            event.register(BuiltInRegistries.ENTITY_TYPE.key(), (r) -> {
                r.register(ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, type.name()), type.type());
            });
        }
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(PIGEON_TYPE, PigeonEntity.createAttributes().build());
        event.put(CARACARA_TYPE, CaracaraEntity.createAttributes().build());
        event.put(COYOTE_TYPE, CoyoteEntity.createAttributes().build());
    }

    protected static <T extends GeoEntityBase> EntityType<T> registerType(String name, EntityType.Builder<T> entity) {
        EntityType<T> entityType = entity.build(name);
        TYPES.add(new EntityHolder<>(name, entityType));
        return entityType;
    }
}
