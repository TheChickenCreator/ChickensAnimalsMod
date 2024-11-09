package chicken.chickensanimalmod.server.entity.obj;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;

public interface GeoEntityInt extends GeoEntity {
    default boolean hasChildModel() {
        return false;
    }

    default String getVariantName() {
        return "";
    }
}
