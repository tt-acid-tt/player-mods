package dev.acid.playermods;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class PlayerModsClient implements ClientModInitializer {
    private static KeyBinding openMenu;

    @Override
    public void onInitializeClient() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.playermods.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.playermods"
        ));

        ClientPlayNetworking.registerGlobalReceiver(ModListPayload.ID, (payload, context) ->
                context.client().execute(() -> SharedModsCache.put(payload)));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            SharedModsCache.clear();
            List<String> mods = new ArrayList<>();
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                mods.add(mod.getMetadata().getName() + "  ·  " + mod.getMetadata().getVersion().getFriendlyString());
            }
            mods.sort(String.CASE_INSENSITIVE_ORDER);
            String name = client.getSession().getUsername();
            sender.sendPacket(new ModListPayload(name, mods));
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SharedModsCache.clear());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen instanceof PlayerModsScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new PlayerModsScreen());
                }
            }
        });
    }
}
