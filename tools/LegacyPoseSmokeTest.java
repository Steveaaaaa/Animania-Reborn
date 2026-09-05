package com.animania.client;

import java.nio.file.Files;
import java.nio.file.Path;

/** Loads every mechanically extracted state pose and checks the source inventory. */
public final class LegacyPoseSmokeTest {
    private LegacyPoseSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        int files = 0;
        int parts = 0;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json")).toList()) {
                String key = root.relativize(path.getParent()).toString().replace('\\', '/');
                LegacyPose pose = LegacyPose.load(key, "sitting");
                files++;
                parts += pose.partCount();
            }
        }
        System.out.println("Loaded legacy sitting poses: " + files + " (" + parts + " parts)");
        if (files != 22 || parts != 171) {
            throw new IllegalStateException("Expected 22 poses/171 parts, found " + files + "/" + parts);
        }
    }
}
