package chicken.creaturecorner.server.entity.obj;

import software.bernie.geckolib.animatable.GeoEntity;

public interface GeoEntityInt extends GeoEntity {
    default boolean hasChildModel() {
        return false;
    }

    default String getVariantName() {
        return "";
    }
}
