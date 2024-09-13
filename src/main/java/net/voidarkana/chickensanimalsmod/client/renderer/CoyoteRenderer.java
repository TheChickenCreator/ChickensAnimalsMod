package net.voidarkana.chickensanimalsmod.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.voidarkana.chickensanimalsmod.client.model.CoyoteModel;
import net.voidarkana.chickensanimalsmod.common.entity.custom.CoyoteEntity;
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
