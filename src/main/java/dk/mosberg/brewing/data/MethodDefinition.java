package dk.mosberg.brewing.data;

import java.util.List;
import dk.mosberg.brewing.data.common.EventAction;

public record MethodDefinition(String id, String nameKey, String tooltipKey, List<String> roles,
        Meta meta, Ui ui, Compat compat, Processing processing, Quality quality, Effects effects,
        Config config) {
    public record Meta(String displayName, String notes, String icon, List<String> tags) {
    }

    public record Ui(Integer color, Integer sortOrder, Boolean showInJei) {
    }

    public record Compat(List<String> allowedEquipmentIds, List<String> deniedEquipmentIds,
            List<String> allowedContainerKinds, String requiresFeatureFlag,
            String requiresGameruleTrue) {
    }

    public record Processing(Integer baseTimeTicks, Double timeVariance, Integer batchSize,
            Double successChance, Boolean consumesCatalyst, Boolean requiresHeat,
            Boolean requiresCooling, Double optimalTemperature, Double temperatureTolerance,
            List<Double> optimalPressureRange) {
    }

    public record Quality(Boolean enabled, Double base, Double min, Double max, Double variance,
            Boolean affectsYield, Boolean affectsEffects) {
    }

    public record Effects(List<EventAction> onSuccess, List<EventAction> onFail,
            List<EventAction> onTick) {
    }

    public record Config(Boolean enabled, Double timeMultiplier, Double qualityMultiplier,
            Double yieldMultiplier) {
    }
}
