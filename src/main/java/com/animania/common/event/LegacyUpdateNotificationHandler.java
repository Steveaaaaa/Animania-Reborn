package com.animania.common.event;

import com.animania.Animania;
import com.animania.common.config.LegacyConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Shows the installed version and project link at login without an HTTP request.
 */
@EventBusSubscriber(modid = Animania.MOD_ID)
public final class LegacyUpdateNotificationHandler {
    private static final String PROJECT_URL = "https://github.com/Steveaaaaa/Animania-Reborn";

    private LegacyUpdateNotificationHandler() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!LegacyConfig.SHOW_MOD_UPDATE_NOTIFICATION.get() || event.getEntity().level().isClientSide()) return;
        String version = ModList.get().getModContainerById(Animania.MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString()).orElse("unknown");
        Component link = Component.translatable("message.animania.update_link")
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, PROJECT_URL))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("message.animania.update_hover"))));
        event.getEntity().sendSystemMessage(Component.translatable("message.animania.update_notice", version, link));
    }
}
