package chicken.creaturecorner.client.renderer.obj;

import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.server.entity.obj.EntityHolder;
import chicken.creaturecorner.server.entity.obj.geo.GeoEntityBase;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Objects;

public class GeoBaseEntityRenderer extends GeoEntityRenderer<GeoEntityBase> {
    @Override
    public float getMotionAnimThreshold(GeoEntityBase animatable) {
        return 0.005F;
    }

    public GeoBaseEntityRenderer(EntityRendererProvider.Context renderManager, EntityHolder<?> entityHolder) {
        super(renderManager, new GeoModel<>() {
            @Override
            public ResourceLocation getModelResource(GeoEntityBase animatable) {
                String entityName = entityHolder.name();
                if (animatable.hasChildModel() && animatable.isBaby()) {
                    return ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "geo/animal/" + entityName + "/" + entityName + "_baby.geo.json");
                } else {
                    return ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "geo/animal/" + entityName + "/" + entityName + ".geo.json");
                }
            }

            @Override
            public ResourceLocation getTextureResource(GeoEntityBase animatable) {
                String entityName = entityHolder.name();
                String variantName = animatable.getVariantName();
                if (!Objects.equals(variantName, "")) {
                    variantName = "_" + variantName;
                }

                if (animatable.hasChildModel() && animatable.isBaby()) {
                    return ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "textures/animal/" + entityName + "/" + entityName + (animatable.childVariants() ? variantName : "") + "_baby.png");
                } else {
                    return ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "textures/animal/" + entityName + "/" + entityName + variantName + ".png");
                }
            }

            @Override
            public ResourceLocation getAnimationResource(GeoEntityBase animatable) {
                String entityName = entityHolder.name();
                if (animatable.hasChildModel() && animatable.isBaby()) {
                    return ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "animations/animal/" + entityName + "/" + entityName + "_baby.animation.json");
                } else {
                    return ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID, "animations/animal/" + entityName + "/" + entityName + ".animation.json");
                }
            }
        });
    }
}
