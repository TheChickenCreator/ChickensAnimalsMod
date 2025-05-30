package chicken.creaturecorner.server.entity;

import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class PigeonSpawner implements CustomSpawner {
    private int nextTick;

    @Override
    public int tick(ServerLevel level, boolean spawnHostiles, boolean spawnPassives) {
        if (spawnPassives && level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            this.nextTick--;
            if (this.nextTick <= 0) {
                this.nextTick = 200;
                Player player = level.getRandomPlayer();
                if (player != null) {
                    BlockPos origin = player.blockPosition();
                    BlockPos groundPos = null;

                    int radius = level.random.nextInt(2, 12);
                    outer:
                    for (int dx = -radius; dx <= radius; dx++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            BlockPos pos = origin.offset(dx, 0, dz);
                            BlockPos top = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);

                            if (SpawnPlacements.isSpawnPositionOk(CCEntities.PIGEON_TYPE.get(), level, top)) {
                                if (level.isCloseToVillage(top, 2)) {
                                    groundPos = top;
                                    break outer;
                                }
                            }
                        }
                    }

                    if (groundPos != null && level.getBiome(groundPos).is(BiomeTags.HAS_VILLAGE_PLAINS)) {
                        return this.spawnInVillage(level, groundPos);
                    }

                }
            }
        }
        return 0;
    }

    private int spawnInVillage(ServerLevel serverLevel, BlockPos pos) {
        List<PigeonEntity> list = serverLevel.getEntitiesOfClass(PigeonEntity.class, new AABB(pos).inflate(52.0, 32.0, 52.0));
        if (list.size() < 8) {
            return this.spawnCat(pos, serverLevel);
        } else {
            this.nextTick = 1200;
        }
        return 0;
    }

    private int spawnCat(BlockPos pos, ServerLevel serverLevel) {
        PigeonEntity cat = (PigeonEntity) CCEntities.PIGEON_TYPE.get().create(serverLevel);
        if (cat == null) {
            return 0;
        } else {
            cat.moveTo(pos, 0.0F, 0.0F); // Fix MC-147659: Some witch huts spawn the incorrect cat
            cat.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
            serverLevel.addFreshEntityWithPassengers(cat);
            return 1;
        }
    }
}
