package net.voidarkana.ratswithwings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.voidarkana.ratswithwings.RatsWithWings;
import net.voidarkana.ratswithwings.client.model.PigeonModel;
import net.voidarkana.ratswithwings.common.entity.custom.PigeonEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PigeonRenderer extends GeoEntityRenderer<PigeonEntity> {
    public PigeonRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PigeonModel());
    }

    @Override
    public ResourceLocation getTextureLocation(PigeonEntity pEntity) {
        return switch (pEntity.getVariant()){
            case 1-> new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/red.png");
            case 2-> new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/white.png");
            case 3-> new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/end.png");
            default-> new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/main.png");
        };
    }

    @Override
    public void render(PigeonEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack,
                       MultiBufferSource pBuffer, int pPackedLight) {

        if (pEntity.isBaby()){
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }
}
