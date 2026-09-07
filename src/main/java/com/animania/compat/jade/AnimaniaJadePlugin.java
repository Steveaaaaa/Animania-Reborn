package com.animania.compat.jade;

import com.animania.Animania;
import com.animania.catsdogs.block.PetBowlBlock;
import com.animania.catsdogs.block.entity.PetBowlBlockEntity;
import com.animania.catsdogs.cat.AnimaniaCat;
import com.animania.catsdogs.cat.CatRole;
import com.animania.catsdogs.dog.AnimaniaDog;
import com.animania.catsdogs.dog.DogRole;
import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.AnimalInformation;
import com.animania.common.registry.ModAttachments;
import com.animania.common.world.block.SaltLickBlock;
import com.animania.common.world.block.TroughBlock;
import com.animania.common.world.block.entity.SaltLickBlockEntity;
import com.animania.common.world.block.entity.TroughBlockEntity;
import com.animania.extra.peafowl.AnimaniaPeafowl;
import com.animania.extra.peafowl.PeafowlRole;
import com.animania.extra.rabbit.AnimaniaRabbit;
import com.animania.extra.rabbit.RabbitRole;
import com.animania.extra.world.block.HamsterWheelBlock;
import com.animania.extra.world.block.entity.HamsterWheelBlockEntity;
import com.animania.farm.chicken.AnimaniaChicken;
import com.animania.farm.chicken.ChickenRole;
import com.animania.farm.livestock.AnimaniaCow;
import com.animania.farm.livestock.AnimaniaGoat;
import com.animania.farm.livestock.AnimaniaHorse;
import com.animania.farm.livestock.AnimaniaPig;
import com.animania.farm.livestock.AnimaniaSheep;
import com.animania.farm.livestock.FarmAnimalRole;
import com.animania.farm.livestock.GoatBreed;
import com.animania.farm.world.block.CheeseMoldBlock;
import com.animania.farm.world.block.CheeseWheelBlock;
import com.animania.farm.world.block.HiveBlock;
import com.animania.farm.world.block.NestBlock;
import com.animania.farm.world.block.entity.CheeseMoldBlockEntity;
import com.animania.farm.world.block.entity.HiveBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

@WailaPlugin("jade")
public final class AnimaniaJadePlugin implements IWailaPlugin {
    private static final ResourceLocation NEEDS = id("animal_needs");
    private static final ResourceLocation TROUGH = id("trough");
    private static final ResourceLocation PET_BOWL = id("pet_bowl");
    private static final ResourceLocation CHEESE_MOLD = id("cheese_mold");
    private static final ResourceLocation HAMSTER_WHEEL = id("hamster_wheel");
    private static final ResourceLocation NEST = id("nest");
    private static final ResourceLocation HIVE = id("hive");
    private static final ResourceLocation CHEESE_WHEEL = id("cheese_wheel");
    private static final ResourceLocation SALT_LICK = id("salt_lick");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(AnimalProvider.INSTANCE, Animal.class);
        registration.registerBlockDataProvider(PetBowlProvider.INSTANCE, PetBowlBlockEntity.class);
        registration.registerBlockDataProvider(CheeseMoldProvider.INSTANCE, CheeseMoldBlockEntity.class);
        registration.registerBlockDataProvider(HamsterWheelProvider.INSTANCE, HamsterWheelBlockEntity.class);
        registration.registerBlockDataProvider(HiveProvider.INSTANCE, HiveBlockEntity.class);
        registration.registerBlockDataProvider(SaltLickProvider.INSTANCE, SaltLickBlockEntity.class);
        registration.registerBlockDataProvider(TroughProvider.INSTANCE, TroughBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(AnimalProvider.INSTANCE, Animal.class);
        registration.registerBlockComponent(TroughProvider.INSTANCE, TroughBlock.class);
        registration.registerBlockComponent(PetBowlProvider.INSTANCE, PetBowlBlock.class);
        registration.registerBlockComponent(CheeseMoldProvider.INSTANCE, CheeseMoldBlock.class);
        registration.registerBlockComponent(HamsterWheelProvider.INSTANCE, HamsterWheelBlock.class);
        registration.registerBlockComponent(NestProvider.INSTANCE, NestBlock.class);
        registration.registerBlockComponent(HiveProvider.INSTANCE, HiveBlock.class);
        registration.registerBlockComponent(CheeseWheelProvider.INSTANCE, CheeseWheelBlock.class);
        registration.registerBlockComponent(SaltLickProvider.INSTANCE, SaltLickBlock.class);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Animania.MOD_ID, path);
    }

    private enum AnimalProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, EntityAccessor accessor) {
            if (!(accessor.getEntity() instanceof Animal animal) || !AnimalInformation.isAnimaniaAnimal(animal)) return;

            data.putBoolean("AnimaniaAnimal", true);
            data.putBoolean("Fed", animal.getData(ModAttachments.FED));
            data.putBoolean("Watered", animal.getData(ModAttachments.WATERED));
            data.putBoolean("Sleeping", animal.getData(ModAttachments.SLEEPING));
            data.putString("Gender", AnimalInformation.gender(animal).name().toLowerCase());
            data.putBoolean("Sterilized", AnimalInformation.isSterilized(animal));

            CompoundTag saved = animal.saveWithoutId(new CompoundTag());
            boolean pregnancy = supportsPregnancy(animal);
            data.putBoolean("SupportsPregnancy", pregnancy);
            if (pregnancy) {
                boolean pregnant = saved.getBoolean("Pregnant");
                data.putBoolean("Pregnant", pregnant);
                data.putInt("Gestation", Math.max(0, saved.getInt("Gestation")));
                data.putBoolean("Fertile", com.animania.common.config.LegacyBreedingRules.isReady(animal) && !pregnant);
            } else if (AnimalInformation.gender(animal) != AnimalInformation.Gender.YOUNG) {
                data.putBoolean("Fertile", com.animania.common.config.LegacyBreedingRules.isReady(animal));
            }

            if (supportsMilk(animal)) data.putBoolean("Milkable", saved.getBoolean("HasKids"));
            if (animal instanceof AnimaniaChicken chicken && chicken.role() == ChickenRole.HEN) {
                data.putBoolean("EggLayer", true);
                data.putInt("EggTimer", Math.max(0, saved.getInt("EggLayTime")));
                data.putBoolean("LookingForNest", saved.getBoolean("LookingForNest"));
            } else if (animal instanceof AnimaniaPeafowl peafowl && peafowl.role() == PeafowlRole.PEAHEN) {
                data.putBoolean("EggLayer", true);
                data.putInt("EggTimer", Math.max(0, saved.getInt("LaidTimer")));
                data.putBoolean("LookingForNest", saved.getBoolean("LookingForNest"));
            }

            if (animal instanceof AnimaniaSheep sheep && sheep.role() != FarmAnimalRole.YOUNG) {
                data.putBoolean("WoolAnimal", true);
                data.putBoolean("Sheared", sheep.isSheared());
                data.putInt("WoolRegrowth", Math.max(0, saved.getInt("WoolRegrowth")));
            } else if (animal instanceof AnimaniaGoat goat && goat.breed() == GoatBreed.ANGORA
                    && goat.role() != FarmAnimalRole.YOUNG) {
                data.putBoolean("WoolAnimal", true);
                data.putBoolean("Sheared", goat.isAngoraSheared());
                data.putInt("WoolRegrowth", Math.max(0, saved.getInt("WoolRegrowth")));
            }

            if (saved.contains("Played")) {
                data.putBoolean("HasPlayStatus", true);
                data.putBoolean("Played", saved.getBoolean("Played"));
            }

            if (animal instanceof TamableAnimal tame) {
                data.putBoolean("Tamable", true);
                data.putBoolean("Tamed", tame.isTame());
                data.putBoolean("Sitting", tame.isInSittingPose());
                if (tame.isTame() && tame.getOwnerUUID() != null) {
                    Entity owner = animal.level() instanceof ServerLevel server
                            ? server.getEntity(tame.getOwnerUUID()) : null;
                    if (owner != null) data.putString("Owner", owner.getName().getString());
                    else data.putBoolean("OwnerMissing", true);
                }
            }

            addRelationship(data, animal, "Mate", animal.getData(ModAttachments.LAST_MATE), false);
            addRelationship(data, animal, "Parent", animal.getData(ModAttachments.PARENT), true);
        }

        private static void addRelationship(CompoundTag data, Animal animal, String key,
                                            String rawUuid, boolean reportMissing) {
            if (rawUuid.isEmpty() || !(animal.level() instanceof ServerLevel server)) return;
            try {
                Entity related = server.getEntity(UUID.fromString(rawUuid));
                if (related != null && related.distanceToSqr(animal) <= 400.0D) {
                    data.putBoolean(key + "Known", true);
                    if (related.hasCustomName()) data.putString(key + "Name", related.getName().getString());
                } else if (reportMissing) {
                    data.putBoolean(key + "Missing", true);
                }
            } catch (IllegalArgumentException ignored) {
                if (reportMissing) data.putBoolean(key + "Missing", true);
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (!data.getBoolean("AnimaniaAnimal")) return;

            boolean fed = data.getBoolean("Fed");
            boolean watered = data.getBoolean("Watered");
            if (!LegacyConfig.AMBIANCE_MODE.get()) {
                tooltip.add(Component.translatable(fed && watered ? "jade.animania.fed"
                        : fed ? "jade.animania.thirsty"
                        : watered ? "jade.animania.hungry" : "jade.animania.hungry_thirsty"));
            }
            if (data.getBoolean("Sleeping")) tooltip.add(Component.translatable("jade.animania.sleeping"));
            if (!accessor.getPlayer().isShiftKeyDown()) {
                tooltip.add(Component.translatable("jade.animania.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
                return;
            }

            String gender = data.getString("Gender");
            if (!gender.equals("none")) {
                ChatFormatting color = gender.equals("male") ? ChatFormatting.AQUA
                        : gender.equals("female") ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY;
                tooltip.add(Component.translatable("jade.animania.gender." + gender).withStyle(color));
            }
            if (data.getBoolean("Sterilized")) tooltip.add(Component.translatable("jade.animania.sterilized"));
            if (data.getBoolean("MateKnown")) {
                tooltip.add(data.contains("MateName")
                        ? Component.translatable("jade.animania.mated_named", data.getString("MateName"))
                        : Component.translatable("jade.animania.mated"));
            }
            if (data.getBoolean("ParentKnown")) {
                tooltip.add(data.contains("ParentName")
                        ? Component.translatable("jade.animania.parent_named", data.getString("ParentName"))
                        : Component.translatable("jade.animania.parent"));
            } else if (data.getBoolean("ParentMissing")) {
                tooltip.add(Component.translatable("jade.animania.parent_missing"));
            }
            if (data.getBoolean("Fertile")) tooltip.add(Component.translatable("jade.animania.fertile"));
            if (data.getBoolean("SupportsPregnancy") && data.getBoolean("Pregnant")) {
                tooltip.add(Component.translatable("jade.animania.pregnant", data.getInt("Gestation")));
            }
            if (data.getBoolean("Milkable")) tooltip.add(Component.translatable("jade.animania.milkable"));
            if (data.getBoolean("EggLayer")) {
                tooltip.add(data.getBoolean("LookingForNest")
                        ? Component.translatable("jade.animania.looking_for_nest")
                        : Component.translatable("jade.animania.egg_timer", data.getInt("EggTimer")));
            }
            if (data.getBoolean("WoolAnimal")) {
                int timer = data.getInt("WoolRegrowth");
                tooltip.add(data.getBoolean("Sheared")
                        ? timer > 0 ? Component.translatable("jade.animania.wool_timer", timer)
                        : Component.translatable("jade.animania.sheared")
                        : Component.translatable("jade.animania.wool_ready"));
            }
            if (data.getBoolean("HasPlayStatus")) {
                tooltip.add(Component.translatable(data.getBoolean("Played")
                        ? "jade.animania.content" : "jade.animania.bored"));
            }
            if (data.getBoolean("Tamable")) {
                if (data.getBoolean("Sitting")) tooltip.add(Component.translatable("jade.animania.sitting"));
                if (data.getBoolean("Tamed")) {
                    tooltip.add(data.contains("Owner")
                            ? Component.translatable("jade.animania.tamed_named", data.getString("Owner"))
                            : Component.translatable(data.getBoolean("OwnerMissing")
                            ? "jade.animania.owner_missing" : "jade.animania.tamed"));
                }
            }
        }

        private static boolean supportsPregnancy(Animal animal) {
            return animal instanceof AnimaniaCow cow && cow.role() == FarmAnimalRole.FEMALE
                    || animal instanceof AnimaniaGoat goat && goat.role() == FarmAnimalRole.FEMALE
                    || animal instanceof AnimaniaPig pig && pig.role() == FarmAnimalRole.FEMALE
                    || animal instanceof AnimaniaSheep sheep && sheep.role() == FarmAnimalRole.FEMALE
                    || animal instanceof AnimaniaHorse horse && horse.role() == FarmAnimalRole.FEMALE
                    || animal instanceof AnimaniaRabbit rabbit && rabbit.role() == RabbitRole.DOE
                    || animal instanceof AnimaniaCat cat && cat.role() == CatRole.QUEEN
                    || animal instanceof AnimaniaDog dog && dog.role() == DogRole.FEMALE;
        }

        private static boolean supportsMilk(Animal animal) {
            return animal instanceof AnimaniaCow cow && cow.role() == FarmAnimalRole.FEMALE
                    || animal instanceof AnimaniaGoat goat && goat.role() == FarmAnimalRole.FEMALE
                    || animal instanceof AnimaniaSheep sheep && sheep.role() == FarmAnimalRole.FEMALE;
        }

        @Override public ResourceLocation getUid() { return NEEDS; }
    }

    private enum TroughProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof TroughBlockEntity trough) {
                data.putInt("Water", trough.water());
                data.putInt("Slop", trough.slop());
                if (!trough.feed().isEmpty()) {
                    data.putString("FeedKey", trough.feed().getDescriptionId());
                    data.putInt("FeedCount", trough.feed().getCount());
                }
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (data.contains("FeedKey")) {
                tooltip.add(Component.translatable("jade.animania.food_stack",
                        data.getInt("FeedCount"), Component.translatable(data.getString("FeedKey"))));
            } else if (data.getInt("Slop") > 0) {
                tooltip.add(Component.translatable("jade.animania.slop_amount", data.getInt("Slop"), 1000));
            } else if (data.getInt("Water") > 0) {
                tooltip.add(Component.translatable("jade.animania.water_amount", data.getInt("Water"), 1000));
            } else {
                tooltip.add(Component.translatable("jade.animania.empty"));
            }
        }

        @Override public ResourceLocation getUid() { return TROUGH; }
    }

    private enum PetBowlProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof PetBowlBlockEntity bowl) {
                data.putInt("Water", bowl.water());
                if (!bowl.food().isEmpty()) {
                    data.putString("FoodKey", bowl.food().getDescriptionId());
                    data.putInt("FoodCount", bowl.food().getCount());
                }
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (data.contains("FoodKey")) {
                tooltip.add(Component.translatable("jade.animania.food_stack",
                        data.getInt("FoodCount"), Component.translatable(data.getString("FoodKey"))));
            } else if (data.getInt("Water") > 0) {
                tooltip.add(Component.translatable("jade.animania.water_amount", data.getInt("Water"), 1000));
            } else {
                tooltip.add(Component.translatable("jade.animania.empty"));
            }
        }

        @Override public ResourceLocation getUid() { return PET_BOWL; }
    }

    private enum CheeseMoldProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof CheeseMoldBlockEntity mold
                    && (mold.milk() != null || mold.containsWater())) {
                data.putInt("Progress", mold.progress());
                data.putBoolean("Ready", mold.isReady());
                data.putBoolean("Water", mold.containsWater());
                if (mold.milk() != null) data.putString("Milk", mold.milk().getSerializedName());
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (!data.contains("Progress")) {
                tooltip.add(Component.translatable("jade.animania.empty"));
                return;
            }
            Component content = data.getBoolean("Water") ? Component.translatable("jade.animania.water")
                    : Component.translatable("fluid.animania.milk_" + data.getString("Milk"));
            tooltip.add(Component.translatable("jade.animania.mold_content", content, 1000));
            if (data.getBoolean("Ready")) tooltip.add(Component.translatable("jade.animania.cheese_ready"));
            else {
                int percent = Math.min(100, data.getInt("Progress") * 100 / CheeseMoldBlockEntity.maturityTime());
                tooltip.add(Component.translatable("message.animania.cheese_progress", percent));
            }
        }

        @Override public ResourceLocation getUid() { return CHEESE_MOLD; }
    }

    private enum HamsterWheelProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof HamsterWheelBlockEntity wheel) {
                data.putInt("Energy", wheel.energy().getEnergyStored());
                data.putInt("Capacity", wheel.capacity());
                data.putInt("Food", wheel.food());
                data.putBoolean("Hamster", wheel.hasHamster());
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (data.contains("Energy")) {
                tooltip.add(Component.translatable("jade.animania.hamster_wheel",
                        data.getBoolean("Hamster") ? Component.translatable("jade.animania.running")
                                : Component.translatable("jade.animania.empty"),
                        data.getInt("Food"), data.getInt("Energy"), data.getInt("Capacity")));
            }
        }

        @Override public ResourceLocation getUid() { return HAMSTER_WHEEL; }
    }

    private enum NestProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            int eggs = accessor.getBlockState().getValue(NestBlock.EGGS);
            if (eggs == 0) tooltip.add(Component.translatable("jade.animania.nest_empty"));
            else tooltip.add(Component.translatable("jade.animania.nest_contents", eggs,
                    Component.translatable("jade.animania.nest_breed."
                            + accessor.getBlockState().getValue(NestBlock.BREED).getSerializedName())));
        }

        @Override public ResourceLocation getUid() { return NEST; }
    }

    private enum HiveProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof HiveBlockEntity hive) {
                data.putInt("Honey", hive.honeyAmount());
                data.putInt("NextHoney", hive.nextHoney());
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (data.contains("Honey")) tooltip.add(Component.translatable("jade.animania.hive",
                    data.getInt("Honey"), HiveBlockEntity.CAPACITY, data.getInt("NextHoney")));
        }

        @Override public ResourceLocation getUid() { return HIVE; }
    }

    private enum CheeseWheelProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            tooltip.add(Component.translatable("jade.animania.cheese_portions",
                    4 - accessor.getBlockState().getValue(CheeseWheelBlock.BITES), 4));
        }

        @Override public ResourceLocation getUid() { return CHEESE_WHEEL; }
    }

    private enum SaltLickProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof SaltLickBlockEntity lick) data.putInt("Uses", lick.usesLeft());
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            if (data.contains("Uses")) tooltip.add(Component.translatable("jade.animania.salt_lick",
                    data.getInt("Uses"), SaltLickBlockEntity.maxUses()));
        }

        @Override public ResourceLocation getUid() { return SALT_LICK; }
    }
}
