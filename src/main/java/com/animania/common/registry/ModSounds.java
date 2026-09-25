package com.animania.common.registry;

import com.animania.Animania;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Animania.MOD_ID);

    public static final RegistryObject<SoundEvent> PIG_DEATH = sound("pig_death");
    public static final RegistryObject<SoundEvent> PIGLET_DEATH = sound("piglet_death");
    public static final RegistryObject<SoundEvent> SHEEP_DEATH = sound("sheep_death");
    public static final RegistryObject<SoundEvent> LAMB_DEATH = sound("lamb_death");
    public static final RegistryObject<SoundEvent> GOAT_DEATH = sound("goat_death");
    public static final RegistryObject<SoundEvent> KID_DEATH = sound("kid_death");
    public static final RegistryObject<SoundEvent> HORSE_DEATH = sound("horse_death");
    public static final RegistryObject<SoundEvent> RABBIT_DEATH = sound("rabbit_death");

    public static final RegistryObject<SoundEvent> CHICKEN_AMBIENT = sound("chicken_ambient");
    public static final RegistryObject<SoundEvent> CHICKEN_CROW_1 = sound("chicken_crow_1");
    public static final RegistryObject<SoundEvent> CHICKEN_CROW_2 = sound("chicken_crow_2");
    public static final RegistryObject<SoundEvent> CHICKEN_CROW_3 = sound("chicken_crow_3");
    public static final RegistryObject<SoundEvent> ROOSTER_AMBIENT = sound("rooster_ambient");
    public static final RegistryObject<SoundEvent> CHICKEN_HURT = sound("chicken_hurt");
    public static final RegistryObject<SoundEvent> CHICKEN_DEATH = sound("chicken_death");
    public static final RegistryObject<SoundEvent> COW_AMBIENT = sound("cow_ambient");
    public static final RegistryObject<SoundEvent> BULL_AMBIENT = sound("bull_ambient");
    public static final RegistryObject<SoundEvent> CALF_AMBIENT = sound("calf_ambient");
    public static final RegistryObject<SoundEvent> COW_HURT = sound("cow_hurt");
    public static final RegistryObject<SoundEvent> CALF_HURT = sound("calf_hurt");
    public static final RegistryObject<SoundEvent> COW_DEATH = sound("cow_death");
    public static final RegistryObject<SoundEvent> COW_ANGRY = sound("cow_angry");
    public static final RegistryObject<SoundEvent> COW_EAT = sound("cow_eat");
    public static final RegistryObject<SoundEvent> GOAT_AMBIENT = sound("goat_ambient");
    public static final RegistryObject<SoundEvent> KID_AMBIENT = sound("kid_ambient");
    public static final RegistryObject<SoundEvent> GOAT_HURT = sound("goat_hurt");
    public static final RegistryObject<SoundEvent> KID_HURT = sound("kid_hurt");
    public static final RegistryObject<SoundEvent> HORSE_AMBIENT = sound("horse_ambient");
    public static final RegistryObject<SoundEvent> HORSE_HURT = sound("horse_hurt");
    public static final RegistryObject<SoundEvent> PIG_AMBIENT = sound("pig_ambient");
    public static final RegistryObject<SoundEvent> HOG_AMBIENT = sound("hog_ambient");
    public static final RegistryObject<SoundEvent> PIGLET_AMBIENT = sound("piglet_ambient");
    public static final RegistryObject<SoundEvent> PIG_HURT = sound("pig_hurt");
    public static final RegistryObject<SoundEvent> PIGLET_HURT = sound("piglet_hurt");
    public static final RegistryObject<SoundEvent> SHEEP_AMBIENT = sound("sheep_ambient");
    public static final RegistryObject<SoundEvent> LAMB_AMBIENT = sound("lamb_ambient");
    public static final RegistryObject<SoundEvent> SHEEP_HURT = sound("sheep_hurt");
    public static final RegistryObject<SoundEvent> FROG_AMBIENT = sound("frog_ambient");
    public static final RegistryObject<SoundEvent> TOAD_AMBIENT = sound("toad_ambient");
    public static final RegistryObject<SoundEvent> DART_FROG_AMBIENT = sound("dart_frog_ambient");
    public static final RegistryObject<SoundEvent> HAMSTER_AMBIENT = sound("hamster_ambient");
    public static final RegistryObject<SoundEvent> HAMSTER_HURT = sound("hamster_hurt");
    public static final RegistryObject<SoundEvent> HAMSTER_DEATH = sound("hamster_death");
    public static final RegistryObject<SoundEvent> HAMSTER_EAT = sound("hamster_eat");
    public static final RegistryObject<SoundEvent> FERRET_AMBIENT = sound("ferret_ambient");
    public static final RegistryObject<SoundEvent> FERRET_HURT = sound("ferret_hurt");
    public static final RegistryObject<SoundEvent> HEDGEHOG_AMBIENT = sound("hedgehog_ambient");
    public static final RegistryObject<SoundEvent> HEDGEHOG_HURT = sound("hedgehog_hurt");
    public static final RegistryObject<SoundEvent> RABBIT_AMBIENT = sound("rabbit_ambient");
    public static final RegistryObject<SoundEvent> RABBIT_HURT = sound("rabbit_hurt");
    public static final RegistryObject<SoundEvent> PEAFOWL_AMBIENT = sound("peafowl_ambient");
    public static final RegistryObject<SoundEvent> PEAFOWL_HURT = sound("peafowl_hurt");
    public static final RegistryObject<SoundEvent> AMPHIBIAN_REEEE = sound("amphibian_reeee");
    public static final RegistryObject<SoundEvent> AMPHIBIAN_OOOOHH = sound("amphibian_oooohh");
    public static final RegistryObject<SoundEvent> VEHICLE_HITCH = sound("vehicle_hitch");
    public static final RegistryObject<SoundEvent> VEHICLE_UNHITCH = sound("vehicle_unhitch");
    public static final RegistryObject<SoundEvent> COMBO = sound("combo");
    public static final RegistryObject<SoundEvent> SHEARS = sound("shears");
    public static final RegistryObject<SoundEvent> ZAP = sound("zap");

    public static final RegistryObject<SoundEvent> CAT_AMBIENT = sound("cat_ambient");
    public static final RegistryObject<SoundEvent> CAT_PURR = sound("cat_purr");
    public static final RegistryObject<SoundEvent> CAT_HISS = sound("cat_hiss");
    public static final RegistryObject<SoundEvent> CAT_HURT = sound("cat_hurt");
    public static final RegistryObject<SoundEvent> CAT_DEATH = sound("cat_death");
    public static final RegistryObject<SoundEvent> OCELOT_AMBIENT = sound("ocelot_ambient");
    public static final RegistryObject<SoundEvent> OCELOT_HURT = sound("ocelot_hurt");
    public static final RegistryObject<SoundEvent> OCELOT_DEATH = sound("ocelot_death");
    public static final RegistryObject<SoundEvent> DOG_AMBIENT = sound("dog_ambient");
    public static final RegistryObject<SoundEvent> DOG_GROWL = sound("dog_growl");
    public static final RegistryObject<SoundEvent> DOG_WHINE = sound("dog_whine");
    public static final RegistryObject<SoundEvent> DOG_HURT = sound("dog_hurt");
    public static final RegistryObject<SoundEvent> DOG_DEATH = sound("dog_death");
    public static final RegistryObject<SoundEvent> WOLF_AMBIENT = sound("wolf_ambient");
    public static final RegistryObject<SoundEvent> WOLF_HOWL = sound("wolf_howl");
    public static final RegistryObject<SoundEvent> WOLF_GROWL = sound("wolf_growl");
    public static final RegistryObject<SoundEvent> WOLF_WHINE = sound("wolf_whine");
    public static final RegistryObject<SoundEvent> WOLF_HURT = sound("wolf_hurt");
    public static final RegistryObject<SoundEvent> WOLF_DEATH = sound("wolf_death");
    public static final RegistryObject<SoundEvent> FOX_AMBIENT = sound("fox_ambient");
    public static final RegistryObject<SoundEvent> FOX_HURT = sound("fox_hurt");
    public static final RegistryObject<SoundEvent> FOX_DEATH = sound("fox_death");
    public static final RegistryObject<SoundEvent> FERRET_DEATH = sound("ferret_death");
    public static final RegistryObject<SoundEvent> HEDGEHOG_DEATH = sound("hedgehog_death");
    public static final RegistryObject<SoundEvent> AMPHIBIAN_HURT = sound("amphibian_hurt");
    public static final RegistryObject<SoundEvent> AMPHIBIAN_DEATH = sound("amphibian_death");

    private static RegistryObject<SoundEvent> sound(String name) {
        ResourceLocation id = new ResourceLocation(Animania.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }

    private ModSounds() {
    }
}
