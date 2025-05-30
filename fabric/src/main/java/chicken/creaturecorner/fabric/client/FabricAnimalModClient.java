package chicken.creaturecorner.fabric.client;

import chicken.creaturecorner.client.renderer.obj.*;
import chicken.creaturecorner.server.entity.CCEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class FabricAnimalModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(CCEntities.PIGEON_TYPE.get(), PigeonRenderer::new);
        EntityRendererRegistry.register(CCEntities.NEW_PIGEON.get(), NewPigeonRenderer::new);
        EntityRendererRegistry.register(CCEntities.COYOTE_TYPE.get(), CoyoteRenderer::new);
        EntityRendererRegistry.register(CCEntities.CARACARA_TYPE.get(), CaracaraRenderer::new);
        EntityRendererRegistry.register(CCEntities.ENDOVE_TYPE.get(), EndoveRenderer::new);
    }
}
