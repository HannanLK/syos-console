package com.syos.adapter.in.cli.io;

/**
 * Console I/O abstraction interface
 * Allows for testability and different implementations
 */
public interface ConsoleIO {
    void print(String message);
    void println(String message);
    void println();
    String readLine();
    String readPassword();
    void clear();
    void printError(String message);
    void printSuccess(String message);
    void printWarning(String message);
}