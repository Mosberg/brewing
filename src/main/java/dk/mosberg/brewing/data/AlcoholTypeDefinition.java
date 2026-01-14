package dk.mosberg.brewing.data;

import java.util.List;

public record AlcoholTypeDefinition(String id, String displayName, List<Double> abvRangePct,
        String baseMethod, Meta meta, Style style, Processing processing, Effects effects,
        Constraints constraints, Config config) {

    public record Meta(String notes, List<String> tags, String icon) {
    }

    public record Style(Integer color, String clarity, String carbonation, String sweetness,
            String body, Double bitterness, List<String> aromaNotes) {
    }

    public record Processing(List<String> defaultMethodChain, Double targetAbvPct, Aging aging) {
        public record Aging(Boolean recommended, Double minDays, Double maxDays,
                Boolean woodInfluence) {
        }
    }

    public record Effects(Intoxication intoxication, List<StatusEffect> statusEffects) {
        public record Intoxication(Double baseStrength, Double strengthPerAbv,
                Double nauseaThreshold, Double blackoutThreshold) {
        }
    }

    public record Constraints(String requiresFeatureFlag, String requiresGameruleTrue,
            Integer minPlayerLevel) {
    }

    public record Config(Boolean enabled, Double abvMultiplier, Double effectStrengthMultiplier,
            Boolean debugLogging) {
    }

    public record StatusEffect(String effect, Integer durationTicks, Integer amplifier,
            Double chance) {
    }
}
