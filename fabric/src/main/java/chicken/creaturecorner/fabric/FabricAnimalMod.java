package chicken.creaturecorner.fabric;

import chicken.creaturecorner.CCCommon;
import chicken.creaturecorner.fabric.server.entity.CCEntitySpawns;
import chicken.creaturecorner.server.entity.CCEntities;
import chicken.creaturecorner.server.entity.obj.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class FabricAnimalMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CCCommon.init();
        FabricDefaultAttributeRegistry.register(CCEntities.PIGEON_TYPE.get(), PigeonEntity.createAttributes().build());
//        FabricDefaultAttributeRegistry.register(CCEntities.NEW_PIGEON.get(), NewPigeonEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(CCEntities.ENDOVE_TYPE.get(), EndoveEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(CCEntities.CARACARA_TYPE.get(), CaracaraEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(CCEntities.COYOTE_TYPE.get(), CoyoteEntity.createAttributes().build());

        CCEntitySpawns.init();
    }
}
