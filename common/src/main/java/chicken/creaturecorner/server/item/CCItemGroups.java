package chicken.creaturecorner.server.item;

import chicken.creaturecorner.platform.Services;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Blocks;


public class CCItemGroups {
    public static final Supplier<CreativeModeTab> dev_tab = register(
            "animal_tab", () -> Services.GENERIC.createTab(CCItems.PIGEON_EGG.get().getDefaultInstance(),
                    (var1, output) -> {
                        output.accept(CCItems.PIGEON_EGG.get());
//                        output.accept(CCItems.NEW_PIGEON_EGG.get());
                        output.accept(CCItems.ENDOVE_SPAWN_EGG.get());
                        output.accept(CCItems.COYOTE_SPAWN_EGG.get());
                        output.accept(CCItems.CARACARA_SPAWN_EGG.get());
                        output.accept(CCItems.GALLIAN_GROTTO_DISC.get());
                    },
                    Component.translatable("creaturecorner.title.animal")));


    private static Supplier<CreativeModeTab> register(String name, Supplier<CreativeModeTab> supplier) {
        Supplier<CreativeModeTab> actual = Suppliers.memoize(supplier);
        Services.PLATFORM.register(BuiltInRegistries.CREATIVE_MODE_TAB, name, supplier);
        return actual;
    }
}
