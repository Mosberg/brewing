package dk.mosberg.brewing.block;

import com.mojang.serialization.MapCodec;
import dk.mosberg.brewing.block.entity.EquipmentBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public final class EquipmentBlock extends BlockWithEntity {
    public static final MapCodec<EquipmentBlock> CODEC =
            AbstractBlock.createCodec(EquipmentBlock::new);

    public EquipmentBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EquipmentBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
