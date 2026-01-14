package dk.mosberg.brewing.state;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public final class BrewingCustomData {
    public static final String ROOT_KEY = "brewing";

    private BrewingCustomData() {}

    public static Optional<NbtCompound> getRoot(ItemStack stack) {
        NbtComponent component =
                stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        if (component == null || component.isEmpty()) {
            return Optional.empty();
        }

        NbtCompound nbt = component.copyNbt();
        if (!nbt.contains(ROOT_KEY)) {
            return Optional.empty();
        }

        return nbt.getCompound(ROOT_KEY);
    }

    public static NbtCompound getOrCreateRoot(ItemStack stack) {
        NbtComponent component =
                stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = component == null ? new NbtCompound() : component.copyNbt();

        NbtCompound root;
        Optional<NbtCompound> existingRoot = nbt.getCompound(ROOT_KEY);
        if (existingRoot.isPresent()) {
            root = existingRoot.get();
        } else {
            root = new NbtCompound();
            nbt.put(ROOT_KEY, root);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }

        return root;
    }

    public static void updateRoot(ItemStack stack, Consumer<NbtCompound> rootMutator) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            NbtCompound root;
            Optional<NbtCompound> existingRoot = nbt.getCompound(ROOT_KEY);
            if (existingRoot.isPresent()) {
                root = existingRoot.get();
            } else {
                root = new NbtCompound();
                nbt.put(ROOT_KEY, root);
            }

            rootMutator.accept(root);
        });
    }
}
