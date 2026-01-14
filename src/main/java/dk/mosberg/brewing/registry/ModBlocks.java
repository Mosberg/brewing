package dk.mosberg.brewing.registry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import dk.mosberg.brewing.Brewing;
import dk.mosberg.brewing.block.ContainerBlock;
import dk.mosberg.brewing.block.EquipmentBlock;
import dk.mosberg.brewing.data.EquipmentDefinition;
import dk.mosberg.brewing.data.loader.BrewingGson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    private static final List<Block> CONTAINERS = new ArrayList<>();
    private static final List<Block> EQUIPMENT = new ArrayList<>();

    private ModBlocks() {}

    public static List<Block> containerBlocks() {
        return List.copyOf(CONTAINERS);
    }

    public static List<Block> equipmentBlocks() {
        return List.copyOf(EQUIPMENT);
    }

    public static void init() {
        registerContainerBlocksFromData();
        registerEquipmentBlocksFromData();

        Brewing.LOGGER.info("Registered {} container block(s) and {} equipment block(s)",
                CONTAINERS.size(), EQUIPMENT.size());
    }

    private static void registerContainerBlocksFromData() {
        Path root = FabricLoader.getInstance().getModContainer(Brewing.MOD_ID)
                .flatMap(c -> c.findPath("data/" + Brewing.MOD_ID + "/containers")).orElse(null);

        if (root == null) {
            Brewing.LOGGER.warn("No built-in container data folder found at data/{}/containers",
                    Brewing.MOD_ID);
            return;
        }

        try (var stream = Files.list(root)) {
            for (Path path : stream
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".json"))
                    .toList()) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.length() - ".json".length());

                Identifier blockId = Identifier.of(Brewing.MOD_ID, "containers/" + baseName);
                registerBlockWithItem(blockId, new ContainerBlock(blockSettings(blockId)));
            }
        } catch (IOException e) {
            Brewing.LOGGER.error("Failed to list built-in containers at {}", root, e);
        }
    }

    private static void registerEquipmentBlocksFromData() {
        Path root = FabricLoader.getInstance().getModContainer(Brewing.MOD_ID)
                .flatMap(c -> c.findPath("data/" + Brewing.MOD_ID + "/equipment")).orElse(null);

        if (root == null) {
            Brewing.LOGGER.warn("No built-in equipment data folder found at data/{}/equipment",
                    Brewing.MOD_ID);
            return;
        }

        try (var stream = Files.list(root)) {
            for (Path path : stream
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".json"))
                    .toList()) {
                String fileName = path.getFileName().toString();
                String baseName = fileName.substring(0, fileName.length() - ".json".length());
                String idName = baseName.equals("brewing_kettle") ? "brew_kettle" : baseName;

                // Ensure the json is schema-valid enough to parse; we derive the block id from the
                // file name.
                try (var reader = Files.newBufferedReader(path)) {
                    BrewingGson.GSON.fromJson(reader, EquipmentDefinition.class);
                }

                Identifier blockId = Identifier.of(Brewing.MOD_ID, "equipment/" + idName);
                registerBlockWithItem(blockId, new EquipmentBlock(blockSettings(blockId)));
            }
        } catch (Exception e) {
            Brewing.LOGGER.error("Failed to register equipment blocks from {}", root, e);
        }
    }

    private static AbstractBlock.Settings blockSettings(Identifier blockId) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, blockId);
        return AbstractBlock.Settings.create().registryKey(key).strength(1.0f);
    }

    private static void registerBlockWithItem(Identifier blockId, Block block) {
        if (Registries.BLOCK.containsId(blockId)) {
            Brewing.LOGGER.warn("Skipping duplicate block id {}", blockId);
            return;
        }

        Registry.register(Registries.BLOCK, blockId, block);

        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, blockId);
        BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));

        if (Registries.ITEM.containsId(blockId)) {
            Brewing.LOGGER.warn("Skipping duplicate block item id {}", blockId);
            return;
        }

        Registry.register(Registries.ITEM, blockId, blockItem);

        if (block instanceof ContainerBlock) {
            CONTAINERS.add(block);
        } else {
            EQUIPMENT.add(block);
        }
    }
}
