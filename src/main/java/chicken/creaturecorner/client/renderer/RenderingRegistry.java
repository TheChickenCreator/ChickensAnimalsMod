package chicken.creaturecorner.client.renderer;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.client.renderer.obj.CaracaraRenderer;
import chicken.creaturecorner.client.renderer.obj.CoyoteRenderer;
import chicken.creaturecorner.client.renderer.obj.PigeonRenderer;
import chicken.creaturecorner.server.entity.AnimalModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;

@EventBusSubscriber(modid = AnimalMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderingRegistry {
    private static final HashMap<EntityType<?>, EntityRendererProvider<?>> renderers = new HashMap<>();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        EntityRenderers.register(AnimalModEntities.PIGEON_TYPE, PigeonRenderer::new);
        EntityRenderers.register(AnimalModEntities.COYOTE_TYPE, CoyoteRenderer::new);
        EntityRenderers.register(AnimalModEntities.CARACARA_TYPE, CaracaraRenderer::new);

    }
}
