package dk.mosberg.brewing.data.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import com.google.gson.JsonParseException;
import dk.mosberg.brewing.Brewing;
import dk.mosberg.brewing.data.AlcoholTypeDefinition;
import dk.mosberg.brewing.data.BeverageDefinition;
import dk.mosberg.brewing.data.BrewingData;
import dk.mosberg.brewing.data.ContainerDefinition;
import dk.mosberg.brewing.data.EquipmentDefinition;
import dk.mosberg.brewing.data.IngredientDefinition;
import dk.mosberg.brewing.data.MethodDefinition;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class BrewingDataLoader {
    private BrewingDataLoader() {}

    @SuppressWarnings("null")
    public static BrewingData loadAll(ResourceManager resourceManager) {
        Map<String, AlcoholTypeDefinition> alcoholTypes = loadFolder(resourceManager,
                "alcohol_types", AlcoholTypeDefinition.class, AlcoholTypeDefinition::id);
        Map<String, IngredientDefinition> ingredients = loadFolder(resourceManager, "ingredients",
                IngredientDefinition.class, IngredientDefinition::id);
        Map<String, MethodDefinition> methods = loadFolder(resourceManager, "methods",
                MethodDefinition.class, MethodDefinition::id);
        Map<String, EquipmentDefinition> equipment = loadFolder(resourceManager, "equipment",
                EquipmentDefinition.class, EquipmentDefinition::id);
        Map<String, ContainerDefinition> containers = loadFolder(resourceManager, "containers",
                ContainerDefinition.class, ContainerDefinition::id);
        Map<String, BeverageDefinition> beverages = loadFolder(resourceManager, "beverages",
                BeverageDefinition.class, BeverageDefinition::id);

        return new BrewingData(alcoholTypes, ingredients, methods, equipment, containers,
                beverages);
    }

    private static <T> Map<String, T> loadFolder(ResourceManager resourceManager, String folder,
            Class<T> clazz, Function<T, String> keyFn) {
        Map<String, T> out = new HashMap<>();
        var resources = resourceManager.findResources(folder,
                id -> Brewing.MOD_ID.equals(id.getNamespace()) && id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            if (id.getPath().contains("/schemas/") || id.getPath().endsWith("-schema.json")) {
                continue;
            }
            try (BufferedReader reader = entry.getValue().getReader()) {
                T parsed = BrewingGson.GSON.fromJson(reader, clazz);
                if (parsed == null) {
                    throw new JsonParseException("Parsed null for " + id);
                }

                String key = keyFn.apply(parsed);
                if (key == null || key.isBlank()) {
                    Brewing.LOGGER.error(
                            "Parsed brewing data {} ({}) but it had no usable id() for keying", id,
                            clazz.getSimpleName());
                    continue;
                }

                if (out.containsKey(key)) {
                    Brewing.LOGGER.warn(
                            "Duplicate brewing data id '{}' while loading {} ({}); last one wins",
                            key, folder, id);
                }

                out.put(key, parsed);
            } catch (IOException | RuntimeException e) {
                Brewing.LOGGER.error("Failed to parse brewing data {} ({})", id,
                        clazz.getSimpleName(), e);
            }
        }

        Brewing.LOGGER.info("Loaded brewing {}: {} file(s)", folder, out.size());
        return Map.copyOf(out);
    }
}
