package dk.mosberg.brewing.data;

import java.util.Map;

public record BrewingData(Map<String, AlcoholTypeDefinition> alcoholTypes,
        Map<String, IngredientDefinition> ingredients, Map<String, MethodDefinition> methods,
        Map<String, EquipmentDefinition> equipment, Map<String, ContainerDefinition> containers,
        Map<String, BeverageDefinition> beverages) {
    public static BrewingData empty() {
        return new BrewingData(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
    }
}
