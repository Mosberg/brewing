package dk.mosberg.brewing.block;

import com.mojang.serialization.MapCodec;
import dk.mosberg.brewing.block.entity.EquipmentBlockEntity;
import dk.mosberg.brewing.data.BrewingDataManager;
import dk.mosberg.brewing.data.EquipmentDefinition;
import dk.mosberg.brewing.registry.ModBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class EquipmentBlock extends BlockWithEntity {
    public static final MapCodec<EquipmentBlock> CODEC =
            AbstractBlock.createCodec(EquipmentBlock::new);

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public EquipmentBlock(Settings settings) {
        super(settings);

        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
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
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(
            StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
            BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        Identifier definitionId = ModBlocks.equipmentDefinitionIdForBlock(blockId);
        if (definitionId == null) {
            player.sendMessage(Text.literal("[Brewing] Equipment block: " + blockId), false);
            return ActionResult.SUCCESS;
        }

        EquipmentDefinition def = BrewingDataManager.get().equipment().get(definitionId.toString());
        if (def == null) {
            player.sendMessage(Text.literal("[Brewing] Equipment block: " + blockId + " -> "
                    + definitionId + " (definition not loaded?)"), false);
            return ActionResult.SUCCESS;
        }

        player.sendMessage(Text.literal("[Brewing] Equipment: " + definitionId + " (function="
                + def.function() + ", material=" + def.material() + ")"), false);

        return ActionResult.SUCCESS;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
