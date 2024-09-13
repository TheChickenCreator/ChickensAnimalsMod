package net.voidarkana.chickensanimalsmod.client.model;

import net.minecraft.resources.ResourceLocation;
import net.voidarkana.chickensanimalsmod.ChickensAnimalsMod;
import net.voidarkana.chickensanimalsmod.common.entity.custom.PigeonEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import javax.annotation.Nullable;

public class PigeonModel extends GeoModel<PigeonEntity> {
	
	@Override
	public ResourceLocation getModelResource(PigeonEntity pigeonEntity) {
		return new ResourceLocation(ChickensAnimalsMod.MOD_ID, "geo/pigeon.geo.json");
//		if (pigeonEntity.isBaby()){
//			return new ResourceLocation(RatsWithWings.MOD_ID, "geo/baby_dodo.geo.json");
//		}
//		else {
//			return new ResourceLocation(RatsWithWings.MOD_ID, "geo/dodo.geo.json");
//		}
	}

	@Override
	public ResourceLocation getTextureResource(PigeonEntity pEntity) {
		return switch (pEntity.getVariant()){
			case 1-> new ResourceLocation(ChickensAnimalsMod.MOD_ID, "textures/entity/red.png");
			case 2-> new ResourceLocation(ChickensAnimalsMod.MOD_ID, "textures/entity/white.png");
			case 3-> new ResourceLocation(ChickensAnimalsMod.MOD_ID, "textures/entity/end.png");
			default-> new ResourceLocation(ChickensAnimalsMod.MOD_ID, "textures/entity/main.png");
		};
	}

	@Override
	public ResourceLocation getAnimationResource(PigeonEntity pigeonEntity) {
		return new ResourceLocation(ChickensAnimalsMod.MOD_ID, "animations/pigeon.animation.json");
	}

	@Override
	public void setCustomAnimations(PigeonEntity dodo, long instanceId, @Nullable AnimationState<PigeonEntity> animationEvent) {

		super.setCustomAnimations(dodo, instanceId, animationEvent);

		if (animationEvent == null) return;

		CoreGeoBone head = this.getAnimationProcessor().getBone("head_look");

		EntityModelData entityData = animationEvent.getData(DataTickets.ENTITY_MODEL_DATA);
		head.setRotX(entityData.headPitch() * ((float) Math.PI / 180F));
		head.setRotY(entityData.netHeadYaw() * ((float) Math.PI / 180F));
	}
}