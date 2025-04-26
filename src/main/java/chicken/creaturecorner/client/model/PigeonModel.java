package chicken.creaturecorner.client.model;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import javax.annotation.Nullable;

public class PigeonModel extends GeoModel<PigeonEntity> {

    @Override
    public ResourceLocation getModelResource(PigeonEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "geo/animal/pigeon/pigeon_baby.geo.json");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "geo/animal/pigeon/pigeon.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(PigeonEntity object) {


        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "textures/entity/pigeon/pigeon_baby_"+object.getVariantName()+".png");
        }
        else {
            String s = ChatFormatting.stripFormatting(object.getName().getString());

            if (s.toLowerCase().equals("cannoli")){
                return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "textures/entity/pigeon/pigeon_canolli.png");
            }

            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "textures/entity/pigeon/pigeon_"+object.getVariantName()+".png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(PigeonEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "animations/animal/pigeon/pigeon_baby.animation.json");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "animations/animal/pigeon/pigeon.animation.json");
        }
    }

    @Override
    public void setCustomAnimations(PigeonEntity object, long instanceId, @Nullable AnimationState<PigeonEntity> animationEvent) {

        super.setCustomAnimations(object, instanceId, animationEvent);

        if (animationEvent == null) return;

        GeoBone head = this.getAnimationProcessor().getBone("head_look");

        EntityModelData entityData = animationEvent.getData(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(-(entityData.headPitch() * ((float) Math.PI / 180F)));
        head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
    }

}
