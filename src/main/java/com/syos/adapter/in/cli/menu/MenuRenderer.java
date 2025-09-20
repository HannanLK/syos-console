package com.syos.adapter.in.cli.menu;

import com.syos.adapter.in.cli.io.ConsoleIO;

/**
 * Enhanced Menu Renderer with consistent design patterns
 * Implements consistent navigation headers across the application
 */
public class MenuRenderer {
    private final ConsoleIO console;
    private static final String HEADER_LINE = "═══════════════════════════════════════";
    private static final int HEADER_WIDTH = 39;

    public MenuRenderer(ConsoleIO console) {
        this.console = console;
    }

    public void render(Menu menu) {
        renderHeader(menu.getTitle());
        renderItems(menu);
        renderFooter();
    }

    /**
     * Renders consistent navigation header with the requested design:
     * ═══════════════════════════════════════
     *               [Menu Title]
     * ═══════════════════════════════════════
     */
    private void renderHeader(String title) {
        console.println();
        if (title != null && !title.isEmpty()) {
            console.println(HEADER_LINE);
            
            // Center the title within the header width
            String centeredTitle = centerText(title, HEADER_WIDTH);
            console.println(centeredTitle);
            
            console.println(HEADER_LINE);
            console.println();
        }
    }

    /**
     * Centers text within the specified width
     * @param text Text to center
     * @param width Total width for centering
     * @return Centered text with appropriate padding
     */
    private String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        
        int padding = (width - text.length()) / 2;
        StringBuilder centered = new StringBuilder();
        
        // Add left padding
        for (int i = 0; i < padding; i++) {
            centered.append(" ");
        }
        
        centered.append(text);
        
        // Add right padding to reach total width
        while (centered.length() < width) {
            centered.append(" ");
        }
        
        return centered.toString();
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
    
    /**
     * Renders a simple section header for sub-menus
     */
    public void renderSectionHeader(String sectionTitle) {
        console.println();
        console.println("─── " + sectionTitle + " ───");
        console.println();
    }
    
    /**
     * Renders user profile information with consistent styling
     */
    public void renderUserProfile(String username, String role, String memberSince) {
        console.println(HEADER_LINE);
        console.println(centerText("User Profile", HEADER_WIDTH));
        console.println(HEADER_LINE);
        console.println();
        console.println("Username: " + username);
        console.println("Role: " + role);
        if (memberSince != null) {
            console.println("Member Since: " + memberSince);
        }
        console.println();
    }
}