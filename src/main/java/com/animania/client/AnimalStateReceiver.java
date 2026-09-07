package com.animania.client;
import com.animania.common.registry.ModAttachments;
import net.minecraft.client.Minecraft;
public final class AnimalStateReceiver {
    public static void receive(ModAttachments.Update update) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var entity = level.getEntity(update.entityId());
        if (entity != null && update.values() != null) ModAttachments.apply(entity, update.values());
    }
}
