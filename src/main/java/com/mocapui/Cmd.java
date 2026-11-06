package com.mocapui;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/** Sab buttons yahin se asli /mocap commands chalate hain (isliye original mod ke sare features same rehte hain). */
public final class Cmd {
    public static final List<String> LOG = new ArrayList<>();

    private Cmd() {}

    public static void run(String command) {
        var conn = Minecraft.getInstance().getConnection();
        if (conn == null) return;
        conn.sendCommand(command); // slash ke bina
    }

    public static void addLog(String line) {
        for (String s : line.split("\n")) {
            if (!s.isBlank()) LOG.add(s);
        }
        while (LOG.size() > 40) LOG.remove(0);
    }

    /** Spaces/slash hata deta hai taaki command toot na jaye. */
    public static String clean(String s) {
        return s == null ? "" : s.trim().replaceAll("[\\s/\\\\\"']", "");
    }
}
