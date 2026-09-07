package com.animania.compat.jei;

import com.animania.Animania;
import com.animania.common.registry.ModBlocks;
import com.animania.compat.CheeseAgingDisplay;
import com.animania.farm.world.block.entity.CheeseMoldBlockEntity;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class AnimaniaJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = id("jei_plugin");
    private static final RecipeType<CheeseAgingDisplay> CHEESE_AGING =
            RecipeType.create(Animania.MOD_ID, "cheese_aging", CheeseAgingDisplay.class);

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CheeseAgingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(CHEESE_AGING, CheeseAgingDisplay.all());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.CHEESE_MOLD.get(), CHEESE_AGING);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Animania.MOD_ID, path);
    }

    private static final class CheeseAgingCategory implements IRecipeCategory<CheeseAgingDisplay> {
        private final IDrawable icon;
        private final IDrawable arrow;

        private CheeseAgingCategory(IGuiHelper guiHelper) {
            icon = guiHelper.createDrawableItemLike(ModBlocks.CHEESE_MOLD.get());
            arrow = guiHelper.createAnimatedRecipeArrow(CheeseMoldBlockEntity.maturityTime());
        }

        @Override
        public RecipeType<CheeseAgingDisplay> getRecipeType() {
            return CHEESE_AGING;
        }

        @Override
        public Component getTitle() {
            return Component.translatable("compat.animania.cheese_aging");
        }

        @Override
        public int getWidth() {
            return 100;
        }

        @Override
        public int getHeight() {
            return 44;
        }

        @Override
        public IDrawable getIcon() {
            return icon;
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, CheeseAgingDisplay recipe, IFocusGroup focuses) {
            builder.addInputSlot(8, 12).setStandardSlotBackground().addItemStack(recipe.input());
            builder.addOutputSlot(74, 12).setOutputSlotBackground().addItemStack(recipe.output());
        }

        @Override
        public void draw(CheeseAgingDisplay recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                         double mouseX, double mouseY) {
            arrow.draw(graphics, 38, 12);
            graphics.drawCenteredString(Minecraft.getInstance().font,
                    Component.translatable("compat.animania.aging_time"), 50, 34, 0xFF808080);
        }
    }
}
