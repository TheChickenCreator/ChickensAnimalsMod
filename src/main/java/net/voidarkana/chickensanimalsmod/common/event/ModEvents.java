package net.voidarkana.chickensanimalsmod.common.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.voidarkana.chickensanimalsmod.ChickensAnimalsMod;
import net.voidarkana.chickensanimalsmod.common.entity.ModEntities;
import net.voidarkana.chickensanimalsmod.common.entity.custom.CoyoteEntity;
import net.voidarkana.chickensanimalsmod.common.entity.custom.PigeonEntity;

@Mod.EventBusSubscriber(modid = ChickensAnimalsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(ModEntities.PIGEON.get(), PigeonEntity.createAttributes().build());
        event.put(ModEntities.COYOTE.get(), CoyoteEntity.createAttributes().build());
    }
}
