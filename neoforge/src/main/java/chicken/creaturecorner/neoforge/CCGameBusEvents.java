package chicken.creaturecorner.neoforge;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.server.entity.CCEntities;
import chicken.creaturecorner.server.entity.PigeonSpawner;
import chicken.creaturecorner.server.entity.obj.CaracaraEntity;
import chicken.creaturecorner.server.entity.obj.CoyoteEntity;
import chicken.creaturecorner.server.entity.obj.EndoveEntity;
import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = CCConstants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CCGameBusEvents {
    private static final PigeonSpawner spawner = new PigeonSpawner();
    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        spawner.tick(serverLevel, true, true);
    }
}
