package chicken.creaturecorner;

import chicken.creaturecorner.server.entity.AnimalModEntities;
import chicken.creaturecorner.server.item.ItemRegistry;
import chicken.creaturecorner.server.item.creativegroup.CreativeGroupRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(AnimalMod.MODID)
public class AnimalMod {
    public static final String MODID = "creaturecorner";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AnimalMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(AnimalModEntities::registerEvent);
        modEventBus.addListener(AnimalModEntities::registerAttributes);
        modEventBus.addListener(AnimalModEntities::registerAdditionalSpawns);
        CreativeGroupRegistry.TAB_DEFERRED_REGISTER.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
    }
}
