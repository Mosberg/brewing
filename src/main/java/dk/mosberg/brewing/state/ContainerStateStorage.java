package dk.mosberg.brewing.state;

import java.util.Optional;
import dk.mosberg.brewing.data.ContainerDefinition;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public final class ContainerStateStorage {
    private static final String BREWING_PAYLOAD_KEY = "payload";

    private static final String MODE_ITEM = "item";
    private static final String MODE_BLOCK = "block";
    private static final String MODE_BOTH = "both";

    private static final String MERGE_STRATEGY_REPLACE = "replace";
    private static final String MERGE_STRATEGY_MERGE = "merge";

    private ContainerStateStorage() {}

    public static boolean supportsBlockStorage(ContainerDefinition container) {
        ContainerDefinition.StateStorage ss = container == null ? null : container.stateStorage();
        if (ss == null || ss.mode() == null) {
            return false;
        }

        return MODE_BLOCK.equals(ss.mode()) || MODE_BOTH.equals(ss.mode());
    }

    public static boolean shouldCopyItemToBlock(ContainerDefinition container) {
        ContainerDefinition.StateStorage ss = container == null ? null : container.stateStorage();
        if (ss == null || ss.conversion() == null) {
            return false;
        }

        String action = ss.conversion().onPlace();
        if (action == null) {
            return false;
        }

        String normalized = action.trim().toLowerCase();
        return normalized.contains("copy")
                && (normalized.contains("block") || normalized.contains("block_entity"));
    }

    public static boolean shouldCopyBlockToItem(ContainerDefinition container) {
        ContainerDefinition.StateStorage ss = container == null ? null : container.stateStorage();
        if (ss == null || ss.conversion() == null) {
            return false;
        }

        String action = ss.conversion().onBreak();
        if (action == null) {
            return false;
        }

        String normalized = action.trim().toLowerCase();
        return normalized.contains("copy") && normalized.contains("item");
    }

    public static ContainerPayload defaultPayload(ContainerDefinition container) {
        if (container == null || container.stateStorage() == null
                || container.stateStorage().defaults() == null
                || container.stateStorage().defaults().payload() == null) {
            return ContainerPayload.empty();
        }

        ContainerDefinition.StateStorage.Payload p = container.stateStorage().defaults().payload();
        return new ContainerPayload(p.contentId(), p.amountMb(), p.quality(), p.temperature(),
                p.pressure(), p.sealed(), p.createdTime());
    }

    public static ContainerPayload applyDefaults(ContainerDefinition container,
            ContainerPayload payload) {
        ContainerPayload defaults = defaultPayload(container);

        ContainerDefinition.StateStorage ss = container == null ? null : container.stateStorage();
        String mergeStrategy = (ss == null || ss.conversion() == null) ? MERGE_STRATEGY_REPLACE
                : ss.conversion().mergeStrategy();

        ContainerPayload effective = payload == null ? ContainerPayload.empty() : payload;

        if (MERGE_STRATEGY_MERGE.equals(mergeStrategy)) {
            return merge(defaults, effective);
        }

        if (effective.isEmpty()) {
            return defaults;
        }

        return effective;
    }

    private static ContainerPayload merge(ContainerPayload base, ContainerPayload override) {
        if (base == null) {
            return override == null ? ContainerPayload.empty() : override;
        }
        if (override == null || override.isEmpty()) {
            return base;
        }

        boolean hasMeaningfulOverrides =
                (override.contentId() != null && !override.contentId().isBlank())
                        || override.amountMb() != 0 || override.quality() != 0.0
                        || (override.temperature() != null && !override.temperature().isBlank())
                        || override.pressure() != 0.0 || override.createdTime() != 0L;

        String contentId = (override.contentId() != null && !override.contentId().isBlank())
                ? override.contentId()
                : base.contentId();
        int amountMb = (override.amountMb() != 0) ? override.amountMb() : base.amountMb();
        double quality = (override.quality() != 0.0) ? override.quality() : base.quality();
        String temperature = (override.temperature() != null && !override.temperature().isBlank())
                ? override.temperature()
                : base.temperature();
        double pressure = (override.pressure() != 0.0) ? override.pressure() : base.pressure();
        boolean sealed = hasMeaningfulOverrides ? override.sealed() : base.sealed();
        long createdTime =
                (override.createdTime() != 0L) ? override.createdTime() : base.createdTime();

        return new ContainerPayload(contentId, amountMb, quality, temperature, pressure, sealed,
                createdTime);
    }

    public static Optional<ContainerPayload> readPayloadFromItem(ItemStack stack,
            ContainerDefinition container) {
        if (stack == null || stack.isEmpty() || container == null) {
            return Optional.empty();
        }

        // Preferred format: CUSTOM_DATA -> { brewing: { payload: { ... } } }
        Optional<NbtCompound> rootOpt = BrewingCustomData.getRoot(stack);
        if (rootOpt.isPresent()) {
            NbtCompound root = rootOpt.get();
            return root.getCompound(BREWING_PAYLOAD_KEY).map(ContainerPayload::fromNbt);
        }

        // Fallback: schema-configured legacy-ish keys inside CUSTOM_DATA
        ContainerDefinition.StateStorage ss = container.stateStorage();
        if (ss == null || ss.itemNbt() == null || !ss.itemNbt().enabled()) {
            return Optional.empty();
        }

        NbtComponent component =
                stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        if (component == null || component.isEmpty()) {
            return Optional.empty();
        }

        NbtCompound custom = component.copyNbt();

        String rootKey = ss.itemNbt().nbtRoot();
        String payloadKey = ss.itemNbt().payloadKey();
        if (rootKey == null || rootKey.isBlank() || payloadKey == null || payloadKey.isBlank()) {
            return Optional.empty();
        }

        Optional<NbtCompound> rootOpt2 = custom.getCompound(rootKey);
        if (rootOpt2.isEmpty()) {
            return Optional.empty();
        }

        return rootOpt2.get().getCompound(payloadKey).map(ContainerPayload::fromNbt);
    }

    public static void writePayloadToItem(ItemStack stack, ContainerDefinition container,
            ContainerPayload payload) {
        if (stack == null || stack.isEmpty() || container == null || payload == null) {
            return;
        }

        // Always write the mod-rooted format.
        BrewingCustomData.updateRoot(stack, root -> {
            if (payload.isEmpty()) {
                root.remove(BREWING_PAYLOAD_KEY);
            } else {
                root.put(BREWING_PAYLOAD_KEY, payload.toNbt());
            }
        });

        // Optionally mirror into schema-configured root/payloadKey.
        ContainerDefinition.StateStorage ss = container.stateStorage();
        if (ss == null || ss.itemNbt() == null || !ss.itemNbt().enabled()) {
            return;
        }

        String rootKey = ss.itemNbt().nbtRoot();
        String payloadKey = ss.itemNbt().payloadKey();
        if (rootKey == null || rootKey.isBlank() || payloadKey == null || payloadKey.isBlank()) {
            return;
        }

        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, custom -> {
            NbtCompound root;
            Optional<NbtCompound> existingRoot = custom.getCompound(rootKey);
            if (existingRoot.isPresent()) {
                root = existingRoot.get();
            } else {
                root = new NbtCompound();
                custom.put(rootKey, root);
            }

            if (payload.isEmpty()) {
                root.remove(payloadKey);
            } else {
                root.put(payloadKey, payload.toNbt());
            }
        });
    }
}
