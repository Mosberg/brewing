package dk.mosberg.brewing.data;

import java.util.List;
import java.util.Map;
import dk.mosberg.brewing.data.common.EventAction;

public record ContainerDefinition(String type, String id, String containerKind, int stackSize,
        String rarity, String category, Meta meta, Durability durability, Material material,
        Liquid liquid, Seal seal, Pressure pressure, Temperature temperature,
        Map<String, Object> logic, Interaction interaction, Client client, Gates gates, Text text,
        StateStorage stateStorage, Config config, Events events) {

    public record Meta(String displayName, String icon, String notes, List<String> tags,
            String sortKey) {
    }

    public record Durability(boolean breakable, int maxDamage, boolean fireproof,
            String explosionResistance) {
    }

    public record Liquid(boolean canContainLiquid, int capacityMb, List<String> acceptedTags,
            Integer defaultFillMb, List<String> deniedTags, List<String> acceptedIds,
            List<String> deniedIds, Transfer transfer) {
        public record Transfer(int fillRateMbPerTick, int pourRateMbPerTick, boolean allowPartial,
                Integer minTransferMb, Integer maxTransferMb) {
        }
    }

    public record Seal(boolean startsSealed, boolean reopenable, String sealQuality,
            double leakChancePerMinuteOpen, double oxidationMultiplierOpen,
            double spoilageMultiplierOpen, String requiresTool, Integer toolDamage) {
    }

    public record Pressure(boolean supportsPressure, String carbonationStyle, double maxPressure,
            Burst burst) {
        public record Burst(boolean enabled, double pressureThreshold, boolean dropContentsOnBurst,
                String burstSound, List<String> burstParticles) {
        }
    }

    public record Temperature(double insulationFactor, String preferredServing,
            boolean freezingSafe, boolean heatSafe, Double heatDamagePerTick,
            Double freezeDamagePerTick) {
    }

    public record Interaction(String useAction, boolean returnsContainer, String returnItemId,
            Boolean consumeOnUse, Boolean consumeOnDrink, List<String> allowFillFrom,
            List<String> allowPourTo, Integer cooldownTicks, Boolean requiresSneak,
            Boolean requiresEmptyHand, Permissions permissions) {
        public record Permissions(Boolean ownerOnly, String trustListTag) {
        }
    }

    public record Client(String renderMode, boolean liquidTintFromContent,
            boolean showFillLevelTooltip, int fillLevelSteps, String model, Sounds sounds) {
        public record Sounds(String fill, String pour, String open, String close, String drink) {
        }
    }

    public record Gates(String requiresFeatureFlag, String requiresGameruleTrue,
            List<String> requiresMods) {
    }

    public record Text(String loreKey, String tooltipKey, String flavorTextKey,
            String craftingInstructionsKey) {
    }

    public record StateStorage(String mode, int schemaVersion, Defaults defaults, ItemNbt itemNbt,
            PlacedBlock placedBlock, Conversion conversion) {
        public record Defaults(Payload payload) {
        }

        public record Payload(String contentId, int amountMb, double quality, String temperature,
                double pressure, boolean sealed, long createdTime) {
        }

        public record ItemNbt(boolean enabled, String nbtRoot, String payloadKey,
                Map<String, String> fields) {
        }

        public record PlacedBlock(boolean enabled, String blockId, String blockEntityId,
                boolean syncToClient, boolean dropsKeepContents) {
        }

        public record Conversion(String onPlace, String onBreak, String mergeStrategy) {
        }
    }

    public record Material(GlassMaterial glass, WoodMaterial wood, MetalMaterial metal) {
        public record GlassMaterial(String glassType, boolean supportsGlassVariants,
                List<String> allowedGlassTypes, String flavorBias, Integer tint) {
        }

        public record WoodMaterial(String woodType, boolean supportsWoodVariants,
                List<String> allowedWoodTypes, String flavorBias, Integer charLevel) {
        }

        public record MetalMaterial(String metalType, boolean supportsMetalVariants,
                List<String> allowedMetalTypes, String flavorBias, String coating) {
        }
    }

    public record Config(Boolean enabled, Double capacityMultiplier, Double transferRateMultiplier,
            Boolean disablePressureBurst, Boolean allowAutomation, Boolean allowHoppers,
            Boolean debugLogging) {
    }

    public record Events(List<EventAction> onFill, List<EventAction> onPour,
            List<EventAction> onOpen, List<EventAction> onClose, List<EventAction> onTick,
            List<EventAction> onPlace, List<EventAction> onBreak) {
    }
}
