package chicken.creaturecorner.platform;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.platform.services.IGeneric;
import chicken.creaturecorner.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IGeneric GENERIC = load(IGeneric.class);


    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        CCConstants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}