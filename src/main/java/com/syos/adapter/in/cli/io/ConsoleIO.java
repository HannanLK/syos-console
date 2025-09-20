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
     * Alias for println - prints a line with message
     */
    default void printLine(String message) {
        println(message);
    }

    /**
     * Read a line from input without printing any prompt.
     */
    String readLine();

    /**
     * Read a line from input after printing the given prompt.
     */
    String readLine(String prompt);

    /**
     * Backward-compatible alias used by some controllers.
     */
    default String readInput(String prompt) {
        return readLine(prompt);
    }

    String readPassword();

    /**
     * Backward-compatible overload used by some tests/controllers to show a prompt before password input.
     */
    default String readPassword(String prompt) {
        if (prompt != null && !prompt.isEmpty()) {
            print(prompt);
        }
        return readPassword();
    }

    /**
     * Printf-style formatted output to console.
     */
    void printf(String format, Object... args);

    void clear();
    void printError(String message);
    void printSuccess(String message);
    void printWarning(String message);
    default void printInfo(String message) { println(message); }
    
    // Additional input methods needed by controllers
    default String readString(String prompt) {
        return readLine(prompt);
    }
    
    default Integer readInt(String prompt) {
        try {
            String input = readLine(prompt);
            return input != null && !input.trim().isEmpty() ? Integer.parseInt(input.trim()) : null;
        } catch (NumberFormatException e) {
            printError("Invalid number format. Please enter a valid integer.");
            return readInt(prompt);
        }
    }
    
    default boolean readBoolean(String prompt) {
        String input = readLine(prompt);
        return input != null && (input.trim().toLowerCase().startsWith("y") || input.trim().toLowerCase().equals("true"));
    }
}