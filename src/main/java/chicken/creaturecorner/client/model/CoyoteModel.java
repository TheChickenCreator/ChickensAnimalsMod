package chicken.creaturecorner.client.model;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.server.entity.obj.CoyoteEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import javax.annotation.Nullable;

public class CoyoteModel extends GeoModel<CoyoteEntity> {

    @Override
    public ResourceLocation getModelResource(CoyoteEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "geo/animal/coyote/coyote_baby.geo.json");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "geo/animal/coyote/coyote.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(CoyoteEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "textures/entity/coyote/coyote_baby_"+object.getVariantName()+".png");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "textures/entity/coyote/coyote_"+object.getVariantName()+".png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(CoyoteEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "animations/animal/coyote/coyote_baby.animation.json");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "animations/animal/coyote/coyote.animation.json");
        }
    }

    @Override
    public void setCustomAnimations(CoyoteEntity object, long instanceId, @Nullable AnimationState<CoyoteEntity> animationEvent) {

        super.setCustomAnimations(object, instanceId, animationEvent);

        if (animationEvent == null) return;

        GeoBone head = this.getAnimationProcessor().getBone("head_rot");

        EntityModelData entityData = animationEvent.getData(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX((entityData.headPitch() * ((float) Math.PI / 180F)));
        head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
    }

    @Override
    public @org.jetbrains.annotations.Nullable RenderType getRenderType(CoyoteEntity animatable, ResourceLocation texture) {
        return RenderType.entityCutout(texture);
    }
}
