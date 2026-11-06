package com.fiwpr06.websocket.common.util;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public static File resolveSafePath(File rootDir, String relativePath) {
        if (rootDir == null || relativePath == null) return null;

        // Strip leading slash
        String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;

        File target = new File(rootDir, cleanPath);
        try {
            String rootCanonical = rootDir.getCanonicalPath();
            String targetCanonical = target.getCanonicalPath();
            if (!targetCanonical.startsWith(rootCanonical)) {
                // Path traversal attempt
                return null;
            }
            return target;
        } catch (IOException e) {
            return null;
        }
    }

    public static void ensureDirectoryExists(File dir) {
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
    }
}
