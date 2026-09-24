package com.animania.common.entity;

import com.animania.common.registry.ModAttachments;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderLookup;
import java.util.*;

/** Deliver family changes after a parent or mate's chunk has been loaded again. */
public final class FamilyUpdates extends SavedData {
    private final Map<String, Set<String>> weaned = new HashMap<>();
    private final Map<String, String> bereaved = new HashMap<>();
    private static FamilyUpdates get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(FamilyUpdates::new, (tag, provider) -> load(tag)), "animania_family_updates");
    }
    private static FamilyUpdates load(CompoundTag tag) {
        FamilyUpdates data = new FamilyUpdates();
        ListTag list = tag.getList("Weaned", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            data.weaned.computeIfAbsent(entry.getString("Mother"), ignored -> new HashSet<>()).add(entry.getString("Child"));
        }
        CompoundTag mates = tag.getCompound("Bereaved");
        for (String id : mates.getAllKeys()) data.bereaved.put(id, mates.getString(id));
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        weaned.forEach((mother, children) -> children.forEach(child -> {
            CompoundTag entry = new CompoundTag(); entry.putString("Mother", mother); entry.putString("Child", child); list.add(entry);
        }));
        tag.put("Weaned", list);
        CompoundTag mates = new CompoundTag(); bereaved.forEach(mates::putString); tag.put("Bereaved", mates);
        return tag;
    }
    public static void weaned(Animal child) {
        if (!(child.level() instanceof ServerLevel level)) return;
        String mother = child.getData(ModAttachments.PARENT), id = child.getData(ModAttachments.NURSING_ID);
        if (mother.isEmpty() || id.isEmpty()) return;
        FamilyUpdates data = get(level);
        data.weaned.computeIfAbsent(mother, ignored -> new HashSet<>()).add(id);
        child.setData(ModAttachments.NURSING_ID, ""); data.setDirty();
    }
    public static void died(Animal animal) {
        if (!(animal.level() instanceof ServerLevel level)) return;
        weaned(animal);
        String mate = animal.getData(ModAttachments.LAST_MATE);
        if (!mate.isEmpty()) {
            FamilyUpdates data = get(level); data.bereaved.put(mate, animal.getUUID().toString()); data.setDirty();
        }
    }
    public static void receive(Animal animal) {
        if (!(animal.level() instanceof ServerLevel level)) return;
        FamilyUpdates data = get(level);
        String id = animal.getUUID().toString();
        Set<String> removed = data.weaned.remove(id);
        if (removed != null) {
            Set<String> children = new LinkedHashSet<>(Arrays.asList(animal.getData(ModAttachments.NURSING_YOUNG).split(",")));
            children.remove(""); children.removeAll(removed);
            animal.setData(ModAttachments.NURSING_YOUNG, String.join(",", children));
            if (children.isEmpty()) FamilyLifecycle.endLactation(animal);
            data.setDirty();
        }
        String deadMate = data.bereaved.remove(id);
        if (deadMate != null) {
            if (animal.getData(ModAttachments.LAST_MATE).equals(deadMate)) animal.setData(ModAttachments.LAST_MATE, "");
            data.setDirty();
        }
    }
}
