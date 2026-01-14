package dk.mosberg.brewing.registry;

import dk.mosberg.brewing.Brewing;
import dk.mosberg.brewing.block.entity.ContainerBlockEntity;
import dk.mosberg.brewing.block.entity.EquipmentBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {
    public static BlockEntityType<ContainerBlockEntity> CONTAINER;
    public static BlockEntityType<EquipmentBlockEntity> EQUIPMENT;

    private ModBlockEntities() {}

    @SuppressWarnings("null")
    public static void init() {
        if (CONTAINER != null || EQUIPMENT != null) {
            return;
        }

        CONTAINER = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(Brewing.MOD_ID, "container"),
                FabricBlockEntityTypeBuilder.create(ContainerBlockEntity::new,
                        ModBlocks.containerBlocks().toArray(Block[]::new)).build());

        EQUIPMENT = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(Brewing.MOD_ID, "equipment"),
                FabricBlockEntityTypeBuilder.create(EquipmentBlockEntity::new,
                        ModBlocks.equipmentBlocks().toArray(Block[]::new)).build());

        Brewing.LOGGER.info("Registered block entity types (container/equipment)");
    }
}
