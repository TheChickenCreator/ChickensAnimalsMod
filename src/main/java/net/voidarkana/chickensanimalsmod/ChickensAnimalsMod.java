package net.voidarkana.chickensanimalsmod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.voidarkana.chickensanimalsmod.client.renderer.CoyoteRenderer;
import net.voidarkana.chickensanimalsmod.client.renderer.PigeonRenderer;
import net.voidarkana.chickensanimalsmod.common.entity.ModEntities;
import net.voidarkana.chickensanimalsmod.common.items.ModItems;
import org.slf4j.Logger;


@Mod(ChickensAnimalsMod.MOD_ID)
public class ChickensAnimalsMod
{

    public static final String MOD_ID = "chickensanimalsmod";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ChickensAnimalsMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS){
            event.accept(ModItems.PIGEON_SPAWN_EGG);
            event.accept(ModItems.COYOTE_SPAWN_EGG);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            EntityRenderers.register(ModEntities.PIGEON.get(), PigeonRenderer::new);
            EntityRenderers.register(ModEntities.COYOTE.get(), CoyoteRenderer::new);
        }
    }
}
