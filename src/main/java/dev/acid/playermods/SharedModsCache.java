package dev.acid.playermods;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SharedModsCache {
    private static final Map<String, ModListPayload> PLAYERS = new ConcurrentHashMap<>();

    private SharedModsCache() {}

    public static void put(ModListPayload payload) {
        PLAYERS.put(payload.playerName().toLowerCase(java.util.Locale.ROOT), payload);
    }

    public static ModListPayload get(String playerName) {
        return PLAYERS.get(playerName.toLowerCase(java.util.Locale.ROOT));
    }

    public static void clear() {
        PLAYERS.clear();
    }
}
