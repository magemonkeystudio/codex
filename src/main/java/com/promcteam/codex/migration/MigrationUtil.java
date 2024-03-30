package com.promcteam.codex.migration;

import com.promcteam.codex.CodexEngine;

import java.io.File;

public class MigrationUtil {
    public static void renameDirectory(String oldPath, String newPath) {
        File oldDir = new File(oldPath);
        File newDir = new File(newPath);
        if (oldDir.exists()) {
            boolean renamed = oldDir.renameTo(newDir);
            if (!renamed) {
                CodexEngine.get().getLogger().warning("Failed to rename directory: " + oldPath + " -> " + newPath);
            } else {
                CodexEngine.get().getLogger().info("Renamed directory: " + oldPath + " -> " + newPath);
            }
        }
    }
}
