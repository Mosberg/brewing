package dk.mosberg.brewing.data;

import java.util.List;

public record BeverageDefinition(String type, int schemaVersion, String id, String category,
        String style, String container, String rarity, int stackSize, Brewing brewing, Stats stats,
        Quality quality, Aging aging, Spoilage spoilage, Carbonation carbonation, Visuals visuals,
        List<StatusEffect> effects, List<IngredientStack> ingredients, List<String> tags, Loot loot,
        Gates gates, Client client, Text text, Meta meta) {

    public record Meta(String displayName, String icon, Integer sortOrder, Boolean hidden,
            String notes, Source source) {
        public record Source(String pack, String author, String homepage) {
        }
    }

    public record Brewing(double brewTimeSeconds, int brewTimeTicks, List<String> stationTags,
            int difficulty, int batchSizeServings, List<Byproduct> byproducts, Failure failure) {
        public record Byproduct(String item, int count, double chance) {
        }

        public record Failure(Boolean enabled, Double baseFailChance, String outcome,
                List<StatusEffect> extraEffects) {
        }
    }

    public record Stats(double alcoholByVolume, double strength, Intoxication intoxication,
            Nutrition nutrition) {
        public record Intoxication(double value, double decayRatePerTick) {
        }

        public record Nutrition(int hunger, double saturation) {
        }
    }

    public record Quality(String tier, boolean supportsQuality, Double qualityOnBrew,
            Double qualityFloor, Double qualityCeiling) {
    }

    public record Aging(Boolean supported, Double minDays, Double maxDays,
            List<String> preferredContainerTags, Double qualityBonusPerDay, Double riskPerDayOpen) {
    }

    public record Spoilage(Boolean enabled, Double baseDecayPerDay, Double openedDecayMultiplier,
            Temperature temperature) {
        public record Temperature(String preferred, Double hotDecayMultiplier,
                Double coldDecayMultiplier) {
        }
    }

    public record Carbonation(String level, Foam foam) {
        public record Foam(Boolean enabled, Double spillChanceOnOpen) {
        }
    }

    public record Visuals(int liquidColor, String bubbles, String glow, String particle) {
    }

    public record StatusEffect(String effect, Integer duration, Integer amplifier, Double chance,
            Boolean showParticles, Boolean showIcon, Boolean ambient) {
    }

    public record IngredientStack(String item, Integer count) {
    }

    public record Loot(int weight, List<String> tables) {
    }

    public record Gates(String requiresFeatureFlag, String requiresGameruleTrue,
            Integer minPlayerLevel, List<String> requiresModLoaded) {
    }

    public record Client(Boolean useLiquidTint, Boolean showStrengthLine, Boolean showQualityLine,
            Boolean showSpoilageHint, Boolean showAlcoholByVolume, String tooltipTheme) {
    }

    public record Text(String nameKey, String loreKey, String tooltipKey, String effectTextKey,
            String brewTimeTextKey, String ingredientsTextKey, String containerTextKey,
            String rarityTextKey, String categoryTextKey, String flavorTextKey, String warningKey,
            String craftingInstructionsKey) {
    }
}
