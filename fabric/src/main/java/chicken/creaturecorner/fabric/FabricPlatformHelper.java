package chicken.creaturecorner.fabric;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.platform.services.IPlatformHelper;
import com.google.common.base.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public <T> void register(Registry<T> registry, String name, Supplier<T> supplier) {
        Registry.register(registry, ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, name), supplier.get());
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
