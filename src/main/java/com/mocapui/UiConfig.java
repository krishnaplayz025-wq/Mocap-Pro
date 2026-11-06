package com.mocapui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class UiConfig {
    public static final String[] SKIN_TYPES = {"default", "from_player", "from_file", "from_mineskin"};

    public static class Slot {
        public boolean enabled = false;
        public String recording = "";
        public String name = "";
        public String skinType = "default";
        public String skinValue = "";
    }

    public Slot[] slots = {new Slot(), new Slot(), new Slot(), new Slot()};
    public Map<String, Boolean> settings = new HashMap<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static UiConfig instance;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("mocapui.json");
    }

    public static UiConfig get() {
        if (instance == null) {
            try {
                if (Files.exists(path())) instance = GSON.fromJson(Files.readString(path()), UiConfig.class);
            } catch (Exception ignored) {}
            if (instance == null) instance = new UiConfig();
            if (instance.slots == null || instance.slots.length != 4) instance.slots = new UiConfig().slots;
            if (instance.settings == null) instance.settings = new HashMap<>();
        }
        return instance;
    }

    public void save() {
        try { Files.writeString(path(), GSON.toJson(this)); } catch (Exception ignored) {}
    }
}
