package com.animania.common.registry;
import com.animania.Animania;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.*;

/** Animal persistence and tracking updates for fields used by client animation. */
@EventBusSubscriber(modid = Animania.MOD_ID)
public final class ModAttachments {
    public static final int MAX_NEED = 100;
    private static final List<Key<?>> KEYS = new ArrayList<>();
    private static final Map<Entity, CompoundTag> TRANSIENT = Collections.synchronizedMap(new WeakHashMap<>());
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Animania.MOD_ID, "animal_state"), () -> "1", "1"::equals, "1"::equals);
    public record Key<T>(String id, T initial, boolean persistent, boolean sync) {}
    private static <T> Key<T> key(String id, T initial, boolean persistent, boolean sync) {
        Key<T> key = new Key<>(id, initial, persistent, sync); KEYS.add(key); return key;
    }
    public static final Key<Boolean> FIGHTING = key("fighting", false, false, true);
    public static final Key<String> RIVAL = key("rival", "", false, true);
    public static final Key<Integer> HUNGER = key("hunger", MAX_NEED, true, false);
    public static final Key<Integer> THIRST = key("thirst", MAX_NEED, true, false);
    public static final Key<Boolean> SLEEPING = key("sleeping", false, true, true);
    public static final Key<Integer> EATING_TICKS = key("eating_ticks", 0, true, true);
    public static final Key<Boolean> STERILIZED = key("sterilized", false, true, true);
    public static final Key<Boolean> INTERACTED = key("interacted", false, true, false);
    public static final Key<Boolean> NEEDS_INITIALIZED = key("needs_initialized", false, true, false);
    public static final Key<Boolean> FED = key("fed", true, true, false);
    public static final Key<Boolean> WATERED = key("watered", true, true, false);
    public static final Key<Boolean> HAND_FED = key("hand_fed", false, true, false);
    public static final Key<Integer> FED_TIMER = key("fed_timer", -1, true, false);
    public static final Key<Integer> WATERED_TIMER = key("watered_timer", -1, true, false);
    public static final Key<Integer> STARVATION_TIMER = key("starvation_timer", 0, true, false);
    public static final Key<Integer> UNHAPPY_TIMER = key("unhappy_timer", 60, true, false);
    public static final Key<String> LAST_MATE = key("last_mate", "", true, false);
    public static final Key<String> PARENT = key("parent", "", true, false);
    public static final Key<Integer> CHILD_GROWTH = key("child_growth", 0, true, true);
    public static final Key<Integer> CHILD_GROWTH_TIMER = key("child_growth_timer", 0, true, false);
    public static final Key<Boolean> FERTILE = key("fertile", true, true, false);
    public static final Key<Integer> DRY_TIMER = key("dry_timer", -1, true, false);
    private static CompoundTag data(Entity entity, Key<?> key) {
        if (!key.persistent()) return TRANSIENT.computeIfAbsent(entity, e -> new CompoundTag());
        CompoundTag root = entity.getPersistentData();
        if (!root.contains("AnimaniaState", 10)) root.put("AnimaniaState", new CompoundTag());
        return root.getCompound("AnimaniaState");
    }
    @SuppressWarnings("unchecked")
    public static <T> T getData(Entity entity, Key<T> key) {
        CompoundTag tag = data(entity, key);
        if (!tag.contains(key.id())) return key.initial();
        Object value = key.initial() instanceof Boolean ? tag.getBoolean(key.id())
                : key.initial() instanceof Integer ? tag.getInt(key.id()) : tag.getString(key.id());
        return (T) value;
    }
    private static <T> void put(CompoundTag tag, Key<T> key, T value) {
        if (value instanceof Boolean v) tag.putBoolean(key.id(), v);
        else if (value instanceof Integer v) tag.putInt(key.id(), v);
        else tag.putString(key.id(), (String)value);
    }
    public static <T> void setData(Entity entity, Key<T> key, T value) {
        if (Objects.equals(getData(entity, key), value)) return;
        put(data(entity, key), key, value);
        if (key.sync() && !entity.level().isClientSide()) {
            CompoundTag update = new CompoundTag(); put(update, key, value);
            CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new Update(entity.getId(), update));
        }
    }
    private static <T> void snapshot(Entity entity, Key<T> key, CompoundTag tag) { put(tag, key, getData(entity, key)); }
    @SubscribeEvent public static void startTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(event.getTarget().getType()).getNamespace().equals(Animania.MOD_ID)) return;
        CompoundTag snapshot = new CompoundTag();
        for (Key<?> key : KEYS) if (key.sync()) snapshot(event.getTarget(), key, snapshot);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Update(event.getTarget().getId(), snapshot));
    }
    public record Update(int entityId, CompoundTag values) {
        public static void encode(Update msg, FriendlyByteBuf buf) { buf.writeVarInt(msg.entityId()); buf.writeNbt(msg.values()); }
        public static Update decode(FriendlyByteBuf buf) { return new Update(buf.readVarInt(), buf.readNbt()); }
    }
    public static void apply(Entity entity, CompoundTag values) {
        for (Key<?> key : KEYS) if (key.sync() && values.contains(key.id()))
            data(entity, key).put(key.id(), values.get(key.id()).copy());
    }
    public static void register(IEventBus bus) {
        CHANNEL.messageBuilder(Update.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(Update::encode).decoder(Update::decode)
            .consumerMainThread((msg, ctx) -> com.animania.client.AnimalStateReceiver.receive(msg)).add();
    }
}
