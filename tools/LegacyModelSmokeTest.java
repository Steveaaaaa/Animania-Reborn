package com.animania.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

/** Standalone verifier for every mechanically converted legacy model resource. */
public final class LegacyModelSmokeTest {
    private LegacyModelSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        int[] count = {0};
        int[] animated = {0};
        int[] staticModels = {0};
        Field partsField = LegacyAnimalModel.class.getDeclaredField("parts");
        partsField.setAccessible(true);
        try (var files = Files.walk(root)) {
            files.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                String key = root.relativize(path).toString().replace('\\', '/');
                key = key.substring(0, key.length() - ".json".length());
                LegacyAnimalModel<?> model = LegacyAnimalModel.load(key);
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, ModelPart> parts = (Map<String, ModelPart>) partsField.get(model);
                    float before = rotationChecksum(parts);
                    model.setupAnim(null, 1.37F, 0.65F, 17.3F, 18F, 11F);
                    boolean intentionallyStatic = key.endsWith("/modelpetbowl")
                            || key.endsWith("/modelhamsterwheel");
                    if (Math.abs(rotationChecksum(parts) - before) > 1.0E-5F) animated[0]++;
                    else if (intentionallyStatic) staticModels[0]++;
                    else System.out.println("No animation response: " + key);
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException(exception);
                }
                count[0]++;
            });
        }
        System.out.println("Baked converted legacy models: " + count[0]);
        System.out.println("Animal models responding to animation inputs: " + animated[0]);
        System.out.println("Intentionally static block models: " + staticModels[0]);
        if (animated[0] + staticModels[0] != count[0]) {
            throw new IllegalStateException((count[0] - animated[0] - staticModels[0])
                    + " animal models did not animate");
        }
    }

    private static float rotationChecksum(Map<String, ModelPart> parts) {
        float checksum = 0;
        int index = 1;
        for (ModelPart part : parts.values()) {
            checksum += index++ * (part.xRot * 3F + part.yRot * 5F + part.zRot * 7F);
        }
        return checksum;
    }
}
