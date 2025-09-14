package com.syos.adapter.in.cli.menu;

import com.syos.adapter.in.cli.io.ConsoleIO;

/**
 * Renders menus to the console with proper formatting
 */
public class MenuRenderer {
    private final ConsoleIO console;
    private static final String SEPARATOR = "═";
    private static final int WIDTH = 60;

    public MenuRenderer(ConsoleIO console) {
        this.console = console;
    }

    public void render(Menu menu) {
        renderHeader(menu.getTitle());
        renderItems(menu);
        renderFooter();
    }

    private void renderHeader(String title) {
        console.println();
        console.println("╔" + SEPARATOR.repeat(WIDTH - 2) + "╗");
        
        if (title != null && !title.isEmpty()) {
            String paddedTitle = centerText(title, WIDTH - 2);
            console.println("║" + paddedTitle + "║");
            console.println("╠" + SEPARATOR.repeat(WIDTH - 2) + "╣");
        }
    }

    private void renderItems(Menu menu) {
        for (MenuItem item : menu.getItems()) {
            if (item.isVisible()) {
                String itemText = String.format("  [%s] %s", item.getKey(), item.getLabel());
                String paddedItem = padRight(itemText, WIDTH - 2);
                console.println("║" + paddedItem + "║");
            }
        }
    }

    private void renderFooter() {
        console.println("╚" + SEPARATOR.repeat(WIDTH - 2) + "╝");
        console.println();
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }

    private String padRight(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }
}