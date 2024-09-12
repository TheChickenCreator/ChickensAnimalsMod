package net.voidarkana.ratswithwings.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.voidarkana.ratswithwings.client.model.CoyoteModel;
import net.voidarkana.ratswithwings.common.entity.custom.CoyoteEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CoyoteRenderer extends GeoEntityRenderer<CoyoteEntity> {
    public CoyoteRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CoyoteModel());
    }


    @Override
    public float getMotionAnimThreshold(CoyoteEntity animatable) {
        return 0.005F;
    }
}
