package net.voidarkana.ratswithwings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.voidarkana.ratswithwings.RatsWithWings;
import net.voidarkana.ratswithwings.client.layer.ModModelLayer;
import net.voidarkana.ratswithwings.client.model.PigeonModel;
import net.voidarkana.ratswithwings.common.entity.custom.PigeonEntity;

public class PigeonRenderer extends MobRenderer<PigeonEntity, PigeonModel<PigeonEntity>> {
    public PigeonRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PigeonModel<>(pContext.bakeLayer(ModModelLayer.PIGEON_LAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(PigeonEntity pEntity) {
        return new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/main");
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
