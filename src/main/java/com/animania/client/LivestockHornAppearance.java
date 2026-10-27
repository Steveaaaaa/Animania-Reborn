package com.animania.client;

import com.animania.farm.livestock.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import java.util.Map;

/** Apply after legacy motion so shared models cannot leak horn visibility between animals. */
final class LivestockHornAppearance {
    private LivestockHornAppearance() {}

    static void apply(Entity entity, Map<String, ModelPart> parts) {
        boolean visible;
        float size = 1;
        if (entity instanceof AnimaniaCow cow) {
            visible = cow.breed().hasAdultHorns() && cow.role() != FarmAnimalRole.YOUNG && !cow.isBaby();
        } else if (entity instanceof AnimaniaSheep sheep) {
            visible = sheep.breed().hasAdultHorns(sheep.role()) && !sheep.isBaby();
            if (sheep.role() == FarmAnimalRole.FEMALE) {
                size = switch (sheep.breed()) {
                    case DORSET -> .65F;
                    case LONG_NOSED -> .8F;
                    default -> 1;
                };
            }
        } else return;

        for (var entry : parts.entrySet()) {
            if (!entry.getKey().toLowerCase(java.util.Locale.ROOT).contains("horn")) continue;
            ModelPart horn = entry.getValue();
            horn.visible = visible;
            horn.xScale = horn.yScale = horn.zScale = size;
            if (size == 1) continue;
            // The Dorset and Merino-derived Valais horns are sibling segments on HeadNode.
            // Scale around each horn's skull attachment, not each segment's own pivot.
            float anchorX = entry.getKey().startsWith("Left") ? 1 : -1;
            horn.x = anchorX + (horn.x - anchorX) * size;
            horn.y = -6.155134F + (horn.y + 6.155134F) * size;
            horn.z = -10.29287F + (horn.z + 10.29287F) * size;
        }
    }
}
