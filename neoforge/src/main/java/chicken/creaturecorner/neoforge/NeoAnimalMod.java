package chicken.creaturecorner.neoforge;

import chicken.creaturecorner.CCCommon;
import chicken.creaturecorner.CCConstants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(CCConstants.MOD_ID)
public class NeoAnimalMod {

    public NeoAnimalMod(IEventBus modEventBus, ModContainer modContainer) {
        CCCommon.init();
        modEventBus.addListener(NeoForgePlatformHelper::registerEvent);
    }
}
