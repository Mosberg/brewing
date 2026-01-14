package dk.mosberg.brewing.block.entity;

import dk.mosberg.brewing.data.ContainerDefinition;
import dk.mosberg.brewing.registry.ModBlockEntities;
import dk.mosberg.brewing.state.ContainerPayload;
import dk.mosberg.brewing.state.ContainerStateStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ContainerBlockEntity extends BlockEntity {
    private ContainerPayload payload = ContainerPayload.empty();
    private boolean syncToClient = false;

    public ContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTAINER, pos, state);
    }

    public ContainerPayload payload() {
        return payload;
    }

    public void setSyncToClient(boolean syncToClient) {
        this.syncToClient = syncToClient;
    }

    public void setPayload(ContainerPayload payload, boolean markForSync) {
        this.payload = payload == null ? ContainerPayload.empty() : payload;
        this.markDirty();
        if (markForSync) {
            this.markForUpdate();
        }
    }

    public void setPayload(ContainerPayload payload) {
        setPayload(payload, syncToClient);
    }

    public void applyPayloadFromItem(ItemStack stack, ContainerDefinition def,
            boolean markForSync) {
        ContainerStateStorage.readPayloadFromItem(stack, def)
                .ifPresent(p -> setPayload(p, markForSync));
    }

    public void writePayloadToItem(ItemStack stack, ContainerDefinition def) {
        if (payload == null || payload.isEmpty()) {
            return;
        }

        ContainerStateStorage.writePayloadToItem(stack, def, payload);
    }

    private void markForUpdate() {
        World world = this.getWorld();
        if (!syncToClient || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        serverWorld.getChunkManager().markForUpdate(this.pos);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createNbt(registries);
    }

    @Override
    protected void writeData(WriteView view) {
        if (payload == null || payload.isEmpty()) {
            view.remove(ContainerPayload.NBT_KEY);
        } else {
            payload.writeToView(view.get(ContainerPayload.NBT_KEY));
        }

        view.putBoolean("SyncToClient", syncToClient);
    }

    @Override
    protected void readData(ReadView view) {
        payload = view.getOptionalReadView(ContainerPayload.NBT_KEY).map(ContainerPayload::fromView)
                .orElse(ContainerPayload.empty());

        syncToClient = view.getBoolean("SyncToClient", false);
    }
}
