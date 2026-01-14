package dk.mosberg.brewing.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public record ContainerPayload(String contentId, int amountMb, double quality, String temperature,
        double pressure, boolean sealed, long createdTime) {

    public static final String NBT_KEY = "Payload";

    public static ContainerPayload empty() {
        return new ContainerPayload("", 0, 0.0, "", 0.0, false, 0L);
    }

    public boolean isEmpty() {
        return (contentId == null || contentId.isBlank()) && amountMb <= 0;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("content_id", contentId == null ? "" : contentId);
        nbt.putInt("amount_mb", amountMb);
        nbt.putDouble("quality", quality);
        nbt.putString("temperature", temperature == null ? "" : temperature);
        nbt.putDouble("pressure", pressure);
        nbt.putBoolean("sealed", sealed);
        nbt.putLong("created_time", createdTime);
        return nbt;
    }

    public static ContainerPayload fromNbt(NbtCompound nbt) {
        if (nbt == null || nbt.isEmpty()) {
            return empty();
        }

        return new ContainerPayload(nbt.getString("content_id", ""), nbt.getInt("amount_mb", 0),
                nbt.getDouble("quality", 0.0), nbt.getString("temperature", ""),
                nbt.getDouble("pressure", 0.0), nbt.getBoolean("sealed", false),
                nbt.getLong("created_time", 0L));
    }

    public void writeToView(WriteView view) {
        view.putString("content_id", contentId == null ? "" : contentId);
        view.putInt("amount_mb", amountMb);
        view.putDouble("quality", quality);
        view.putString("temperature", temperature == null ? "" : temperature);
        view.putDouble("pressure", pressure);
        view.putBoolean("sealed", sealed);
        view.putLong("created_time", createdTime);
    }

    public static ContainerPayload fromView(ReadView view) {
        if (view == null) {
            return empty();
        }

        return new ContainerPayload(view.getString("content_id", ""), view.getInt("amount_mb", 0),
                view.getDouble("quality", 0.0), view.getString("temperature", ""),
                view.getDouble("pressure", 0.0), view.getBoolean("sealed", false),
                view.getLong("created_time", 0L));
    }
}
