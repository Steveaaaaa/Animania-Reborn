package com.animania.modern;

import com.animania.common.config.LegacyBreedingRules;
import com.animania.common.config.LegacyConfig;
import com.animania.common.entity.AnimalInformation;
import com.animania.common.entity.LegacyAnimalNeeds;
import com.animania.common.entity.LegacyReproduction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

/** Pregnancy state for animals which keep their vanilla entity and animation hierarchy. */
public final class ModernCare {
    private final Animal animal;
    private int gestation;
    private CompoundTag father = new CompoundTag();
    private int milk;
    private java.util.UUID feeder;

    public void fedBy(net.minecraft.world.entity.player.Player player) { feeder = player.getUUID(); }
    public java.util.UUID feeder() { return feeder; }

    public ModernCare(Animal animal) { this.animal = animal; }
    public boolean pregnant() { return gestation > 0; }
    public int gestation() { return gestation; }
    public boolean hasMilk() { return milk > 0; }
    public void milked() { milk = 0; }

    public boolean canMate(Animal other) {
        if (other == animal || other.getClass() != animal.getClass()
                || !(other instanceof ModernAnimal partner) || animal.isBaby() || other.isBaby()) return false;
        ModernAnimal self = (ModernAnimal) animal;
        return self.isFemale() != partner.isFemale() && !pregnant() && !partner.care().pregnant()
                && LegacyBreedingRules.canMate(animal, other);
    }

    public void conceive(ServerLevel level, Animal mate) {
        if (!canMate(mate)) return;
        Animal mother = ((ModernAnimal) animal).isFemale() ? animal : mate;
        Animal sire = mother == animal ? mate : animal;
        ModernCare state = ((ModernAnimal) mother).care();
        state.gestation = Math.max(1, LegacyConfig.GESTATION_TIMER.get() + mother.getRandom().nextInt(200));
        state.father = new CompoundTag();
        sire.addAdditionalSaveData(state.father);
        state.father.putString("id", net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(sire.getType()).toString());
        state.father.remove("AnimaniaFather");
        AnimalInformation.recordMating(mother, sire);
        LegacyReproduction.conceived(mother);
        mother.setAge(6000);
        sire.setAge(6000);
        mother.resetLove();
        sire.resetLove();
        level.broadcastEntityEvent(mother, (byte) 18);
    }

    public void tick() {
        if (!(animal.level() instanceof ServerLevel level) || !pregnant()) return;
        if (animal.tickCount % 200 == 0 && LegacyBreedingRules.shouldLosePregnancy(animal, animal.getRandom())) {
            gestation = 0;
            father = new CompoundTag();
            LegacyReproduction.completedPregnancy(animal);
            return;
        }
        if (--gestation > 0) return;
        var sireId = net.minecraft.resources.ResourceLocation.tryParse(father.getString("id"));
        var fallback = animal instanceof ModernFox ? com.animania.common.registry.ModEntities.MODERN_FOX.get()
                : com.animania.common.registry.ModEntities.MOUNTAIN_GOAT.get();
        var sireType = sireId == null ? fallback : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getOptional(sireId).orElse(fallback);
        var candidate = sireType.create(level);
        Animal sire = candidate instanceof Animal parent && parent.getClass() == animal.getClass() ? parent : null;
        if (sire == null) return;
        sire.readAdditionalSaveData(father);
        int count = LegacyBreedingRules.litterSize(animal.getRandom());
        for (int i = 0; i < count; i++) {
            var offspring = animal.getBreedOffspring(level, sire);
            if (!(offspring instanceof Animal child)) continue;
            child.setAge(-24000);
            child.moveTo(animal.getX(), animal.getY(), animal.getZ(), animal.getYRot(), 0.0F);
            AnimalInformation.recordParent(child, animal);
            LegacyAnimalNeeds.setInteracted(child, LegacyAnimalNeeds.isInteracted(animal));
            level.addFreshEntity(child);
        }
        milk = animal instanceof MountainGoat ? 1 : 0;
        father = new CompoundTag();
        LegacyReproduction.completedPregnancy(animal);
        level.broadcastEntityEvent(animal, (byte) 18);
    }

    public void save(CompoundTag tag) {
        tag.putInt("AnimaniaGestation", gestation);
        tag.putBoolean("Pregnant", pregnant());
        tag.putInt("Gestation", gestation);
        tag.putBoolean("HasKids", hasMilk());
        tag.put("AnimaniaFather", father.copy());
        tag.putInt("AnimaniaMilk", milk);
        if (feeder != null) tag.putUUID("AnimaniaFeeder", feeder);
    }

    public void load(CompoundTag tag) {
        feeder = tag.hasUUID("AnimaniaFeeder") ? tag.getUUID("AnimaniaFeeder") : null;
        gestation = Math.max(0, tag.getInt("AnimaniaGestation"));
        father = tag.getCompound("AnimaniaFather").copy();
        // Only inheritance data is needed when a father is recreated at birth.
        father.remove("AnimaniaFather");
        milk = Math.max(0, tag.getInt("AnimaniaMilk"));
    }
}
