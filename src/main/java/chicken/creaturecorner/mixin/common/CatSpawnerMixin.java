package chicken.creaturecorner.mixin.common;

import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.level.CustomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatSpawner.class)
public class CatSpawnerMixin implements CustomSpawner {
    @Override
    public int tick(ServerLevel serverLevel, boolean b, boolean b1) {
        return 0;
    }

    @Inject(method = "spawnCat", at = @At("HEAD"), cancellable = true)
    private void spawnCatMethod(BlockPos pos, ServerLevel serverLevel, CallbackInfoReturnable<Integer> cir) {
        PigeonEntity pigeon = AnimalModEntities.PIGEON_TYPE.get().create(serverLevel);
        if (pigeon != null) {
            pigeon.moveTo(pos, 0.0F, 0.0F);
            pigeon.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
            serverLevel.addFreshEntityWithPassengers(pigeon);
        }
    }
}
