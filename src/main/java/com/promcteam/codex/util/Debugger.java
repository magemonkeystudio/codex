package com.promcteam.codex.util;

public class Debugger {

    private static boolean isDebug = false;

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static void log(String message) {
        if (!isDebug) return;

        System.out.println("[RiseDebugger] INFO - " + message);
    }

    public static void warn(String message) {
        if (!isDebug) return;

        System.out.println("[RiseDebugger] WARNING - " + message);
    }

    public static void err(String message) {
        if (!isDebug) return;

        System.out.println("[RiseDebugger] ERROR - " + message);
    }

}
