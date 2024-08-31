package net.voidarkana.ratswithwings.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.voidarkana.ratswithwings.client.animations.ModAnimationDefinitions;
import net.voidarkana.ratswithwings.common.entity.custom.PigeonEntity;

public class PigeonModel<T extends Entity> extends HierarchicalModel<T> {

	private final ModelPart pigeon;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart body;
	private final ModelPart wing1;
	private final ModelPart wing2;
	private final ModelPart tail;
	private final ModelPart head;

	public PigeonModel(ModelPart root) {
		this.pigeon = root.getChild("pigeon");
		this.leg1 = pigeon.getChild("leg1");
		this.leg2 = pigeon.getChild("leg2");
		this.body = pigeon.getChild("body");
		this.wing1 = pigeon.getChild("body").getChild("wing1");
		this.wing2 = pigeon.getChild("body").getChild("wing2");
		this.tail = pigeon.getChild("body").getChild("tail");
		this.head = pigeon.getChild("body").getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition pigeon = partdefinition.addOrReplaceChild("pigeon", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition leg1 = pigeon.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(2, 3).addBox(-0.5F, -1.15F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(18, 21).addBox(-1.5F, 1.85F, -3.0F, 3.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, -1.85F, 0.0F));

		PartDefinition leg2 = pigeon.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 3).addBox(-0.5F, -1.15F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(10, 15).addBox(-1.5F, 1.85F, -3.0F, 3.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, -1.85F, 0.0F));

		PartDefinition body = pigeon.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, -1.0F));

		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.5F, -4.0F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition wing1 = body.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(22, 8).addBox(-0.5F, -3.0F, 0.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(12, 26).addBox(0.0F, -3.0F, 4.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -1.0F, -4.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition wing2 = body.addOrReplaceChild("wing2", CubeListBuilder.create().texOffs(13, 19).addBox(-0.5F, -3.0F, 0.0F, 1.0F, 6.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(0.0F, -3.0F, 4.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -1.0F, -4.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(13, 0).addBox(-3.0F, 0.0F, -1.2F, 6.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 4.2F, -0.5672F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(25, 28).addBox(-2.0F, -6.8333F, -2.6667F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-2.0F, -1.8333F, -4.6667F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.5F, -5.8333F, -4.6667F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.1667F, -2.3333F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch, ageInTicks);

		PigeonEntity pigeon = (PigeonEntity) entity;
		this.animateWalk(pigeon.getLastHurtByMob() != null || pigeon.isFreezing() || pigeon.isOnFire() ? ModAnimationDefinitions.RUN_BAKED
				: pigeon.onGround() ? ModAnimationDefinitions.WALK_BAKED : ModAnimationDefinitions.FLY_BAKED,
				limbSwing, limbSwingAmount, 2f, 2.5f);
	}

	private void applyHeadRotation(float pNetHeadYaw, float pHeadPitch, float pAgeInTicks) {
		pNetHeadYaw = Mth.clamp(pNetHeadYaw, -30.0F, 30.0F);
		pHeadPitch = Mth.clamp(pHeadPitch, -25.0F, 45.0F);

		this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
		this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		pigeon.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return pigeon;
	}
}