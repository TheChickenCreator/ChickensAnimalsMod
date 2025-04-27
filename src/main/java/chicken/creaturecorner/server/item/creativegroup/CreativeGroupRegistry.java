package chicken.creaturecorner.server.item.creativegroup;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.server.item.ItemRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeGroupRegistry {
    public static final DeferredRegister<CreativeModeTab> TAB_DEFERRED_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AnimalMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANIMAL_TAB = TAB_DEFERRED_REGISTER.register("animal_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("creaturecorner.title.animal"))
                    .icon(ItemRegistry.PIGEON_EGG::toStack)
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ItemRegistry.PIGEON_EGG);
                        output.accept(ItemRegistry.ENDOVE_SPAWN_EGG);
                        output.accept(ItemRegistry.COYOTE_SPAWN_EGG);
                        output.accept(ItemRegistry.CARACARA_SPAWN_EGG);
                        output.accept(ItemRegistry.GALLIAN_GROTTO_DISC);
                    }).withLabelColor(0x008000).build());
}
