package com.syos.adapter.in.cli.io;

/**
 * Console I/O abstraction interface
 * Allows for testability and different implementations
 */
public interface ConsoleIO {
    void print(String message);
    void println(String message);
    void println();

    /**
     * Read a line from input without printing any prompt.
     */
    String readLine();

    /**
     * Read a line from input after printing the given prompt.
     */
    String readLine(String prompt);

    String readPassword();

    /**
     * Printf-style formatted output to console.
     */
    void printf(String format, Object... args);

    void clear();
    void printError(String message);
    void printSuccess(String message);
    void printWarning(String message);
}