package dk.mosberg.brewing.data;

import java.util.List;
import java.util.Map;

public record IngredientDefinition(String id, String displayName, String kind, Meta meta,
        ItemMapping item, Properties properties, Processing processing, Effects effects,
        Constraints constraints, Config config) {

    public record Meta(String notes, List<String> tags, String icon) {
    }

    public record ItemMapping(String itemId, String itemTag, Integer count,
            Map<String, Object> nbt) {
    }

    public record Properties(Double sugarGPer100g, Double starchGPer100g, Double acidityPh,
            Double bitternessIbu, Double tannin, Double oiliness, Double colorSrm,
            Double aromaIntensity, Double fermentability, Double distillability) {
    }

    public record Processing(MethodModifier mashing, MethodModifier boiling,
            MethodModifier fermentation, MethodModifier maceration, MethodModifier filtration,
            MethodModifier conditioning, MethodModifier distillation, MethodModifier aging) {
        public record MethodModifier(Boolean enabled, Double timeMultiplier, Double yieldMultiplier,
                Double qualityAdd, Double qualityMultiplier, Double requiresMinTemperature,
                Double requiresMaxTemperature, Integer consumptionMb, List<Byproduct> byproducts) {
        }

        public record Byproduct(String id, Double chance, Integer count) {
        }
    }

    public record Effects(List<String> flavorNotes, List<StatusEffect> statusEffects,
            QualityModifiers qualityModifiers) {
        public record QualityModifiers(Double base, Double multiplier) {
        }
    }

    public record Constraints(String requiresFeatureFlag, String requiresGameruleTrue,
            List<String> allowedMethods, List<String> deniedMethods) {
    }

    public record Config(Boolean enabled, Double rarityWeight, Boolean debugLogging) {
    }

    public record StatusEffect(String id, Integer durationTicks, Integer amplifier, Double chance) {
    }
}
