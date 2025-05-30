package chicken.creaturecorner.platform.services;

import com.google.common.base.Supplier;
import net.minecraft.core.Registry;


public interface IPlatformHelper {

    <T> void register(Registry<T> registry, String name, Supplier<T> tSupplier);

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }
}