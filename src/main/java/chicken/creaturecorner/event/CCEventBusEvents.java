package chicken.creaturecorner.event;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.entity.obj.CaracaraEntity;
import chicken.creaturecorner.server.entity.obj.CoyoteEntity;
import chicken.creaturecorner.server.entity.obj.EndoveEntity;
import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = AnimalMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CCEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(AnimalModEntities.PIGEON_TYPE.get(), PigeonEntity.createAttributes().build());
        event.put(AnimalModEntities.ENDOVE_TYPE.get(), EndoveEntity.createAttributes().build());
        event.put(AnimalModEntities.CARACARA_TYPE.get(), CaracaraEntity.createAttributes().build());
        event.put(AnimalModEntities.COYOTE_TYPE.get(), CoyoteEntity.createAttributes().build());
    }


    @SubscribeEvent
    public static void registerAdditionalSpawns(RegisterSpawnPlacementsEvent e) {
        e.register(AnimalModEntities.PIGEON_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.WORLD_SURFACE, PigeonEntity::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(AnimalModEntities.ENDOVE_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndoveEntity::canEndoveSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(AnimalModEntities.CARACARA_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaracaraEntity::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(AnimalModEntities.COYOTE_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CoyoteEntity::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }


}
