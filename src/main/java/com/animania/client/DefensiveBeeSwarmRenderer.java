package com.animania.client;

import com.animania.modern.DefensiveBeeSwarm;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class DefensiveBeeSwarmRenderer extends EntityRenderer<DefensiveBeeSwarm> {
    private final java.util.Map<DefensiveBeeSwarm, SwarmFlightAnimation> flights = new java.util.WeakHashMap<>();
    private final ModelPart model = ModernBeeModel.createBodyLayer().bakeRoot();
    public DefensiveBeeSwarmRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = .3F; }
    @Override public ResourceLocation getTextureLocation(DefensiveBeeSwarm entity) {
        return texture("amber", entity.defenseState() == DefensiveBeeSwarm.PURSUIT);
    }
    private ResourceLocation texture(String breed, boolean angry) {
        return ResourceLocation.fromNamespaceAndPath("animania", "textures/entity/modern/bee_" + breed + (angry ? "_angry" : "") + ".png");
    }
    @Override public void render(DefensiveBeeSwarm entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        String[] breeds = entity.breeds();
        int count = Math.min(18, entity.residentCount() * 6);
        SwarmFlightAnimation flight = flights.get(entity);
        if (flight == null || flight.bees.length != count) {
            flight = new SwarmFlightAnimation(entity, count); flights.put(entity, flight);
        }
        flight.update(entity);
        float time = entity.tickCount + partial;
        ModelPart body = model.getChild("bone");
        net.minecraft.world.phys.Vec3 origin = new net.minecraft.world.phys.Vec3(
                net.minecraft.util.Mth.lerp(partial, entity.xOld, entity.getX()),
                net.minecraft.util.Mth.lerp(partial, entity.yOld, entity.getY()),
                net.minecraft.util.Mth.lerp(partial, entity.zOld, entity.getZ()));
        for (int i = 0; i < count; i++) {
            SwarmFlightAnimation.BeeFlight bee = flight.bees[i];
            var position = bee.previous.lerp(bee.position, partial).subtract(origin);
            var direction = bee.oldVelocity.lerp(bee.velocity, partial);
            float heading = (float)Math.atan2(direction.x, direction.z);
            float pitch = (float)-Math.atan2(direction.y, Math.max(.025, direction.horizontalDistance()));
            float bank = (float)net.minecraft.util.Mth.clamp(bee.oldVelocity.cross(bee.velocity).y * 70, -.5, .5);
            body.getChild("right_wing").zRot = (float)Math.cos(time * bee.wingSpeed + bee.wingPhase) * .95F;
            body.getChild("left_wing").zRot = -body.getChild("right_wing").zRot;
            pose.pushPose();
            pose.translate(position.x, position.y, position.z);
            pose.mulPose(Axis.YP.rotation(heading));
            pose.mulPose(Axis.XP.rotation(pitch));
            pose.mulPose(Axis.ZP.rotation(bank));
            pose.scale(bee.size, -bee.size, -bee.size);
            pose.translate(0, -1.1875, 0);
            model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture(breeds[i % breeds.length], entity.defenseState() == DefensiveBeeSwarm.PURSUIT))), light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        super.render(entity, yaw, partial, pose, buffers, light);
    }
}
