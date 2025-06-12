package chicken.creaturecorner.client.model;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import chicken.creaturecorner.client.animation.GallianAnims;
import chicken.creaturecorner.client.animation.GallianChickAnims;
import chicken.creaturecorner.server.entity.obj.GallianEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GallianModel<T extends GallianEntity> extends HierarchicalModel<T> {

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart neck;
	private final ModelPart eyes;
	private final ModelPart jaw;
	private final ModelPart beard;
	private final ModelPart beard_l;
	private final ModelPart beard_r;
	private final ModelPart eyebrow_l;
	private final ModelPart eyebrow_r;
	private final ModelPart wing_l;
	private final ModelPart claw1_l;
	private final ModelPart claw2_l;
	private final ModelPart wing_r;
	private final ModelPart claw1_r;
	private final ModelPart claw2_r;
	private final ModelPart thigh_l;
	private final ModelPart leg_l;
	private final ModelPart backclaw_l;
	private final ModelPart toes_l;
	private final ModelPart toeback_l;
	private final ModelPart toesfront_l;
	private final ModelPart toe1_l;
	private final ModelPart toe2_l;
	private final ModelPart thigh_r;
	private final ModelPart leg_r;
	private final ModelPart backclaw_r;
	private final ModelPart toes_r;
	private final ModelPart toeback_r;
	private final ModelPart toesfront_r;
	private final ModelPart toe1_r;
	private final ModelPart toe2_r;

	public GallianModel(ModelPart root) {
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.tail = this.body.getChild("tail");
		this.neck = this.body.getChild("neck");
		this.eyes = this.neck.getChild("eyes");
		this.jaw = this.neck.getChild("jaw");
		this.beard = this.jaw.getChild("beard");
		this.beard_l = this.beard.getChild("beard_l");
		this.beard_r = this.beard.getChild("beard_r");
		this.eyebrow_l = this.neck.getChild("eyebrow_l");
		this.eyebrow_r = this.neck.getChild("eyebrow_r");
		this.wing_l = this.body.getChild("wing_l");
		this.claw1_l = this.wing_l.getChild("claw1_l");
		this.claw2_l = this.wing_l.getChild("claw2_l");
		this.wing_r = this.body.getChild("wing_r");
		this.claw1_r = this.wing_r.getChild("claw1_r");
		this.claw2_r = this.wing_r.getChild("claw2_r");
		this.thigh_l = this.root.getChild("thigh_l");
		this.leg_l = this.thigh_l.getChild("leg_l");
		this.backclaw_l = this.leg_l.getChild("backclaw_l");
		this.toes_l = this.leg_l.getChild("toes_l");
		this.toeback_l = this.toes_l.getChild("toeback_l");
		this.toesfront_l = this.toes_l.getChild("toesfront_l");
		this.toe1_l = this.toesfront_l.getChild("toe1_l");
		this.toe2_l = this.toesfront_l.getChild("toe2_l");
		this.thigh_r = this.root.getChild("thigh_r");
		this.leg_r = this.thigh_r.getChild("leg_r");
		this.backclaw_r = this.leg_r.getChild("backclaw_r");
		this.toes_r = this.leg_r.getChild("toes_r");
		this.toeback_r = this.toes_r.getChild("toeback_r");
		this.toesfront_r = this.toes_r.getChild("toesfront_r");
		this.toe1_r = this.toesfront_r.getChild("toe1_r");
		this.toe2_r = this.toesfront_r.getChild("toe2_r");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 22.0F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 29).addBox(-5.5F, -7.0F, -4.0F, 11.0F, 12.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -20.0F, -4.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -4.0F, 10.0F, 6.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9F, 12.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(55, 29).addBox(-3.0F, -20.5F, -3.0F, 6.0F, 23.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(65, 15).addBox(-2.0F, -20.5F, -9.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(65, 26).addBox(-1.0F, -16.5F, -9.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, -3.0F));

		PartDefinition eyes = neck.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(80, 26).addBox(-3.0F, -1.0F, -1.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -18.5F, -2.0F));

		PartDefinition jaw = neck.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(42, 76).addBox(-2.0F, 0.0F, -5.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(-0.009F)), PartPose.offset(0.0F, -18.5F, -3.0F));

		PartDefinition beard = jaw.addOrReplaceChild("beard", CubeListBuilder.create(), PartPose.offset(-1.0F, 2.0F, -2.0F));

		PartDefinition beard_l = beard.addOrReplaceChild("beard_l", CubeListBuilder.create().texOffs(80, 38).addBox(0.0F, 0.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition beard_r = beard.addOrReplaceChild("beard_r", CubeListBuilder.create().texOffs(80, 38).mirror().addBox(0.0F, 0.0F, -1.0F, 0.0F, 6.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

		PartDefinition eyebrow_l = neck.addOrReplaceChild("eyebrow_l", CubeListBuilder.create().texOffs(29, 59).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 4.0F, 11.0F, new CubeDeformation(0.25F)), PartPose.offset(2.0F, -20.5F, -2.0F));

		PartDefinition eyebrow_r = neck.addOrReplaceChild("eyebrow_r", CubeListBuilder.create().texOffs(29, 59).mirror().addBox(-1.0F, -3.0F, -1.0F, 2.0F, 4.0F, 11.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offset(-2.0F, -20.5F, -2.0F));

		PartDefinition wing_l = body.addOrReplaceChild("wing_l", CubeListBuilder.create().texOffs(0, 58).addBox(0.5F, -2.0F, 1.0F, 0.0F, 9.0F, 14.0F, new CubeDeformation(0.01F))
		.texOffs(56, 59).addBox(0.0F, -2.0F, -2.0F, 1.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(5.5F, -3.0F, 0.0F));

		PartDefinition claw1_l = wing_l.addOrReplaceChild("claw1_l", CubeListBuilder.create().texOffs(0, 82).addBox(0.0F, -1.0F, -0.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F)), PartPose.offset(0.5F, 4.0F, -2.5F));

		PartDefinition claw2_l = wing_l.addOrReplaceChild("claw2_l", CubeListBuilder.create().texOffs(80, 48).addBox(0.0F, -1.0F, -0.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F)), PartPose.offset(0.5F, 5.0F, 0.5F));

		PartDefinition wing_r = body.addOrReplaceChild("wing_r", CubeListBuilder.create().texOffs(0, 58).mirror().addBox(-0.5F, -2.0F, 1.0F, 0.0F, 9.0F, 14.0F, new CubeDeformation(0.01F)).mirror(false)
		.texOffs(56, 59).mirror().addBox(-1.0F, -2.0F, -2.0F, 1.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.5F, -3.0F, 0.0F));

		PartDefinition claw1_r = wing_r.addOrReplaceChild("claw1_r", CubeListBuilder.create().texOffs(0, 82).mirror().addBox(0.0F, -1.0F, -0.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-0.5F, 4.0F, -2.5F));

		PartDefinition claw2_r = wing_r.addOrReplaceChild("claw2_r", CubeListBuilder.create().texOffs(80, 48).mirror().addBox(0.0F, -1.0F, -0.5F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-0.5F, 5.0F, 0.5F));

		PartDefinition thigh_l = root.addOrReplaceChild("thigh_l", CubeListBuilder.create().texOffs(65, 0).addBox(-2.5F, -5.0F, -3.0F, 4.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -15.0F, 2.0F));

		PartDefinition leg_l = thigh_l.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(29, 75).addBox(-2.0F, -4.0F, -1.0F, 3.0F, 17.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition backclaw_l = leg_l.addOrReplaceChild("backclaw_l", CubeListBuilder.create().texOffs(78, 70).addBox(0.0F, -3.0F, -1.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.01F)), PartPose.offset(-0.5F, 8.0F, 2.0F));

		PartDefinition toes_l = leg_l.addOrReplaceChild("toes_l", CubeListBuilder.create(), PartPose.offset(-0.5F, 11.5F, 0.5F));

		PartDefinition toeback_l = toes_l.addOrReplaceChild("toeback_l", CubeListBuilder.create().texOffs(80, 30).addBox(0.0F, 0.3F, -1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.2531F, 0.0F, 0.0F));

		PartDefinition toesfront_l = toes_l.addOrReplaceChild("toesfront_l", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.75F, -1.5F, -0.1484F, 0.0F, 0.0F));

		PartDefinition toe1_l = toesfront_l.addOrReplaceChild("toe1_l", CubeListBuilder.create().texOffs(77, 59).addBox(0.0F, -1.5F, -6.0F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.75F, 0.0F, 0.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition toe2_l = toesfront_l.addOrReplaceChild("toe2_l", CubeListBuilder.create().texOffs(63, 76).addBox(0.0F, -1.5F, -6.0F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-0.75F, 0.0F, 0.0F, 0.0F, 0.1745F, 0.0F));

		PartDefinition thigh_r = root.addOrReplaceChild("thigh_r", CubeListBuilder.create().texOffs(65, 0).mirror().addBox(-1.5F, -5.0F, -3.0F, 4.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, -15.0F, 2.0F));

		PartDefinition leg_r = thigh_r.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(29, 75).mirror().addBox(-1.0F, -4.0F, -1.0F, 3.0F, 17.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition backclaw_r = leg_r.addOrReplaceChild("backclaw_r", CubeListBuilder.create().texOffs(78, 70).mirror().addBox(0.0F, -3.0F, -1.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(0.5F, 8.0F, 2.0F));

		PartDefinition toes_r = leg_r.addOrReplaceChild("toes_r", CubeListBuilder.create(), PartPose.offset(0.5F, 11.5F, 0.5F));

		PartDefinition toeback_r = toes_r.addOrReplaceChild("toeback_r", CubeListBuilder.create().texOffs(80, 30).mirror().addBox(0.0F, -0.5F, -1.0F, 0.0F, 2.0F, 5.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.8F, 1.5F, 0.2531F, 0.0F, 0.0F));

		PartDefinition toesfront_r = toes_r.addOrReplaceChild("toesfront_r", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.75F, -1.5F, -0.1484F, 0.0F, 0.0F));

		PartDefinition toe1_r = toesfront_r.addOrReplaceChild("toe1_r", CubeListBuilder.create().texOffs(77, 59).mirror().addBox(0.0F, -1.45F, -6.0F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-0.75F, -0.05F, 0.0F, 0.0F, 0.2618F, 0.0F));

		PartDefinition toe2_r = toesfront_r.addOrReplaceChild("toe2_r", CubeListBuilder.create().texOffs(63, 76).mirror().addBox(0.0F, -1.45F, -6.0F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(0.75F, -0.05F, 0.0F, 0.0F, -0.1745F, 0.0F));

		return LayerDefinition.create(meshdefinition, 96, 96);
	}

	@Override
	public void setupAnim(GallianEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		if (entity.isAggressive()){
			this.applyStatic(GallianAnims.SQUINT);
		}

		if (entity.isSprinting()){
			this.animateWalk(GallianAnims.RUN, limbSwing, limbSwingAmount, 1.5f, 1f);
		}else {
			this.animateWalk(GallianAnims.WALK, limbSwing, limbSwingAmount, 2f, 2f);
		}

		this.animate(entity.idleAnimationState, GallianAnims.IDLE, ageInTicks, 1);
		this.animate(entity.attackAnimationState, GallianAnims.ATTACK, ageInTicks, 1);
		this.animate(entity.peckAnimationState, GallianAnims.PECK, ageInTicks, 1);
		this.animate(entity.idleBlinkAnimationState, GallianAnims.BLINK, ageInTicks, 1);

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