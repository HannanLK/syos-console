package com.syos.adapter.in.cli.menu;

import com.syos.adapter.in.cli.io.ConsoleIO;

/**
 * Renders menus to the console with minimal formatting (no borders)
 */
public class MenuRenderer {
    private final ConsoleIO console;

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
        if (title != null && !title.isEmpty()) {
            console.println(title);
        }
    }

    private void renderItems(Menu menu) {
        for (MenuItem item : menu.getItems()) {
            if (item.isVisible()) {
                String itemText = String.format("[%s] %s", item.getKey(), item.getLabel());
                console.println(itemText);
            }
        }
    }

    private void renderFooter() {
        console.println();
    }
}