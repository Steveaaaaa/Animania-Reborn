package com.animania.compat.emi;

import com.animania.Animania;
import com.animania.common.registry.ModBlocks;
import com.animania.compat.CheeseAgingDisplay;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@EmiEntrypoint
public final class AnimaniaEmiPlugin implements EmiPlugin {
    private static final EmiRecipeCategory CHEESE_AGING = new EmiRecipeCategory(
            id("cheese_aging"), EmiStack.of(ModBlocks.CHEESE_MOLD.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CHEESE_AGING);
        registry.addWorkstation(CHEESE_AGING, EmiStack.of(ModBlocks.CHEESE_MOLD.get()));
        CheeseAgingDisplay.all().forEach(recipe -> registry.addRecipe(new CheeseAgingRecipe(recipe)));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Animania.MOD_ID, path);
    }

    private static final class CheeseAgingRecipe extends BasicEmiRecipe {
        private CheeseAgingRecipe(CheeseAgingDisplay recipe) {
            super(CHEESE_AGING, id("cheese_aging/" + recipe.milk().getSerializedName()), 92, 42);
            inputs.add(EmiStack.of(recipe.input()));
            catalysts.add(EmiStack.of(ModBlocks.CHEESE_MOLD.get()));
            outputs.add(EmiStack.of(recipe.output()));
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addSlot(inputs.get(0), 2, 2);
            widgets.addFillingArrow(29, 3, 5_000);
            widgets.addSlot(outputs.get(0), 64, 2).recipeContext(this);
            widgets.addText(Component.translatable("compat.animania.aging_time"), 18, 28, 0xFF808080, false);
        }
    }
}
