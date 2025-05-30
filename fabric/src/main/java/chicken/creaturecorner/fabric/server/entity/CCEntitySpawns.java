package chicken.creaturecorner.fabric.server.entity;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.server.entity.CCEntities;
import chicken.creaturecorner.server.entity.PigeonSpawner;
import chicken.creaturecorner.server.entity.obj.CaracaraEntity;
import chicken.creaturecorner.server.entity.obj.CoyoteEntity;
import chicken.creaturecorner.server.entity.obj.EndoveEntity;
import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

public class CCEntitySpawns {
    private static final TagKey<Biome> caracaraBiome = create("is_caracara_biome");
    private static final TagKey<Biome> coyoteBiome = create("is_coyote_biome");
    private static final TagKey<Biome> endoveBiome = create("is_endove_biome");
    private static final TagKey<Biome> pigeonBiome = create("is_pigeon_biome");

    private static final PigeonSpawner pigeonSpawner = new PigeonSpawner();

    public static void init() {
        SpawnPlacements.register(CCEntities.PIGEON_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PigeonEntity::checkAnimalSpawnRules);
        SpawnPlacements.register(CCEntities.ENDOVE_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndoveEntity::canEndoveSpawn);
        SpawnPlacements.register(CCEntities.CARACARA_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CaracaraEntity::checkAnimalSpawnRules);
        SpawnPlacements.register(CCEntities.COYOTE_TYPE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CoyoteEntity::checkCoyoteSpawnRules);

        BiomeModifications.addSpawn(
                biomeSelectionContext -> biomeSelectionContext.hasTag(caracaraBiome), MobCategory.CREATURE, CCEntities.CARACARA_TYPE.get()
                , 8, 2, 5
        );
        BiomeModifications.addSpawn(
                biomeSelectionContext -> biomeSelectionContext.hasTag(coyoteBiome), MobCategory.CREATURE, CCEntities.COYOTE_TYPE.get()
                , 8, 2, 5
        );
        BiomeModifications.addSpawn(
                biomeSelectionContext -> biomeSelectionContext.hasTag(endoveBiome), MobCategory.CREATURE, CCEntities.ENDOVE_TYPE.get()
                , 100, 5, 15
        );
        BiomeModifications.addSpawn(
                biomeSelectionContext -> biomeSelectionContext.hasTag(pigeonBiome), MobCategory.CREATURE, CCEntities.PIGEON_TYPE.get()
                , 6, 5, 10
        );

        ServerTickEvents.START_WORLD_TICK.register(world -> {
            pigeonSpawner.tick(world, true, true);
        });
    }

    private static TagKey<Biome> create(String name) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, name));
    }
}
