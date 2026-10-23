package dev.acid.playermods;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record ModListPayload(String playerName, List<String> mods) implements CustomPayload {
    public static final Id<ModListPayload> ID = new Id<>(Identifier.of("playermods", "mod_list"));
    public static final PacketCodec<RegistryByteBuf, ModListPayload> CODEC =
            PacketCodec.of(ModListPayload::write, ModListPayload::new);

    public ModListPayload {
        mods = List.copyOf(mods);
    }

    private ModListPayload(RegistryByteBuf buf) {
        this(buf.readString(64), readMods(buf));
    }

    private void write(RegistryByteBuf buf) {
        buf.writeString(playerName, 64);
        int count = Math.min(mods.size(), 512);
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            buf.writeString(mods.get(i), 256);
        }
    }

    private static List<String> readMods(RegistryByteBuf buf) {
        int count = Math.max(0, Math.min(buf.readVarInt(), 512));
        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(buf.readString(256));
        }
        return result;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
