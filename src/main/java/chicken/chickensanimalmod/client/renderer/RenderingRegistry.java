package chicken.chickensanimalmod.client.renderer;

import chicken.chickensanimalmod.AnimalMod;
import chicken.chickensanimalmod.client.renderer.obj.GeoBaseEntityRenderer;
import chicken.chickensanimalmod.server.entity.AnimalModEntities;
import chicken.chickensanimalmod.server.entity.obj.EntityHolder;
import chicken.chickensanimalmod.server.entity.obj.geo.GeoEntityBase;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
        for (EntityHolder<? extends GeoEntityBase> type : AnimalModEntities.getTYPES()) {
            EntityRenderers.register(type.type(), (context -> new GeoBaseEntityRenderer(context, type)));
        }
    }
}
