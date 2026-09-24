package com.animania.client;

import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GoatRenderer;
import net.minecraft.world.entity.animal.goat.Goat;

public final class MountainGoatRenderer extends GoatRenderer {
    public MountainGoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new RestingModel(ModernWildlifeModels.mountainGoat().bakeRoot());
    }

    @Override public net.minecraft.resources.ResourceLocation getTextureLocation(Goat entity) {
        if (!(entity instanceof com.animania.modern.MountainGoat goat))
            return super.getTextureLocation(entity);
        return net.minecraft.resources.ResourceLocation.tryParse("animania:textures/entity/modern/goat_" + goat.coatName() + ".png");
    }

    private static final class RestingModel extends GoatModel<Goat> {
        private final ModelPart root;
        private RestingModel(ModelPart root) {
            super(root);
            this.root = root;
        }

        @Override public void setupAnim(Goat goat, float swing, float amount, float age,
                float yaw, float pitch) {
            root.getAllParts().forEach(ModelPart::resetPose);
            if (goat instanceof com.animania.modern.MountainGoat mountain) {
                float hornScale = mountain.isFemale() ? 0.8F : 1.1F;
                head.getChild("left_horn").yScale = hornScale;
                head.getChild("right_horn").yScale = hornScale;
            }
            float sleep = LegacySleepAnimation.petBlend(goat, age - goat.tickCount);
            float rumination = com.animania.common.registry.ModAttachments.getData(goat, com.animania.common.registry.ModAttachments.FARM_ACTIVITY)
                    == com.animania.common.entity.ai.FarmActivityGoal.RUMINATE
                    && !com.animania.common.registry.ModAttachments.getData(goat, com.animania.common.registry.ModAttachments.SLEEPING)
                    ? FarmActivityAnimation.blend(goat, age - goat.tickCount) : 0;
            float blend = Math.max(sleep, rumination);
            super.setupAnim(goat, swing, amount * (1 - blend), age, yaw * (1 - blend), pitch * (1 - blend));
            for (String name : new String[]{"head", "body", "left_front_leg", "right_front_leg", "left_hind_leg", "right_hind_leg"})
                root.getChild(name).y += 6 * blend;
            if (com.animania.common.registry.ModAttachments.getData(goat, com.animania.common.registry.ModAttachments.EATING_TICKS) > 0)
                head.xRot = 0.55F + net.minecraft.util.Mth.sin(age * 0.35F) * 0.08F;
            head.xRot = net.minecraft.util.Mth.lerp(blend, head.xRot, 0.25F);
            if (rumination > 0) {
                head.getChild("nose").x += 0.10F * net.minecraft.util.Mth.sin(age * 0.24F) * rumination;
                head.xRot += 0.02F * net.minecraft.util.Mth.sin(age * 0.12F) * rumination;
            }
            leftFrontLeg.xRot = net.minecraft.util.Mth.lerp(blend, leftFrontLeg.xRot, 1.5708F);
            rightFrontLeg.xRot = net.minecraft.util.Mth.lerp(blend, rightFrontLeg.xRot, 1.5708F);
            leftHindLeg.xRot = net.minecraft.util.Mth.lerp(blend, leftHindLeg.xRot, -1.5708F);
            rightHindLeg.xRot = net.minecraft.util.Mth.lerp(blend, rightHindLeg.xRot, -1.5708F);
            FamilyBehaviorAnimation.applyNative(goat, root, age - goat.tickCount);
        }
    }
}
