package dk.mosberg.brewing.registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import dk.mosberg.brewing.Brewing;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModItems {
    private ModItems() {}

    public static void init() {
        Path itemsRoot = FabricLoader.getInstance().getModContainer(Brewing.MOD_ID)
                .flatMap(c -> c.findPath("assets/" + Brewing.MOD_ID + "/items")).orElse(null);

        if (itemsRoot == null) {
            Brewing.LOGGER.warn("No item definition folder found at assets/{}/items",
                    Brewing.MOD_ID);
            return;
        }

        int registered = 0;

        try (var stream = Files.walk(itemsRoot)) {
            for (Path path : stream
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".json"))
                    .toList()) {
                String rel = itemsRoot.relativize(path).toString().replace('\\', '/');
                String idPath = rel.substring(0, rel.length() - ".json".length());

                Identifier id;
                try {
                    id = Identifier.of(Brewing.MOD_ID, idPath);
                } catch (Exception e) {
                    Brewing.LOGGER.warn("Skipping invalid item id path '{}' from {}", idPath, path,
                            e);
                    continue;
                }

                RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
                Item item = new Item(new Item.Settings().registryKey(key));
                if (Registries.ITEM.containsId(id)) {
                    Brewing.LOGGER.debug("Skipping already-registered item id {}", id);
                    continue;
                }

                Registry.register(Registries.ITEM, id, item);
                registered++;
            }
        } catch (IOException e) {
            Brewing.LOGGER.error("Failed to walk item definitions at {}", itemsRoot, e);
            return;
        }

        Brewing.LOGGER.info("Registered {} item(s) from assets/{}/items", registered,
                Brewing.MOD_ID);
    }
}
