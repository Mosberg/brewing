package dk.mosberg.brewing.block.entity;

import dk.mosberg.brewing.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public final class ContainerBlockEntity extends BlockEntity {
    public ContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTAINER, pos, state);
    }
}
