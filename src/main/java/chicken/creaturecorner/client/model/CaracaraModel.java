package chicken.creaturecorner.client.model;

import chicken.creaturecorner.AnimalMod;
import chicken.creaturecorner.server.entity.obj.CaracaraEntity;
import chicken.creaturecorner.server.entity.obj.CoyoteEntity;
import chicken.creaturecorner.server.entity.obj.PigeonEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import javax.annotation.Nullable;

public class CaracaraModel extends GeoModel<CaracaraEntity> {

    @Override
    public ResourceLocation getModelResource(CaracaraEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "geo/animal/caracara/caracara_baby.geo.json");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "geo/animal/caracara/caracara.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(CaracaraEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "textures/entity/caracara/caracara_baby.png");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "textures/entity/caracara/caracara.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(CaracaraEntity object) {
        if (object.isBaby()){
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "animations/animal/caracara/caracara_baby.animation.json");
        }
        else {
            return ResourceLocation.fromNamespaceAndPath(AnimalMod.MODID, "animations/animal/caracara/caracara.animation.json");
        }
    }

    public void setCustomAnimations(CaracaraEntity object, long instanceId, @Nullable AnimationState<CaracaraEntity> animationEvent) {
        super.setCustomAnimations(object, instanceId, animationEvent);
        if (animationEvent != null) {
            GeoBone head = this.getAnimationProcessor().getBone("look");
            GeoBone flyRot = this.getAnimationProcessor().getBone("fly_rot");
            EntityModelData entityData = (EntityModelData)animationEvent.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * ((float)Math.PI / 180F) * 0.75F);
            head.setRotY(entityData.netHeadYaw() * ((float)Math.PI / 180F) * 0.75F);

            if (flyRot != null){
                if (object.isFlying() && !object.isBaby() && !object.onGround()) {
                    flyRot.setRotX(-object.currentPitch);
                    flyRot.setRotZ(object.currentRoll);
                }else {
                    flyRot.setRotX(0);
                    flyRot.setRotZ(0);
                }
            }

        }
    }

}
