package dev.acid.playermods;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PlayerModsScreen extends Screen {
    private enum Page { PLAYERS, LOCAL_MODS }
    private Page page = Page.PLAYERS;
    private int scroll;

    public PlayerModsScreen() {
        super(Text.literal("Player Mods"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim the world without using Minecraft's default screen panel.
        context.fill(0, 0, width, height, 0x99070A10);

        int panelW = Math.min(720, width - 36);
        int panelH = Math.min(460, height - 36);
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;

        rounded(context, left, top, panelW, panelH, 0xF0151922, 16);
        rounded(context, left + 1, top + 1, panelW - 2, 54, 0xFF1D2330, 15);
        context.fill(left + 1, top + 53, left + panelW - 1, top + 55, 0xFF1D2330);

        text(context, "PLAYER MODS", left + 24, top + 17, 0xFFF4F7FF, true);
        text(context, "FABRIC  /  1.21.4", left + 24, top + 34, 0xFF929DB2, false);
        text(context, "RIGHT SHIFT  ·  CLOSE", left + panelW - 170, top + 25, 0xFF9DAAC0, false);

        int navY = top + 72;
        tab(context, left + 20, navY, 128, "Players", page == Page.PLAYERS);
        tab(context, left + 156, navY, 128, "Local mods", page == Page.LOCAL_MODS);

        int bodyTop = navY + 48;
        text(context, page == Page.PLAYERS ? "PLAYERS IN THIS WORLD" : "MODS INSTALLED ON THIS CLIENT",
                left + 24, bodyTop, 0xFFF0F3FA, true);

        List<String> rows = page == Page.PLAYERS ? getPlayers() : getLocalMods();
        String subtitle = page == Page.PLAYERS
                ? "Player names are visible; their private mod folders are not."
                : "Only mods installed on this computer are listed here.";
        text(context, subtitle, left + 24, bodyTop + 17, 0xFF9AA5B8, false);

        int listTop = bodyTop + 42;
        int listBottom = top + panelH - 24;
        int rowH = 43;
        int visible = Math.max(1, (listBottom - listTop) / rowH);
        int maxScroll = Math.max(0, rows.size() - visible);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        if (rows.isEmpty()) {
            rounded(context, left + 20, listTop, panelW - 40, 58, 0xFF202735, 10);
            text(context, page == Page.PLAYERS ? "No player list available yet." : "No installed mods were found.",
                    left + 34, listTop + 23, 0xFFB7C1D2, false);
        } else {
            int count = Math.min(visible, rows.size() - scroll);
            for (int i = 0; i < count; i++) {
                int y = listTop + i * rowH;
                rounded(context, left + 20, y, panelW - 40, rowH - 5, 0xFF202735, 9);
                rounded(context, left + 31, y + 10, 22, 22, page == Page.PLAYERS ? 0xFF254C62 : 0xFF3A345C, 7);
                text(context, page == Page.PLAYERS ? "P" : "M", left + 38, y + 17, 0xFFFFFFFF, true);
                text(context, rows.get(scroll + i), left + 65, y + 15, 0xFFE9EDF6, false);
                if (page == Page.PLAYERS) {
                    text(context, "MOD LIST NOT SHARED", left + panelW - 184, y + 15, 0xFF929DB2, false);
                }
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private List<String> getPlayers() {
        MinecraftClient client = MinecraftClient.getInstance();
        List<String> result = new ArrayList<>();
        if (client.getNetworkHandler() != null) {
            for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
                result.add(entry.getProfile().getName());
            }
        } else if (client.player != null) {
            result.add(client.player.getGameProfile().getName());
        }
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    private List<String> getLocalMods() {
        List<String> result = new ArrayList<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String name = mod.getMetadata().getName();
            String version = mod.getMetadata().getVersion().getFriendlyString();
            result.add(name + "  ·  " + version);
        }
        result.sort(Comparator.comparing(String::toLowerCase));
        return result;
    }

    private void tab(DrawContext context, int x, int y, int w, String label, boolean active) {
        rounded(context, x, y, w, 32, active ? 0xFF35465D : 0xFF202632, 9);
        text(context, label, x + 14, y + 12, active ? 0xFFFFFFFF : 0xFFABB5C7, active);
    }

    private void text(DrawContext context, String value, int x, int y, int color, boolean bold) {
        if (bold) {
            context.drawTextWithShadow(textRenderer, Text.literal(value), x, y, color);
        } else {
            context.drawText(textRenderer, Text.literal(value), x, y, color, false);
        }
    }

    // Rounded panel composed of horizontal spans to keep the interface independent
    // of vanilla GUI textures. This is a first-pass renderer; corners can be refined.
    private void rounded(DrawContext context, int x, int y, int w, int h, int color, int radius) {
        int r = Math.max(1, Math.min(radius, Math.min(w / 2, h / 2)));
        for (int row = 0; row < h; row++) {
            int inset = 0;
            if (row < r) {
                double dy = r - row - 0.5;
                inset = (int) Math.ceil(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            } else if (row >= h - r) {
                double dy = row - (h - r) + 0.5;
                inset = (int) Math.ceil(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            }
            context.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int panelW = Math.min(720, width - 36);
            int left = (width - panelW) / 2;
            int top = (height - Math.min(460, height - 36)) / 2;
            int navY = top + 72;
            if (mouseY >= navY && mouseY <= navY + 32) {
                if (mouseX >= left + 20 && mouseX <= left + 148) {
                    page = Page.PLAYERS;
                    scroll = 0;
                    return true;
                }
                if (mouseX >= left + 156 && mouseX <= left + 284) {
                    page = Page.LOCAL_MODS;
                    scroll = 0;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = Math.max(0, scroll - (int) Math.signum(verticalAmount) * 3);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || keyCode == 344) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
