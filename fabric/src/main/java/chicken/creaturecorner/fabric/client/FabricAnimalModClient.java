package chicken.creaturecorner.fabric.client;

import chicken.creaturecorner.client.CCModelLayers;
import chicken.creaturecorner.client.model.GallianChickModel;
import chicken.creaturecorner.client.model.GallianModel;
import chicken.creaturecorner.client.renderer.obj.*;
import chicken.creaturecorner.server.entity.CCEntities;
import com.google.common.reflect.Reflection;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class FabricAnimalModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(CCEntities.PIGEON_TYPE.get(), PigeonRenderer::new);
//        EntityRendererRegistry.register(CCEntities.NEW_PIGEON.get(), NewPigeonRenderer::new);
        EntityRendererRegistry.register(CCEntities.COYOTE_TYPE.get(), CoyoteRenderer::new);
        EntityRendererRegistry.register(CCEntities.CARACARA_TYPE.get(), CaracaraRenderer::new);
        EntityRendererRegistry.register(CCEntities.ENDOVE_TYPE.get(), EndoveRenderer::new);

        EntityRendererRegistry.register(CCEntities.GALLIAN.get(), GallianRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(CCModelLayers.GALLIAN, GallianModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CCModelLayers.GALLIAN_CHICK, GallianChickModel::createBodyLayer);
    }
}
