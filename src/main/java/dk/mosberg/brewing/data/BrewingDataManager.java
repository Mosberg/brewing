package dk.mosberg.brewing.data;

import java.util.Objects;

public final class BrewingDataManager {
    private static volatile BrewingData current = BrewingData.empty();

    private BrewingDataManager() {}

    public static BrewingData get() {
        return current;
    }

    public static void set(BrewingData data) {
        current = Objects.requireNonNull(data, "data");
    }
}
