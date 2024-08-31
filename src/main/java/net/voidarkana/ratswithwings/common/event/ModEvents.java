package net.voidarkana.ratswithwings.common.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.voidarkana.ratswithwings.RatsWithWings;
import net.voidarkana.ratswithwings.common.entity.ModEntities;
import net.voidarkana.ratswithwings.common.entity.custom.PigeonEntity;

@Mod.EventBusSubscriber(modid = RatsWithWings.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(ModEntities.PIGEON.get(), PigeonEntity.createAttributes().build());
    }
}
