package com.animania.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Independent geometry with the native bone names used by fox and goat animations. */
public final class ModernWildlifeModels {
    private ModernWildlifeModels() {}

    public static LayerDefinition fox() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 5)
                .addBox(-3, -2, -5, 8, 6, 6, new CubeDeformation(-0.45F, -0.35F, 0)),
                PartPose.offset(-1, 16.5F, -3));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(6, 18)
                .addBox(-1, 2.01F, -8, 4, 2, 3, new CubeDeformation(-0.25F, 0, 0.5F)), PartPose.ZERO);
        for (int side : new int[]{-1, 1}) {
            String suffix = side < 0 ? "right" : "left";
            float x = side < 0 ? -2.5F : 3.5F;
            var ear = head.addOrReplaceChild(suffix + "_ear", CubeListBuilder.create()
                    .texOffs(side < 0 ? 8 : 15, 1).addBox(-1, -2.7F, -0.5F, 2, 2, 1, new CubeDeformation(0, 0.5F, 0)),
                    PartPose.offsetAndRotation(x, -1.7F, -3.5F, -0.12F, 0, side * 0.12F));
            ear.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(9, 1)
                    .addBox(-0.5F, -1, -0.5F, 1, 1, 1), PartPose.offset(0, -2.7F, 0));
            head.addOrReplaceChild(suffix + "_cheek", CubeListBuilder.create().texOffs(24, 18)
                    .addBox(-1, -1, -1, 2, 3, 3),
                    PartPose.offsetAndRotation(side < 0 ? -2.7F : 4.7F, 1.5F, -0.7F, 0, side * -0.22F, 0));
        }
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(24, 15)
                .addBox(-3, 3.999F, -3.5F, 6, 11, 6, new CubeDeformation(-0.25F, 0.4F, -0.25F)),
                PartPose.offsetAndRotation(0, 16, -6, (float) Math.PI / 2, 0, 0));
        body.addOrReplaceChild("chest_ruff", CubeListBuilder.create().texOffs(24, 15)
                .addBox(-3, 3.5F, -3.3F, 6, 4, 6, new CubeDeformation(0.5F, 0, 0)), PartPose.ZERO);
        var tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 0)
                .addBox(2, 0, -1, 4, 9, 5, new CubeDeformation(0.4F, 0, 0.3F)),
                PartPose.offsetAndRotation(-4, 15, -1, -0.05236F, 0, 0));
        tail.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(35, 9)
                .addBox(-1.5F, 0, -1.5F, 3, 2, 3), PartPose.offset(4, 8.5F, 1.5F));
        for (String leg : new String[]{"right_hind_leg", "left_hind_leg", "right_front_leg", "left_front_leg"}) {
            boolean left = leg.startsWith("left");
            root.addOrReplaceChild(leg, CubeListBuilder.create().texOffs(left ? 4 : 13, 24)
                    .addBox(2, 0.5F, -1, 2, 6, 2, new CubeDeformation(-0.1F, 0, -0.1F)),
                    PartPose.offset(left ? -1 : -5, 17.5F, leg.contains("hind") ? 7 : 0));
        }
        return LayerDefinition.create(mesh, 48, 32);
    }

    public static LayerDefinition mountainGoat() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        var head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(2, 61).addBox(-5, -11, -10, 3, 2, 1)
                .texOffs(2, 61).mirror().addBox(2, -11, -10, 3, 2, 1), PartPose.offset(1, 14, 0));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(34, 46)
                .addBox(-3, -4, -8, 5, 7, 10, new CubeDeformation(-0.25F, -0.15F, -0.2F)),
                PartPose.offsetAndRotation(0, -8, -8, 0.9599F, 0, 0));
        head.addOrReplaceChild("beard", CubeListBuilder.create().texOffs(17, 17)
                .addBox(-0.75F, -3, -13, 1, 4, 3), PartPose.ZERO);
        for (int side : new int[]{-1, 1}) {
            var horn = head.addOrReplaceChild(side < 0 ? "right_horn" : "left_horn",
                    CubeListBuilder.create().texOffs(12, 55).addBox(-0.6F, -5, -0.6F, 1, 5, 1),
                    PartPose.offsetAndRotation(side < 0 ? -2 : 1, -9, -9, -0.1F, 0, side * 0.08F));
            horn.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(12, 55)
                    .addBox(-0.5F, -3, -0.5F, 1, 3, 1),
                    PartPose.offsetAndRotation(0, -5, 0, -0.28F, 0, side * 0.07F));
        }
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-4, -17, -7, 9, 11, 16, new CubeDeformation(-0.2F, 0, -0.3F))
                .texOffs(0, 28).addBox(-5, -18, -8, 11, 14, 11, new CubeDeformation(0.2F, 0.7F, 0.2F)),
                PartPose.offset(0, 24, 0));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(36, 29)
                .addBox(0, 4, 0, 3, 6, 3), PartPose.offset(1, 14, 4));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(49, 29)
                .addBox(0, 4, 0, 3, 6, 3), PartPose.offset(-3, 14, 4));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(49, 2)
                .addBox(0, 0, 0, 3, 10, 3), PartPose.offset(1, 14, -6));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(35, 2)
                .addBox(0, 0, 0, 3, 10, 3), PartPose.offset(-3, 14, -6));
        root.getChild("body").addOrReplaceChild("tail", CubeListBuilder.create().texOffs(17, 17)
                .addBox(-1.5F, 0, -1, 3, 3, 2), PartPose.offsetAndRotation(0.5F, -13, 8, 0.35F, 0, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
