package net.voidarkana.chickensanimalsmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.voidarkana.chickensanimalsmod.ChickensAnimalsMod;
import net.voidarkana.chickensanimalsmod.client.model.PigeonModel;
import net.voidarkana.chickensanimalsmod.common.entity.custom.PigeonEntity;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PigeonRenderer extends GeoEntityRenderer<PigeonEntity> {
    public PigeonRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PigeonModel());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(PigeonEntity pEntity) {
        return switch (pEntity.getVariant()){
            case 1-> new ResourceLocation(ChickensAnimalsMod.MOD_ID, "textures/entity/red.png");
            case 2-> new ResourceLocation(ChickensAnimalsMod.MOD_ID, "textures/entity/white.png");
            case 3-> new ResourceLocation(ChickensAnimalsMod.MOD_ID, "textures/entity/end.png");
            default-> new ResourceLocation(ChickensAnimalsMod.MOD_ID, "textures/entity/main.png");
        };
    }

    @Override
    public void render(PigeonEntity pEntity, float pEntityYaw, float pPartialTicks, @NotNull PoseStack pPoseStack,
                       @NotNull MultiBufferSource pBuffer, int pPackedLight) {

        if (pEntity.isBaby()){
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public float getMotionAnimThreshold(PigeonEntity animatable) {
        return 0.005F;
    }
}
