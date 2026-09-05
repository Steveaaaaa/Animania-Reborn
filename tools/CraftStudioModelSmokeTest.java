package com.animania.client;

import java.nio.file.Files;
import java.nio.file.Path;

/** Verifies every converted CraftStudio model and animation resource. */
public final class CraftStudioModelSmokeTest {
    private CraftStudioModelSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path modelRoot = Path.of(args[0]);
        Path animationRoot = Path.of(args[1]);
        int[] models = {0};
        int[] modelNodes = {0};
        try (var files = Files.walk(modelRoot)) {
            files.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                String key = key(modelRoot, path);
                modelNodes[0] += CraftStudioModel.load(key).nodeCount();
                models[0]++;
            });
        }
        int[] animations = {0};
        int[] animationNodes = {0};
        try (var files = Files.walk(animationRoot)) {
            files.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                String key = key(animationRoot, path);
                CraftStudioAnimation animation = CraftStudioAnimation.load(key);
                animationNodes[0] += animation.animatedNodeCount();
                if (animation.duration() <= 0) throw new IllegalStateException("Empty animation " + key);
                animations[0]++;
            });
        }
        System.out.println("Loaded CraftStudio models: " + models[0] + " (" + modelNodes[0] + " nodes)");
        System.out.println("Loaded CraftStudio animations: " + animations[0]
                + " (" + animationNodes[0] + " animated nodes)");
        if (models[0] != 18 || modelNodes[0] != 667 || animations[0] != 8 || animationNodes[0] != 26) {
            throw new IllegalStateException("CraftStudio inventory is incomplete");
        }
    }

    private static String key(Path root, Path path) {
        String key = root.relativize(path).toString().replace('\\', '/');
        return key.substring(0, key.length() - ".json".length());
    }
}
