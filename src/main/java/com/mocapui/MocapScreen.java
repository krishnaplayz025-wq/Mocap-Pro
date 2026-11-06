package com.mocapui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class MocapScreen extends Screen {
    private static int page = 0; // 0 = Actors, 1 = Recording, 2 = Settings
    private static final String[] PAGE_NAMES = {"Actors", "Recording / Scenes", "Settings"};

    // Recording page fields
    private static String nick = "", saveName = "", recName = "", newName = "", sceneName = "";
    // Settings page fields
    private static String setName = "", setValue = "";

    private static final String[] BOOLEAN_SETTINGS = {
            "playBlockActions", "setBlockStates", "allowMineskinRequests", "canPushEntities",
            "useCreativeGameMode", "dropFromBlocks", "trackVehicleEntities", "trackItemEntities",
            "trackOtherEntities", "trackPlayedEntities", "playVehicleEntities", "playItemEntities",
            "playOtherEntities", "preventSavingEntities", "recordPlayerDeath", "fluentMovements"
    };

    private final UiConfig cfg = UiConfig.get();

    public MocapScreen() {
        super(Component.literal("Mocap UI"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = 8;

        // Tabs
        int tabW = 110;
        for (int i = 0; i < 3; i++) {
            final int p = i;
            Button b = Button.builder(Component.literal((page == i ? "> " : "") + PAGE_NAMES[i]), x -> {
                page = p;
                rebuildWidgets();
            }).bounds(cx - (tabW * 3 + 8) / 2 + i * (tabW + 4), top, tabW, 20).build();
            addRenderableWidget(b);
        }

        switch (page) {
            case 0 -> initActors(cx, top + 30);
            case 1 -> initRecording(cx, top + 30);
            default -> initSettings(cx, top + 30);
        }

        addRenderableWidget(Button.builder(Component.literal("Close"), x -> onClose())
                .bounds(cx + 100, this.height - 28, 90, 20).build());
    }

    // ---------------- helpers ----------------
    private EditBox box(int x, int y, int w, String hint, String value, int max, Consumer<String> onChange) {
        EditBox e = new EditBox(this.font, x, y, w, 20, Component.literal(hint));
        e.setMaxLength(max);
        e.setHint(Component.literal(hint));
        e.setValue(value == null ? "" : value);
        e.setResponder(onChange);
        addRenderableWidget(e);
        return e;
    }

    private Button btn(int x, int y, int w, String label, Runnable r) {
        Button b = Button.builder(Component.literal(label), bb -> r.run()).bounds(x, y, w, 20).build();
        addRenderableWidget(b);
        return b;
    }

    // ---------------- PAGE 0: ACTORS ----------------
    private void initActors(int cx, int y0) {
        int left = cx - 190;
        for (int i = 0; i < 4; i++) {
            final UiConfig.Slot s = cfg.slots[i];
            final int idx = i;
            int y = y0 + 6 + i * 26;

            Button onBtn = Button.builder(Component.literal(s.enabled ? "ON" : "OFF"), bb -> {
                s.enabled = !s.enabled;
                bb.setMessage(Component.literal(s.enabled ? "ON" : "OFF"));
                cfg.save();
            }).bounds(left, y, 38, 20).build();
            addRenderableWidget(onBtn);

            box(left + 42, y, 92, "Recording", s.recording, 64, v -> { s.recording = v; cfg.save(); });
            box(left + 138, y, 72, "Actor name", s.name, 16, v -> { s.name = v; cfg.save(); });

            Button skin = Button.builder(Component.literal(skinLabel(s.skinType)), bb -> {
                int k = 0;
                for (int j = 0; j < UiConfig.SKIN_TYPES.length; j++) if (UiConfig.SKIN_TYPES[j].equals(s.skinType)) k = j;
                s.skinType = UiConfig.SKIN_TYPES[(k + 1) % UiConfig.SKIN_TYPES.length];
                bb.setMessage(Component.literal(skinLabel(s.skinType)));
                cfg.save();
            }).bounds(left + 214, y, 52, 20).build();
            addRenderableWidget(skin);

            box(left + 270, y, 72, "Skin value", s.skinValue, 128, v -> { s.skinValue = v; cfg.save(); });
            btn(left + 346, y, 34, "Play", () -> playSlot(idx));
        }

        int by = y0 + 6 + 4 * 26 + 6;
        btn(left, by, 120, "Play ALL (ON actors)", this::playAll);
        btn(left + 124, by, 80, "Stop ALL", () -> Cmd.run("mocap playing stop"));
        btn(left + 208, by, 86, "Playing list", () -> Cmd.run("mocap playing list"));
        btn(left + 298, by, 82, "Recordings", () -> Cmd.run("mocap recordings list"));
    }

    private static String skinLabel(String t) {
        return switch (t) {
            case "from_player" -> "Player";
            case "from_file" -> "File";
            case "from_mineskin" -> "Mineskin";
            default -> "Default";
        };
    }

    /** Ek scene banata hai jisme diye gaye slots ek saath hote hain (isse sab actors synced chalte hain). */
    private void buildScene(String scene, UiConfig.Slot... slots) {
        Cmd.run("mocap scenes remove " + scene);
        Cmd.run("mocap scenes add " + scene);
        for (UiConfig.Slot s : slots) {
            String rec = Cmd.clean(s.recording);
            if (rec.isEmpty()) continue;
            String name = Cmd.clean(s.name);
            String type = s.skinType == null ? "default" : s.skinType;
            String val = Cmd.clean(s.skinValue);
            StringBuilder c = new StringBuilder("mocap scenes addTo ").append(scene).append(' ').append(rec);
            boolean customSkin = !type.equals("default") && !val.isEmpty();
            if (!name.isEmpty() || customSkin) {
                c.append(" 0 0 0 0 ").append(name.isEmpty() ? rec : name);
                if (customSkin) c.append(' ').append(type).append(' ').append(val);
            }
            Cmd.run(c.toString());
        }
    }

    private void playSlot(int idx) {
        UiConfig.Slot s = cfg.slots[idx];
        if (Cmd.clean(s.recording).isEmpty()) { Cmd.addLog("Slot " + (idx + 1) + ": recording name likho"); return; }
        String scene = "ui_slot_" + (idx + 1);
        buildScene(scene, s);
        Cmd.run("mocap playing start ." + scene);
    }

    private void playAll() {
        java.util.List<UiConfig.Slot> list = new java.util.ArrayList<>();
        for (UiConfig.Slot s : cfg.slots) if (s.enabled && !Cmd.clean(s.recording).isEmpty()) list.add(s);
        if (list.isEmpty()) { Cmd.addLog("Koi ON actor nahi hai (recording name bhi chahiye)"); return; }
        buildScene("ui_stage", list.toArray(new UiConfig.Slot[0]));
        Cmd.run("mocap playing start .ui_stage");
    }

    // ---------------- PAGE 1: RECORDING / SCENES ----------------
    private void initRecording(int cx, int y0) {
        int left = cx - 190;
        int y = y0 + 4;

        box(left, y, 150, "Player nickname (optional)", nick, 32, v -> nick = v);
        btn(left + 154, y, 110, "Start Recording", () -> {
            String n = Cmd.clean(nick);
            Cmd.run("mocap recording start" + (n.isEmpty() ? "" : " " + n));
        });
        btn(left + 268, y, 112, "Stop Recording", () -> Cmd.run("mocap recording stop"));

        y += 26;
        box(left, y, 150, "Save as (name)", saveName, 64, v -> saveName = v);
        btn(left + 154, y, 110, "Save Recording", () -> {
            String n = Cmd.clean(saveName);
            if (n.isEmpty()) Cmd.addLog("Save name likho"); else Cmd.run("mocap recording save " + n);
        });
        btn(left + 268, y, 112, "Recording state", () -> Cmd.run("mocap recording state"));

        y += 34;
        box(left, y, 120, "Recording name", recName, 64, v -> recName = v);
        box(left + 124, y, 120, "New name", newName, 64, v -> newName = v);
        y += 24;
        btn(left, y, 60, "Info", () -> withRec("mocap recordings info "));
        btn(left + 64, y, 60, "Play", () -> {
            String n = Cmd.clean(recName);
            if (!n.isEmpty()) Cmd.run("mocap playing start " + n);
        });
        btn(left + 128, y, 60, "Remove", () -> withRec("mocap recordings remove "));
        btn(left + 192, y, 60, "Rename", () -> withTwo("mocap recordings rename "));
        btn(left + 256, y, 60, "Copy", () -> withTwo("mocap recordings copy "));
        btn(left + 320, y, 60, "List", () -> Cmd.run("mocap recordings list"));

        y += 34;
        box(left, y, 150, "Scene name", sceneName, 64, v -> sceneName = v);
        y += 24;
        btn(left, y, 60, "Add", () -> withScene("mocap scenes add "));
        btn(left + 64, y, 60, "Info", () -> withScene("mocap scenes info "));
        btn(left + 128, y, 60, "Remove", () -> withScene("mocap scenes remove "));
        btn(left + 192, y, 60, "Play", () -> {
            String n = Cmd.clean(sceneName);
            if (!n.isEmpty()) Cmd.run("mocap playing start ." + n);
        });
        btn(left + 256, y, 60, "Stop", () -> Cmd.run("mocap playing stop"));
        btn(left + 320, y, 60, "List", () -> Cmd.run("mocap scenes list"));
    }

    private void withRec(String prefix) {
        String n = Cmd.clean(recName);
        if (n.isEmpty()) Cmd.addLog("Recording name likho"); else Cmd.run(prefix + n);
    }

    private void withTwo(String prefix) {
        String a = Cmd.clean(recName), b = Cmd.clean(newName);
        if (a.isEmpty() || b.isEmpty()) Cmd.addLog("Recording name aur New name dono likho"); else Cmd.run(prefix + a + " " + b);
    }

    private void withScene(String prefix) {
        String n = Cmd.clean(sceneName);
        if (n.isEmpty()) Cmd.addLog("Scene name likho"); else Cmd.run(prefix + n);
    }

    // ---------------- PAGE 2: SETTINGS ----------------
    private void initSettings(int cx, int y0) {
        int left = cx - 190;
        int colW = 186;
        for (int i = 0; i < BOOLEAN_SETTINGS.length; i++) {
            final String key = BOOLEAN_SETTINGS[i];
            int x = left + (i % 2) * (colW + 8);
            int y = y0 + 4 + (i / 2) * 22;
            Boolean v = cfg.settings.get(key);
            Button b = Button.builder(Component.literal(settingLabel(key, v)), bb -> {
                boolean next = !Boolean.TRUE.equals(cfg.settings.get(key));
                cfg.settings.put(key, next);
                cfg.save();
                Cmd.run("mocap settings set " + key + " " + next);
                bb.setMessage(Component.literal(settingLabel(key, next)));
            }).bounds(x, y, colW, 20).build();
            addRenderableWidget(b);
        }

        int y = y0 + 4 + (BOOLEAN_SETTINGS.length / 2) * 22 + 6;
        box(left, y, 130, "Setting (e.g. playingSpeed)", setName, 48, v -> setName = v);
        box(left + 134, y, 100, "Value", setValue, 32, v -> setValue = v);
        btn(left + 238, y, 70, "Set", () -> {
            String n = Cmd.clean(setName), v = Cmd.clean(setValue);
            if (n.isEmpty() || v.isEmpty()) Cmd.addLog("Setting name aur value likho");
            else Cmd.run("mocap settings set " + n + " " + v);
        });
        btn(left + 312, y, 68, "List all", () -> Cmd.run("mocap settings list"));
    }

    private static String settingLabel(String key, Boolean v) {
        return key + ": " + (v == null ? "?" : (v ? "ON" : "OFF"));
    }

    // ---------------- render ----------------
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);

        int cx = this.width / 2;
        int baseY = this.height - 28 - 4;
        int n = Math.min(5, Cmd.LOG.size());
        for (int i = 0; i < n; i++) {
            String line = Cmd.LOG.get(Cmd.LOG.size() - n + i);
            String shown = this.font.plainSubstrByWidth(line, 330);
            g.drawString(this.font, shown, cx - 190, baseY - (n - i) * 10, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
