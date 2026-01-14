package dk.mosberg.brewing.block;

import java.util.List;
import com.mojang.serialization.MapCodec;
import dk.mosberg.brewing.block.entity.ContainerBlockEntity;
import dk.mosberg.brewing.data.BrewingDataManager;
import dk.mosberg.brewing.data.ContainerDefinition;
import dk.mosberg.brewing.registry.ModBlocks;
import dk.mosberg.brewing.state.ContainerPayload;
import dk.mosberg.brewing.state.ContainerStateStorage;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
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

public final class ContainerBlock extends BlockWithEntity {
    public static final MapCodec<ContainerBlock> CODEC =
            AbstractBlock.createCodec(ContainerBlock::new);

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public ContainerBlock(Settings settings) {
        super(settings);

        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
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
    public void onPlaced(World world, BlockPos pos, BlockState state,
            @SuppressWarnings("null") LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (world.isClient()) {
            return;
        }

        ContainerDefinition def = resolveDefinition(state);
        if (def == null || def.stateStorage() == null || def.stateStorage().placedBlock() == null
                || !def.stateStorage().placedBlock().enabled()) {
            return;
        }

        if (!ContainerStateStorage.supportsBlockStorage(def)) {
            return;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof ContainerBlockEntity containerBe)) {
            return;
        }

        boolean syncToClient = def.stateStorage().placedBlock().syncToClient();
        containerBe.setSyncToClient(syncToClient);

        if (ContainerStateStorage.shouldCopyItemToBlock(def)) {
            ContainerPayload payload = ContainerStateStorage.readPayloadFromItem(itemStack, def)
                    .map(p -> ContainerStateStorage.applyDefaults(def, p))
                    .orElse(ContainerStateStorage.defaultPayload(def));

            containerBe.setPayload(payload);
        }
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        List<ItemStack> drops = super.getDroppedStacks(state, builder);

        ContainerDefinition def = resolveDefinition(state);
        if (def == null || def.stateStorage() == null || def.stateStorage().placedBlock() == null) {
            return drops;
        }

        ContainerDefinition.StateStorage.PlacedBlock placed = def.stateStorage().placedBlock();
        if (!placed.enabled()) {
            return drops;
        }

        if (!ContainerStateStorage.supportsBlockStorage(def)) {
            return drops;
        }

        if (!placed.dropsKeepContents()) {
            return drops;
        }

        BlockEntity be = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (!(be instanceof ContainerBlockEntity containerBe)) {
            return drops;
        }

        if (!ContainerStateStorage.shouldCopyBlockToItem(def)) {
            return drops;
        }

        ContainerPayload payload = ContainerStateStorage.applyDefaults(def, containerBe.payload());

        for (ItemStack stack : drops) {
            if (stack != null && stack.isOf(this.asItem())) {
                if (payload != null && !payload.isEmpty()) {
                    ContainerStateStorage.writePayloadToItem(stack, def, payload);
                }
                break;
            }
        }
        return drops;
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
        Identifier definitionId = ModBlocks.containerDefinitionIdForBlock(blockId);
        if (definitionId == null) {
            player.sendMessage(Text.literal("[Brewing] Container block: " + blockId), false);
            return ActionResult.SUCCESS;
        }

        ContainerDefinition def =
                BrewingDataManager.get().containers().get(definitionId.toString());
        if (def == null) {
            player.sendMessage(Text.literal("[Brewing] Container block: " + blockId + " -> "
                    + definitionId + " (definition not loaded?)"), false);
            return ActionResult.SUCCESS;
        }

        player.sendMessage(Text.literal(
                "[Brewing] Container: " + definitionId + " (capacityMb=" + def.liquid().capacityMb()
                        + ", canContain=" + def.liquid().canContainLiquid() + ")"),
                false);

        return ActionResult.SUCCESS;
    }

    private static ContainerDefinition resolveDefinition(BlockState state) {
        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        Identifier definitionId = ModBlocks.containerDefinitionIdForBlock(blockId);
        if (definitionId == null) {
            return null;
        }

        return BrewingDataManager.get().containers().get(definitionId.toString());
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
