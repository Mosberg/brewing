package dk.mosberg.brewing.registry;

import dk.mosberg.brewing.Brewing;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
    public static ItemGroup BREWING;

    private ModItemGroups() {}

    public static void init() {
        if (BREWING != null) {
            return;
        }

        BREWING = Registry.register(Registries.ITEM_GROUP, Identifier.of(Brewing.MOD_ID, "brewing"),
                FabricItemGroup.builder().displayName(Text.translatable("itemGroup.brewing"))
                        .icon(ModItemGroups::defaultIcon).entries((displayContext, entries) -> {
                            for (Identifier id : Registries.ITEM.getIds()) {
                                if (!Brewing.MOD_ID.equals(id.getNamespace())) {
                                    continue;
                                }

                                Item item = Registries.ITEM.get(id);
                                if (item == Items.AIR) {
                                    continue;
                                }

                                entries.add(item);
                            }
                        }).build());

        Brewing.LOGGER.info("Registered creative tab {}", Registries.ITEM_GROUP.getId(BREWING));
    }

    private static ItemStack defaultIcon() {
        // Prefer a mod item as the tab icon (first registered brewing:* item), fallback to vanilla.
        for (Identifier id : Registries.ITEM.getIds()) {
            if (!Brewing.MOD_ID.equals(id.getNamespace())) {
                continue;
            }

            Item item = Registries.ITEM.get(id);
            if (item == Items.AIR) {
                continue;
            }

            return new ItemStack(item);
        }

        return new ItemStack(Items.BREWING_STAND);
    }
}
