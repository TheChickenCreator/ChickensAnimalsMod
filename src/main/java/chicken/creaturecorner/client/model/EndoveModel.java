package chicken.creaturecorner.client.model;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.server.entity.obj.EndoveEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import javax.annotation.Nullable;

public class EndoveModel extends GeoModel<EndoveEntity> {

    @Override
    public ResourceLocation getModelResource(EndoveEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "geo/animal/pigeon/pigeon_baby.geo.json");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "geo/animal/pigeon/pigeon.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(EndoveEntity object) {

        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "textures/entity/pigeon/pigeon_baby_end.png");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "textures/entity/pigeon/pigeon_end.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(EndoveEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "animations/animal/pigeon/pigeon_baby.animation.json");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "animations/animal/pigeon/pigeon.animation.json");
        }
    }

    @Override
    public void setCustomAnimations(EndoveEntity object, long instanceId, @Nullable AnimationState<EndoveEntity> animationEvent) {

        super.setCustomAnimations(object, instanceId, animationEvent);

        if (animationEvent == null) return;

        GeoBone head = this.getAnimationProcessor().getBone("head_look");

        EntityModelData entityData = animationEvent.getData(DataTickets.ENTITY_MODEL_DATA);
        head.setRotX(-(entityData.headPitch() * ((float) Math.PI / 180F)));
        head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
    }

}
