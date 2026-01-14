package dk.mosberg.brewing.data.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import dk.mosberg.brewing.Brewing;
import dk.mosberg.brewing.data.BeverageDefinition;
import dk.mosberg.brewing.data.ContainerDefinition;
import dk.mosberg.brewing.data.IngredientDefinition;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

final class BrewingDataSmokeCheck {
    private BrewingDataSmokeCheck() {}

    static void run(ResourceManager manager) {
        check(manager, Identifier.of(Brewing.MOD_ID, "ingredients/water.json"),
                IngredientDefinition.class);
        check(manager, Identifier.of(Brewing.MOD_ID, "containers/glass_bottle.json"),
                ContainerDefinition.class);
        check(manager, Identifier.of(Brewing.MOD_ID, "beverages/beer/basic_beer.json"),
                BeverageDefinition.class);
    }

    private static <T> void check(ResourceManager manager, Identifier id, Class<T> type) {
        try {
            Resource resource = manager.getResource(id).orElse(null);
            if (resource == null) {
                Brewing.LOGGER.warn("Brewing data smoke check: missing {}", id);
                return;
            }

            try (InputStream in = resource.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                BrewingGson.GSON.fromJson(reader, type);
            }

            Brewing.LOGGER.info("Brewing data smoke check: OK {}", id);
        } catch (Exception e) {
            Brewing.LOGGER.error("Brewing data smoke check: FAILED {}", id, e);
        }
    }
}
