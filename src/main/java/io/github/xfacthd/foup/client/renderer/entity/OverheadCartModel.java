package io.github.xfacthd.foup.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.xfacthd.foup.common.entity.OverheadCartEntity;
import io.github.xfacthd.foup.common.util.Utils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class OverheadCartModel extends EntityModel<OverheadCartEntity>
{
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Utils.rl("overheadcart"), "main");

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart hoistWire;
	private final ModelPart gripper;
	private final ModelPart foup;

	public OverheadCartModel(ModelPart root)
	{
		this.body = root.getChild("body");
        this.root = root;
		this.hoistWire = body.getChild("hoist_wire");
		this.gripper = body.getChild("gripper");
		this.foup = gripper.getChild("foup");
	}

	// Hoist wire PartPose y offset must be reduced by 2 from the exported value
	public static LayerDefinition createBodyLayer()
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition body = root.addOrReplaceChild(
				"body", 
				CubeListBuilder.create()
						.texOffs(5, 0).addBox(-6.5F, 6.0F, -10.0F, 12.0F, 14.0F, 2.0F, CubeDeformation.NONE)
						.texOffs(34, 0).addBox(-6.5F, 6.0F, 4.0F, 12.0F, 14.0F, 2.0F, CubeDeformation.NONE)
						.texOffs(0, 46).addBox(-6.5F, 4.0F, -10.0F, 12.0F, 2.0F, 16.0F, CubeDeformation.NONE)
						.texOffs(1, 20).addBox(-1.5F, 0.0F, -3.0F, 2.0F, 4.0F, 2.0F, CubeDeformation.NONE),
				PartPose.offset(0.5F, 4.0F, 2.0F)
		);

		PartDefinition carriage = body.addOrReplaceChild(
				"carriage",
				CubeListBuilder.create()
						.texOffs(34, 17).addBox(-1.5F, -1.0F, -6.0F, 2.0F, 2.0F, 8.0F, CubeDeformation.NONE)
						.texOffs(0, 53).addBox(-2.0F, -0.5F, -5.5F, 3.0F, 1.0F, 1.0F, CubeDeformation.NONE)
						.texOffs(0, 53).addBox(-2.0F, -0.5F, 0.5F, 3.0F, 1.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, 0.0F, 0.0F)
		);

		carriage.addOrReplaceChild(
				"wheel_front_left",
				CubeListBuilder.create().texOffs(0, 46).addBox(-1.5F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F, CubeDeformation.NONE),
				PartPose.offset(2.5F, 0.0F, -5.0F)
		);

		carriage.addOrReplaceChild(
				"wheel_front_right",
				CubeListBuilder.create().texOffs(0, 46).addBox(0.5F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F, CubeDeformation.NONE),
				PartPose.offset(-3.5F, 0.0F, -5.0F)
		);

		carriage.addOrReplaceChild(
				"wheel_back_left",
				CubeListBuilder.create().texOffs(0, 46).addBox(-1.5F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F, CubeDeformation.NONE),
				PartPose.offset(2.5F, 0.0F, 1.0F)
		);

		carriage.addOrReplaceChild(
				"wheel_back_right",
				CubeListBuilder.create().texOffs(0, 46).addBox(0.5F, -1.5F, -1.5F, 1.0F, 3.0F, 3.0F, CubeDeformation.NONE),
				PartPose.offset(-3.5F, 0.0F, 1.0F)
		);

		PartDefinition gripper = body.addOrReplaceChild(
				"gripper",
				CubeListBuilder.create().texOffs(0, 17).addBox(-5.5F, -6.0F, -5.5F, 11.0F, 6.0F, 11.0F, CubeDeformation.NONE),
				PartPose.offset(-0.5F, 12.0F, -2.0F)
		);

		gripper.addOrReplaceChild(
				"foup",
				CubeListBuilder.create()
						.texOffs(35, 28).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 7.0F, 10.0F, CubeDeformation.NONE)
						.texOffs(0, 35).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 2.0F, 8.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, 1.0F, 0.0F)
		);

		body.addOrReplaceChild(
				"hoist_wire",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(0.0F, 0.0F, -1.0F, 1.0F, 16.0F, 1.0F, CubeDeformation.NONE)
						.texOffs(0, 0).addBox(-7.0F, 0.0F, -1.0F, 1.0F, 16.0F, 1.0F, CubeDeformation.NONE)
						.texOffs(0, 0).addBox(0.0F, 0.0F, 6.0F, 1.0F, 16.0F, 1.0F, CubeDeformation.NONE)
						.texOffs(0, 0).addBox(-7.0F, 0.0F, 6.0F, 1.0F, 16.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offset(2.5F, 6.0F, -5.0F)
		);

		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(OverheadCartEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		root.getAllParts().forEach(ModelPart::resetPose);

		foup.visible = entity.getHasFoup();

		float off = switch (entity.getState())
		{
			case LOWERING_HOIST -> computeDistance(entity, computeMultiplier(entity, ageInTicks, false));
			case RAISING_HOIST -> computeDistance(entity, computeMultiplier(entity, ageInTicks, true));
			case POD_IN_LOADER_OR_STORAGE -> computeDistance(entity, 1F);
			default -> 0F;
		};

		gripper.y += off;
		hoistWire.yScale = off / 16F;
	}

	private static float computeMultiplier(OverheadCartEntity entity, float ageInTicks, boolean inverse)
	{
		int start = entity.getActionStart();
		int duration = entity.getActionDuration();
		if (start != -1 && duration != 0)
		{
			float mult = (ageInTicks - start) / duration;
			return Math.clamp(inverse ? (1F - mult) : mult, 0F, 1F);
		}
		return inverse ? 1F : 0F;
	}

	private static float computeDistance(OverheadCartEntity entity, float mult)
	{
		return OverheadCartEntity.calculateHoistDistance(entity.getHeightDiff()) * mult;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color)
	{
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}
