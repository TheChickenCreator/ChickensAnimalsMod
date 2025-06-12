package chicken.creaturecorner.neoforge.client.renderer;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.client.renderer.obj.*;
import chicken.creaturecorner.server.entity.CCEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.HashMap;

@EventBusSubscriber(modid = CCConstants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderingRegistry {
    private static final HashMap<EntityType<?>, EntityRendererProvider<?>> renderers = new HashMap<>();

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CCEntities.PIGEON_TYPE.get(), PigeonRenderer::new);
//        event.registerEntityRenderer(CCEntities.NEW_PIGEON.get(), NewPigeonRenderer::new);
        event.registerEntityRenderer(CCEntities.COYOTE_TYPE.get(), CoyoteRenderer::new);
        event.registerEntityRenderer(CCEntities.CARACARA_TYPE.get(), CaracaraRenderer::new);
        event.registerEntityRenderer(CCEntities.ENDOVE_TYPE.get(), EndoveRenderer::new);

        event.registerEntityRenderer(CCEntities.GALLIAN.get(), GallianRenderer::new);
    }
}
