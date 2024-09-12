package net.voidarkana.ratswithwings.client.model;

import com.eliotlash.mclib.utils.MathHelper;
import com.eliotlash.mclib.utils.MathUtils;
import net.minecraft.resources.ResourceLocation;
import net.voidarkana.ratswithwings.RatsWithWings;
import net.voidarkana.ratswithwings.common.entity.custom.CoyoteEntity;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import java.util.Optional;

public class CoyoteModel extends GeoModel<CoyoteEntity> {
    @Override
    public ResourceLocation getModelResource(CoyoteEntity coyoteEntity) {
        return new ResourceLocation(RatsWithWings.MOD_ID, "geo/entity/coyote.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CoyoteEntity coyoteEntity) {
        return new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/coyote/coyote" + (coyoteEntity.getVariant() ? "_al.png" : ".png"));
    }

    @Override
    public ResourceLocation getAnimationResource(CoyoteEntity coyoteEntity) {
        return new ResourceLocation(RatsWithWings.MOD_ID, "animations/entity/coyote.animation.json");
    }

    @Override
    public void setCustomAnimations(CoyoteEntity animatable, long instanceId, AnimationState<CoyoteEntity> animationState) {
        Optional<GeoBone> bone = getBone("head");
        if(bone.isPresent()) {
            GeoBone bone1 = bone.get();
            EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            bone1.setRotX((float) Math.toRadians(data.headPitch()));
            bone1.setRotY((float) Math.toRadians(data.netHeadYaw()));
        }
    }
}
