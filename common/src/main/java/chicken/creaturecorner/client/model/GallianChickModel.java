package chicken.creaturecorner.client.model;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import chicken.creaturecorner.client.animation.GallianChickAnims;
import chicken.creaturecorner.server.entity.obj.GallianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GallianChickModel<T extends GallianEntity> extends HierarchicalModel<T> {

	private final ModelPart root;
	private final ModelPart leg_l;
	private final ModelPart leg_r;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart wing_l;
	private final ModelPart wing_r;
	private final ModelPart head;

	public GallianChickModel(ModelPart root) {
		this.root = root.getChild("root");
		this.leg_l = this.root.getChild("leg_l");
		this.leg_r = this.root.getChild("leg_r");
		this.body = this.root.getChild("body");
		this.tail = this.body.getChild("tail");
		this.wing_l = this.body.getChild("wing_l");
		this.wing_r = this.body.getChild("wing_r");
		this.head = this.body.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(27, 0).addBox(-0.5F, -2.0F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.001F)), PartPose.offset(1.5F, -2.0F, 1.0F));

		PartDefinition cube_r1 = leg_l.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(5, 24).addBox(-1.0F, -0.075F, -1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(27, 0).mirror().addBox(-0.5F, -2.0F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offset(-1.5F, -2.0F, 1.0F));

		PartDefinition cube_r2 = leg_r.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(5, 24).mirror().addBox(-1.0F, 0.025F, -1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.001F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.9F, -1.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -2.0F, 6.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -1.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 24).addBox(0.0F, -4.0F, 0.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, -2.0F, 4.0F));

		PartDefinition wing_l = body.addOrReplaceChild("wing_l", CubeListBuilder.create().texOffs(19, 22).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -2.0F, -1.0F));

		PartDefinition wing_r = body.addOrReplaceChild("wing_r", CubeListBuilder.create().texOffs(19, 22).mirror().addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, -2.0F, -1.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14).addBox(-2.0F, -2.0167F, -4.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(19, 14).addBox(-2.0F, -5.0167F, -3.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 30).addBox(0.0F, -7.0167F, -2.0F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.001F))
		.texOffs(14, 24).addBox(-0.5F, -4.0167F, -4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.9833F, 0.0F));

		return LayerDefinition.create(meshdefinition, 48, 48);
	}

	@Override
	public void setupAnim(GallianEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		this.head.xRot = headPitch * ((float)Math.PI / 180F);
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);

		this.animate(entity.idleAnimationState, GallianChickAnims.IDLE, ageInTicks, 1);
		this.animate(entity.peckAnimationState, GallianChickAnims.PECK, ageInTicks, 1);
		this.animate(entity.idleBlinkAnimationState, GallianChickAnims.TAIL_SHAKE, ageInTicks, 1);

		this.animateWalk(GallianChickAnims.WALK, limbSwing*3, limbSwingAmount*4, 1f, 2f);

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}