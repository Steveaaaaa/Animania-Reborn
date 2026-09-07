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
