package com.animania.compat.jade;

import com.animania.common.entity.HusbandryMood;
import com.animania.common.registry.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.animal.Animal;
import snownee.jade.api.ITooltip;

final class HusbandryMoodTooltip {
    private HusbandryMoodTooltip() {}
    static void write(CompoundTag data, Animal animal) {
        if (!HusbandryMood.enabled() || HusbandryMood.kind(animal) == null) return;
        data.putBoolean("MoodVisible", true);
        data.putBoolean("Managed", HusbandryMood.managed(animal));
        data.putInt("Mood", animal.getData(ModAttachments.MOOD_SCORE));
        data.putInt("MoodMissing", animal.getData(ModAttachments.MOOD_MISSING));
        data.putInt("MoodGrace", animal.getData(ModAttachments.MOOD_GRACE));
        data.putInt("MoodEffect", HusbandryMood.effect(animal));
        data.putBoolean("MoodBreeding", HusbandryMood.breedingAllowed(animal));
        data.putInt("MilkRest", HusbandryMood.effect(animal) >= 0 ? 0 : animal.getData(ModAttachments.MILK_REST));
    }
    static void append(CompoundTag data, ITooltip tooltip) {
        if (!data.getBoolean("MoodVisible")) return;
        if (!data.getBoolean("Managed")) { tooltip.add(Component.translatable("jade.animania.mood.wild").withStyle(ChatFormatting.GRAY)); return; }
        int mood = data.getInt("Mood"), effect = data.getInt("MoodEffect");
        tooltip.add(Component.translatable("jade.animania.mood.score", mood,
                Component.translatable("jade.animania.mood." + (mood >= 75 ? "content" : mood < 35 ? "unhappy" : "neutral")))
                .withStyle(mood >= 75 ? ChatFormatting.GREEN : mood < 35 ? ChatFormatting.GOLD : ChatFormatting.WHITE));
        if (data.getInt("MoodGrace") > 0) tooltip.add(Component.translatable("jade.animania.mood.grace", (data.getInt("MoodGrace") + 19) / 20));
        else if (effect != 0) tooltip.add(Component.translatable(effect > 0 ? "jade.animania.mood.bonus" : "jade.animania.mood.penalty"));
        if (!data.getBoolean("MoodBreeding")) tooltip.add(Component.translatable("jade.animania.mood.no_breeding").withStyle(ChatFormatting.RED));
        if (data.getInt("MilkRest") > 0) tooltip.add(Component.translatable("jade.animania.mood.milk_rest", (data.getInt("MilkRest") + 19) / 20));
        int missing = data.getInt("MoodMissing");
        for (int bit = 0; bit <= 19; bit++) if ((missing & (1 << bit)) != 0)
            tooltip.add(Component.translatable("jade.animania.mood.need." + bit).withStyle(ChatFormatting.GRAY));
    }
}
