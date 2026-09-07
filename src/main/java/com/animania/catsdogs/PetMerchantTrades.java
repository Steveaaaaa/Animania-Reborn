package com.animania.catsdogs;

import com.animania.Animania;
import com.animania.catsdogs.cat.CatBreed;
import com.animania.catsdogs.cat.CatRole;
import com.animania.catsdogs.dog.DogBreed;
import com.animania.catsdogs.dog.DogRole;
import com.animania.common.registry.ModItems;
import com.animania.common.registry.ModVillagers;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.village.VillagerTradesEvent;

@EventBusSubscriber(modid = Animania.MOD_ID)
public final class PetMerchantTrades {
    private PetMerchantTrades() {
    }

    @SubscribeEvent
    public static void addTrades(VillagerTradesEvent event) {
        if (event.getType() != ModVillagers.PET_SELLER.get()) return;

        addCat(event, 1, CatBreed.AMERICAN_SHORTHAIR, 10, 20);
        addCat(event, 2, CatBreed.RAGDOLL, 10, 20);
        addCat(event, 3, CatBreed.NORWEGIAN, 10, 20);
        addCat(event, 2, CatBreed.ASIATIC, 15, 25);
        addCat(event, 3, CatBreed.EXOTIC, 15, 25);
        addCat(event, 2, CatBreed.TABBY, 15, 25);
        addCat(event, 3, CatBreed.SIAMESE, 25, 35);

        addDog(event, 1, DogBreed.BLOOD_HOUND, 15, 30);
        addDog(event, 2, DogBreed.CHIHUAHUA, 20, 30);
        addDog(event, 3, DogBreed.COLLIE, 20, 30);
        addDog(event, 1, DogBreed.CORGI, 15, 25);
        addDog(event, 2, DogBreed.DACHSHUND, 10, 20);
        addDog(event, 3, DogBreed.GERMAN_SHEPHERD, 20, 30);
        addDog(event, 1, DogBreed.GREAT_DANE, 20, 30);
        addDog(event, 2, DogBreed.GREYHOUND, 20, 30);
        addDog(event, 3, DogBreed.HUSKY, 20, 25);
        addDog(event, 1, DogBreed.LABRADOR, 20, 30);
        addDog(event, 2, DogBreed.POMERANIAN, 15, 25);
        addDog(event, 3, DogBreed.POODLE, 15, 25);
        addDog(event, 1, DogBreed.PUG, 15, 25);
    }

    private static void addCat(VillagerTradesEvent event, int level, CatBreed breed, int min, int max) {
        event.getTrades().get(level).add(new PetForEmeralds(ModItems.catSpawnEgg(CatRole.TOM, breed).get(), min, max, level));
        event.getTrades().get(level).add(new PetForEmeralds(ModItems.catSpawnEgg(CatRole.QUEEN, breed).get(), min, max, level));
        event.getTrades().get(level).add(new PetForEmeralds(ModItems.catSpawnEgg(CatRole.KITTEN, breed).get(),
                min + min / 2, max + max / 2, level));
    }

    private static void addDog(VillagerTradesEvent event, int level, DogBreed breed, int min, int max) {
        event.getTrades().get(level).add(new PetForEmeralds(ModItems.dogSpawnEgg(DogRole.MALE, breed).get(), min, max, level));
        event.getTrades().get(level).add(new PetForEmeralds(ModItems.dogSpawnEgg(DogRole.FEMALE, breed).get(), min, max, level));
        event.getTrades().get(level).add(new PetForEmeralds(ModItems.dogSpawnEgg(DogRole.PUPPY, breed).get(),
                min + min / 2, max + max / 2, level));
    }

    private record PetForEmeralds(Item petEgg, int minimum, int maximum, int level)
            implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            int price = Mth.nextInt(random, minimum, maximum);
            int xp = level == 1 ? 2 : level == 2 ? 10 : 20;
            return new MerchantOffer(new ItemStack(Items.EMERALD, price), new ItemStack(petEgg), 4, xp, 0.2F);
        }
    }
}
