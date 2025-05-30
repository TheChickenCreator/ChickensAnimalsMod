package chicken.creaturecorner.neoforge;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.server.entity.CCEntities;
import chicken.creaturecorner.server.entity.PigeonSpawner;
import chicken.creaturecorner.server.entity.obj.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = CCConstants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CCEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(CCEntities.PIGEON_TYPE.get(), PigeonEntity.createAttributes().build());
        event.put(CCEntities.NEW_PIGEON.get(), NewPigeonEntity.createAttributes().build());
        event.put(CCEntities.ENDOVE_TYPE.get(), EndoveEntity.createAttributes().build());
        event.put(CCEntities.CARACARA_TYPE.get(), CaracaraEntity.createAttributes().build());
        event.put(CCEntities.COYOTE_TYPE.get(), CoyoteEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerAdditionalSpawns(RegisterSpawnPlacementsEvent e) {
        e.register(CCEntities.PIGEON_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.WORLD_SURFACE, PigeonEntity::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(CCEntities.ENDOVE_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndoveEntity::canEndoveSpawn, RegisterSpawnPlacementsEvent.Operation.OR);
        e.register(CCEntities.CARACARA_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaracaraEntity::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(CCEntities.COYOTE_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CoyoteEntity::checkCoyoteSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
