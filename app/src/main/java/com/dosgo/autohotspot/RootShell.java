package com.dosgo.autohotspot;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

final class RootShell {
    private static final String TAG = "RootShell";
    private static Boolean rootAvailable;

    private RootShell() {
    }

    static synchronized boolean isAvailable() {
        if (rootAvailable == null) {
            CommandResult result = execute("id");
            rootAvailable = result.exitCode == 0 && result.output.contains("uid=0");
        }
        return rootAvailable;
    }

    static CommandResult execute(String command) {
        Process process = null;
        try {
            process = new ProcessBuilder("su", "-c", command)
                    .redirectErrorStream(true)
                    .start();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new CommandResult(-1, "timeout");
            }
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }
            return new CommandResult(process.exitValue(), output.toString());
        } catch (Exception e) {
            Log.d(TAG, "Root 命令不可用: " + e.getMessage());
            if (process != null) {
                process.destroy();
            }
            return new CommandResult(-1, e.toString());
        }
    }

    static final class CommandResult {
        final int exitCode;
        final String output;

        CommandResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
