package net.voidarkana.ratswithwings.client.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.voidarkana.ratswithwings.RatsWithWings;
import net.voidarkana.ratswithwings.client.layer.ModModelLayer;
import net.voidarkana.ratswithwings.client.model.PigeonModel;

@Mod.EventBusSubscriber(modid = RatsWithWings.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(ModModelLayer.PIGEON_LAYER, PigeonModel::createBodyLayer);
    }

}
