package com.promcteam.codex.migration;

import com.promcteam.codex.CodexEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public static void replace(String file, String searchRegex, String replacement) throws IOException {
        File f = new File(file);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                String content = new String(fis.readAllBytes());
                content = content.replaceAll(searchRegex, replacement);

                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(content.getBytes());
                    CodexEngine.get()
                            .getLogger()
                            .info("Replaced instances of '" + searchRegex + "' with '" + replacement + "' in file: "
                                    + file);
                }
            }
        }
    }
}
