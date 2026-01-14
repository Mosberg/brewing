package dk.mosberg.brewing.state;

import java.util.Optional;
import dk.mosberg.brewing.data.ContainerDefinition;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public final class ContainerStateStorage {
    private static final String BREWING_PAYLOAD_KEY = "payload";

    private ContainerStateStorage() {}

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
