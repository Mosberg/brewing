package dk.mosberg.brewing.block.entity;

import dk.mosberg.brewing.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public final class EquipmentBlockEntity extends BlockEntity {
    public EquipmentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EQUIPMENT, pos, state);
    }
}
