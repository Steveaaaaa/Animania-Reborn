package com.animania.client;

import com.animania.Animania;
import com.animania.common.registry.ModEntities;
import com.animania.common.registry.ModFluids;
import com.animania.common.registry.ModBlockEntities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import com.animania.common.item.AnimaniaSpawnEggItem;

@EventBusSubscriber(modid = Animania.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.HAMSTER_WHEEL.get(), HamsterWheelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PET_PROP.get(), PetPropRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PET_BOWL.get(), PetBowlRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TROUGH.get(), TroughRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HIVE.get(), HiveRenderer::new);
        ModEntities.ALL_CHICKENS.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaChickenRenderer::new));
        ModEntities.ALL_COWS.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaCowRenderer::new));
        ModEntities.ALL_GOATS.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaGoatRenderer::new));
        ModEntities.ALL_PIGS.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaPigRenderer::new));
        ModEntities.ALL_SHEEP.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaSheepRenderer::new));
        ModEntities.ALL_HORSES.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaHorseRenderer::new));
        event.registerEntityRenderer(ModEntities.CART.get(), FarmVehicleRenderer::new);
        event.registerEntityRenderer(ModEntities.WAGON.get(), FarmVehicleRenderer::new);
        event.registerEntityRenderer(ModEntities.TILLER.get(), FarmVehicleRenderer::new);
        ModEntities.ALL_AMPHIBIANS.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaAmphibianRenderer::new));
        ModEntities.ALL_RODENTS.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaRodentRenderer::new));
        ModEntities.ALL_RABBITS.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaRabbitRenderer::new));
        ModEntities.ALL_PEAFOWL.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaPeafowlRenderer::new));
        ModEntities.ALL_CATS.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaCatRenderer::new));
        ModEntities.ALL_DOGS.values().forEach(type ->
                event.registerEntityRenderer(type.get(), AnimaniaDogRenderer::new));
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HamsterBallLayer.LAYER, HamsterBallLayer::createLayer);
    }

    @SubscribeEvent
    public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
        for (com.animania.farm.dairy.MilkType milkType : com.animania.farm.dairy.MilkType.values()) {
            String id = milkType.getSerializedName();
            event.registerFluidType(fluidRendering("fluid/milk_" + id + "_still",
                    "fluid/milk_" + id + "_flow", 0xFFFFFFFF), ModFluids.milk(milkType).type());
        }
        event.registerFluidType(fluidRendering("fluid/animania_honey_still", "fluid/animania_honey_flow", 0xFFFFFFFF),
                ModFluids.HONEY.type());
        event.registerFluidType(fluidRendering("fluid/slop_still", "fluid/slop_flow", 0xFFFFFFFF),
                ModFluids.SLOP.type());
    }

    @SubscribeEvent
    public static void wrapSpawnEggModels(ModelEvent.ModifyBakingResult event) {
        BuiltInRegistries.ITEM.forEach(item -> {
            if (!(item instanceof AnimaniaSpawnEggItem)) return;
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            ModelResourceLocation modelId = ModelResourceLocation.inventory(id);
            var model = event.getModels().get(modelId);
            if (model != null) event.getModels().put(modelId, new ConfigurableSpawnEggModel(model));
        });
    }

    private static IClientFluidTypeExtensions fluidRendering(String still, String flowing, int tint) {
        return new IClientFluidTypeExtensions() {
            @Override public ResourceLocation getStillTexture() {
                return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID, still);
            }
            @Override public ResourceLocation getFlowingTexture() {
                return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID, flowing);
            }
            @Override public int getTintColor() { return tint; }
        };
    }
}
