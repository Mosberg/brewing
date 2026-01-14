package dk.mosberg.brewing.block;

import com.mojang.serialization.MapCodec;
import dk.mosberg.brewing.block.entity.ContainerBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public final class ContainerBlock extends BlockWithEntity {
    public static final MapCodec<ContainerBlock> CODEC =
            AbstractBlock.createCodec(ContainerBlock::new);

    public ContainerBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ContainerBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
