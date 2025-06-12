package chicken.creaturecorner.client.renderer.obj;

import chicken.creaturecorner.CCCommon;
import chicken.creaturecorner.CCConstants;
import chicken.creaturecorner.client.CCModelLayers;
import chicken.creaturecorner.client.model.GallianChickModel;
import chicken.creaturecorner.client.model.GallianModel;
import chicken.creaturecorner.server.entity.obj.GallianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GallianRenderer extends MobRenderer<GallianEntity, HierarchicalModel<GallianEntity>> {

    private final GallianModel<GallianEntity> gallianModel;
    private final GallianChickModel<GallianEntity> gallianChickModel;

    public GallianRenderer(EntityRendererProvider.Context context) {
        super(context, new GallianModel<>(context.bakeLayer(CCModelLayers.GALLIAN)), 0.75f);

        gallianModel = new GallianModel<>(context.bakeLayer(CCModelLayers.GALLIAN));
        gallianChickModel = new GallianChickModel<>(context.bakeLayer(CCModelLayers.GALLIAN_CHICK));
    }

    @Override
    public void render(GallianEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        if (entity.isBaby()){
            this.model = gallianChickModel;
        }else {
            this.model = gallianModel;
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GallianEntity gallianEntity) {
        return ResourceLocation.fromNamespaceAndPath(CCConstants.MOD_ID,
                "textures/entity/gallian/gallian" + (gallianEntity.isBaby() ? "_chick" : "") + ".png");
    }
}
