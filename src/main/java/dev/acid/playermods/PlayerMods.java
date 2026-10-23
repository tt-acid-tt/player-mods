package dev.acid.playermods;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerMods implements ModInitializer {
    private static final Map<UUID, ModListPayload> PLAYER_MODS = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(ModListPayload.ID, ModListPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModListPayload.ID, ModListPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ModListPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    UUID senderId = context.player().getUuid();
                    ModListPayload normalized = new ModListPayload(context.player().getGameProfile().getName(), payload.mods());
                    PLAYER_MODS.put(senderId, normalized);
                    for (var player : context.server().getPlayerManager().getPlayerList()) {
                        ServerPlayNetworking.send(player, normalized);
                    }
                }));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> server.execute(() -> {
            for (ModListPayload payload : PLAYER_MODS.values()) {
                ServerPlayNetworking.send(handler.getPlayer(), payload);
            }
        }));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                PLAYER_MODS.remove(handler.getPlayer().getUuid()));
    }
}
