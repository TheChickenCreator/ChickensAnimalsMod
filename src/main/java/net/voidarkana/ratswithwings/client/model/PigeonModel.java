package net.voidarkana.ratswithwings.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.voidarkana.ratswithwings.RatsWithWings;
import net.voidarkana.ratswithwings.common.entity.custom.PigeonEntity;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import javax.annotation.Nullable;

public class PigeonModel extends GeoModel<PigeonEntity> {
	
	@Override
	public ResourceLocation getModelResource(PigeonEntity pigeonEntity) {
		return new ResourceLocation(RatsWithWings.MOD_ID, "geo/pigeon.geo.json");
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
			case 1-> new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/red.png");
			case 2-> new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/white.png");
			case 3-> new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/end.png");
			default-> new ResourceLocation(RatsWithWings.MOD_ID, "textures/entity/main.png");
		};
	}

	@Override
	public ResourceLocation getAnimationResource(PigeonEntity pigeonEntity) {
		return new ResourceLocation(RatsWithWings.MOD_ID, "animations/pigeon.animation.json");
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