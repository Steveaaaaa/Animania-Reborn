package com.animania.common.registry;

import com.animania.Animania;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final int MAX_NEED = 100;

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Animania.MOD_ID);

    public static final Supplier<AttachmentType<Integer>> HUNGER = ATTACHMENTS.register(
            "hunger",
            () -> AttachmentType.builder(() -> MAX_NEED).serialize(Codec.intRange(0, MAX_NEED)).build()
    );

    public static final Supplier<AttachmentType<Integer>> THIRST = ATTACHMENTS.register(
            "thirst",
            () -> AttachmentType.builder(() -> MAX_NEED).serialize(Codec.intRange(0, MAX_NEED)).build()
    );

    public static final Supplier<AttachmentType<Boolean>> SLEEPING = ATTACHMENTS.register(
            "sleeping",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<Integer>> EATING_TICKS = ATTACHMENTS.register(
            "eating_ticks",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.intRange(0, 80))
                    .sync(ByteBufCodecs.VAR_INT).build()
    );

    /** Persistent husbandry data used by gameplay and information-overlay integrations. */
    public static final Supplier<AttachmentType<Boolean>> STERILIZED = ATTACHMENTS.register(
            "sterilized",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).sync(ByteBufCodecs.BOOL).build()
    );

    public static final Supplier<AttachmentType<Boolean>> INTERACTED = ATTACHMENTS.register(
            "interacted",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    /** Original 1.12 care-and-feeding state.  These values, rather than the
     * compatibility percentages above, are authoritative for gameplay. */
    public static final Supplier<AttachmentType<Boolean>> NEEDS_INITIALIZED = ATTACHMENTS.register(
            "needs_initialized",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    public static final Supplier<AttachmentType<Boolean>> FED = ATTACHMENTS.register(
            "fed",
            () -> AttachmentType.builder(() -> true).serialize(Codec.BOOL).build()
    );

    public static final Supplier<AttachmentType<Boolean>> WATERED = ATTACHMENTS.register(
            "watered",
            () -> AttachmentType.builder(() -> true).serialize(Codec.BOOL).build()
    );

    public static final Supplier<AttachmentType<Boolean>> HAND_FED = ATTACHMENTS.register(
            "hand_fed",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build()
    );

    public static final Supplier<AttachmentType<Integer>> FED_TIMER = ATTACHMENTS.register(
            "fed_timer",
            () -> AttachmentType.builder(() -> -1).serialize(Codec.intRange(-1, 20_000_000)).build()
    );

    public static final Supplier<AttachmentType<Integer>> WATERED_TIMER = ATTACHMENTS.register(
            "watered_timer",
            () -> AttachmentType.builder(() -> -1).serialize(Codec.intRange(-1, 20_000_000)).build()
    );

    public static final Supplier<AttachmentType<Integer>> STARVATION_TIMER = ATTACHMENTS.register(
            "starvation_timer",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.intRange(0, 9_600_000)).build()
    );

    public static final Supplier<AttachmentType<Integer>> UNHAPPY_TIMER = ATTACHMENTS.register(
            "unhappy_timer",
            () -> AttachmentType.builder(() -> 60).serialize(Codec.intRange(-1, 60)).build()
    );

    public static final Supplier<AttachmentType<String>> LAST_MATE = ATTACHMENTS.register(
            "last_mate",
            () -> AttachmentType.builder(() -> "").serialize(Codec.STRING).build()
    );

    public static final Supplier<AttachmentType<String>> PARENT = ATTACHMENTS.register(
            "parent",
            () -> AttachmentType.builder(() -> "").serialize(Codec.STRING).build()
    );

    /** Number of completed 1% legacy growth steps (0..85). */
    public static final Supplier<AttachmentType<Integer>> CHILD_GROWTH = ATTACHMENTS.register(
            "child_growth",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.intRange(0, 85)).build()
    );

    /** Tick accumulator for the next legacy growth step. */
    public static final Supplier<AttachmentType<Integer>> CHILD_GROWTH_TIMER = ATTACHMENTS.register(
            "child_growth_timer",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.intRange(0, 20_000_000)).build()
    );

    /** Original female fertility/dry-period state, independent of vanilla love mode. */
    public static final Supplier<AttachmentType<Boolean>> FERTILE = ATTACHMENTS.register(
            "fertile",
            () -> AttachmentType.builder(() -> true).serialize(Codec.BOOL).build()
    );

    public static final Supplier<AttachmentType<Integer>> DRY_TIMER = ATTACHMENTS.register(
            "dry_timer",
            () -> AttachmentType.builder(() -> -1).serialize(Codec.intRange(-1, 20_000_000)).build()
    );

    private ModAttachments() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
