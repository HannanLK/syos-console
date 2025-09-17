package com.syos.adapter.in.cli.io;

import java.io.Console;
import java.util.Scanner;

/**
 * Standard console I/O implementation
 */
public class StandardConsoleIO implements ConsoleIO {
    private final Scanner scanner;
    private final Console console;

    public StandardConsoleIO() {
        this.scanner = new Scanner(System.in);
        this.console = System.console();
    }

    @Override
    public void print(String message) {
        System.out.print(message);
    }

    @Override
    public void println(String message) {
        System.out.println(message);
    }

    @Override
    public void println() {
        System.out.println();
    }

    @Override
    public String readLine() {
        return scanner.nextLine();
    }

    @Override
    public String readLine(String prompt) {
        if (prompt != null && !prompt.isEmpty()) {
            System.out.print(prompt);
        }
        return scanner.nextLine();
    }

    @Override
    public String readPassword() {
        if (console != null) {
            char[] passwordChars = console.readPassword();
            return new String(passwordChars);
        } else {
            // Fallback for IDEs that don't support Console
            System.out.print("(Warning: Password will be visible) ");
            return scanner.nextLine();
        }
    }

    @Override
    public void printf(String format, Object... args) {
        System.out.printf(format, args);
    }

    @Override
    public void clear() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback - print empty lines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    @Override
    public void printError(String message) {
        System.err.println("ERROR: " + message);
    }

    @Override
    public void printSuccess(String message) {
        System.out.println("SUCCESS: " + message);
    }

    @Override
    public void printWarning(String message) {
        System.out.println("⚠️ WARNING: " + message);
    }
}