package dk.mosberg.brewing.data.loader;

import dk.mosberg.brewing.Brewing;
import dk.mosberg.brewing.data.BrewingData;
import dk.mosberg.brewing.data.BrewingDataManager;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public final class BrewingDataReloadListener implements SynchronousResourceReloader {
    public static final @NotNull Identifier ID = Identifier.of(Brewing.MOD_ID, "brewing_data");

    @SuppressWarnings("null")
    public static void register() {
        ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(
            Identifier.of(Brewing.MOD_ID, "brewing_data"), new BrewingDataReloadListener());
        Brewing.LOGGER.info("Registered brewing data reload listener");
    }

    @Override
    public void reload(ResourceManager manager) {
        BrewingData data = BrewingDataLoader.loadAll(manager);
        BrewingDataManager.set(data);

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            BrewingDataSmokeCheck.run(manager);
        }

        Brewing.LOGGER.info(
                "Brewing data loaded: {} alcohol_types, {} ingredients, {} methods, {} equipment, {} containers, {} beverages",
                data.alcoholTypes().size(), data.ingredients().size(), data.methods().size(),
                data.equipment().size(), data.containers().size(), data.beverages().size());
    }
}
