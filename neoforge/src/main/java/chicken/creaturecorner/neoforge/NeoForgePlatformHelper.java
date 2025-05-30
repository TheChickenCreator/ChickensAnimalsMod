package chicken.creaturecorner.neoforge;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.platform.services.IPlatformHelper;
import com.google.common.base.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.HashMap;

public class NeoForgePlatformHelper implements IPlatformHelper {
    private static final HashMap<Registry, HashMap<String, Supplier<?>>> arrayListHashMap = new HashMap<>();

    @Override
    public <T> void register(Registry<T> registry, String name, Supplier<T> supplier) {
        var list = arrayListHashMap.getOrDefault(registry, new HashMap<>());
        list.put(name, supplier);
        arrayListHashMap.put(registry, list);
    }

    @SubscribeEvent
    public static void registerEvent(RegisterEvent event) {
        arrayListHashMap.forEach((objects, stringSupplierHashMap) -> {
            event.register(objects.key(), (registerHelper -> {
                stringSupplierHashMap.forEach((string, supplier) -> {
                    registerHelper.register(ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, string), supplier.get());
                });
            }));
        });
    }

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }
}