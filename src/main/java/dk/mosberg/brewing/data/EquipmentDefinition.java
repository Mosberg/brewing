package dk.mosberg.brewing.data;

import java.util.List;
import java.util.Map;
import dk.mosberg.brewing.data.common.EventAction;

public record EquipmentDefinition(String type, int schemaVersion, String id, String nameKey,
        String rarity, String material, String function, String category, Meta meta,
        Placement placement, Inventory inventory, Processing aging, Interaction interaction,
        Automation automation, Upgrades upgrades, WoodVariants woodVariants,
        MetalVariants metalVariants, Crafting crafting, Client client, Gates gates, Text text,
        Config config, Events events) {

    public record Meta(String displayName, String icon, String notes, List<String> tags) {
    }

    public record Placement(String kind, String blockId, String blockEntityId, String facing,
            boolean requiresSolidSupport, List<String> validDimensions, String requiresBiomeTag,
            Integer requiresLightLevelAtMost, Integer requiresLightLevelAtLeast,
            Map<String, Object> placementRules) {
    }

    public record Inventory(int capacity, int stackLimitPerSlot, List<String> acceptsItemTags,
            List<String> rejectsItemTags, List<String> acceptsItems, List<String> rejectsItems,
            Map<String, String> slotRoles, QuickMove quickMove,
            Map<String, SlotFilter> slotFilters) {
        public record QuickMove(boolean enabled, List<String> priority) {
        }

        public record SlotFilter(List<String> acceptsItemTags, List<String> rejectsItemTags,
                List<String> acceptsItems, List<String> rejectsItems) {
        }
    }

    public record Processing(String recipeSource, String recipeType, double agingMultiplier,
            boolean progressPersists, Integer ticksPerCycle, Integer batchSize, Quality quality,
            Spoilage spoilage, Map<String, Map<String, Object>> environmentModifiers,
            Energy energy) {
        public record Quality(Boolean enabled, Double baseQuality, Double baseQualityOnInsert,
                Double maxQuality, Double minQuality, Double qualityVariance,
                Double qualityBonusOnComplete, Double skillInfluence) {
        }

        public record Spoilage(Boolean enabled, Double baseRiskPerDay, Double baseSpoilageRate,
                Double openedMultiplier, List<String> contaminationTags, String spoiledOutputId) {
        }

        public record Energy(Boolean requiresFuel, String fuelSlotRole, Integer fuelBurnTimeTicks,
                Boolean requiresPower, String powerUnit, Double powerPerTick,
                Double consumptionPerTick) {
        }
    }

    public record Interaction(Boolean openGuiOnUse, String rightClickAction,
            String shiftRightClickAction, Boolean requiresSneakToExtract, String soundOnOpen,
            String soundOnClose, Boolean allowAutomation, Boolean requiresFuel, Lockable lockable,
            Permissions permissions) {
        public record Lockable(Boolean enabled, String lockItemTag, Boolean ownerOnlyByDefault) {
        }

        public record Permissions(Boolean ownerOnly, String trustListTag, Boolean allowPublicUse) {
        }
    }

    public record Automation(Boolean enabled, Boolean supportsHoppers, Boolean supportsPipes,
            Boolean supportsEnergy, Boolean debugLogging) {
    }

    public record Upgrades(Boolean enabled, List<String> allowedUpgradeTags, Integer maxUpgrades) {
    }

    public record WoodVariants(Boolean enabled, List<String> allowedWoods,
            Boolean inheritsRecipes) {
    }

    public record MetalVariants(Boolean enabled, List<String> allowedMetals, String coating) {
    }

    public record Crafting(String recipeType, String recipeId, Map<String, String> ingredients,
            List<String> pattern, Map<String, Object> recipeHint) {
    }

    public record Client(String screenId, boolean showProgress, boolean showQuality,
            boolean showEnvironmentState, String blockEntityRenderer, Boolean animated,
            Boolean particles) {
    }

    public record Gates(String requiresFeatureFlag, String requiresGameruleTrue) {
    }

    public record Text(String nameKey, String tooltipKey, String loreKey) {
    }

    public record Config(Boolean enabled, Double speedMultiplier, Double energyMultiplier,
            Double qualityMultiplier, Boolean allowAutomation, Boolean debugLogging) {
    }

    public record Events(List<EventAction> onTick, List<EventAction> onStart,
            List<EventAction> onComplete, List<EventAction> onFail, List<EventAction> onOpen,
            List<EventAction> onClose) {
    }
}
