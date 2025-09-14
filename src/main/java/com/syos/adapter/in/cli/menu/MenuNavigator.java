package com.syos.adapter.in.cli.menu;

import com.syos.adapter.in.cli.io.ConsoleIO;
import com.syos.adapter.in.cli.session.SessionManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

/**
 * Manages menu navigation and user interaction
 */
public class MenuNavigator {
    private static final Logger logger = LoggerFactory.getLogger(MenuNavigator.class);
    private final Stack<Menu> menuStack;
    @Getter
    private final ConsoleIO console;
    private final MenuRenderer renderer;
    @Getter
    private final SessionManager sessionManager;
    @Getter
    private boolean running;

    public MenuNavigator(ConsoleIO console) {
        this.menuStack = new Stack<>();
        this.console = console;
        this.renderer = new MenuRenderer(console);
        this.sessionManager = SessionManager.getInstance();
        this.running = true;
    }

    public void pushMenu(Menu menu) {
        menuStack.push(menu);
        logger.debug("Pushed menu: {}", menu.getTitle());
    }

    public void popMenu() {
        if (!menuStack.isEmpty()) {
            Menu popped = menuStack.pop();
            logger.debug("Popped menu: {}", popped.getTitle());
        }
    }

    public void clearMenuStack() {
        menuStack.clear();
        logger.debug("Cleared menu stack");
    }

    public void start(Menu initialMenu) {
        pushMenu(initialMenu);
        
        while (running && !menuStack.isEmpty()) {
            try {
                Menu currentMenu = menuStack.peek();
                renderer.render(currentMenu);
                console.print(currentMenu.getPrompt());
                
                String choice = console.readLine().trim();
                
                if (!choice.isEmpty()) {
                    processChoice(currentMenu, choice);
                }
            } catch (Exception e) {
                logger.error("Error in menu navigation", e);
                console.printError("An error occurred: " + e.getMessage());
            }
        }
    }

    private void processChoice(Menu menu, String choice) {
        MenuItem item = menu.findItem(choice);
        
        if (item != null && item.isVisible()) {
            try {
                item.execute();
            } catch (Exception e) {
                logger.error("Error executing menu command", e);
                console.printError("Command failed: " + e.getMessage());
                console.println("Press Enter to continue...");
                console.readLine();
            }
        } else {
            console.printWarning("Invalid choice. Please try again.");
            try {
                Thread.sleep(1000); // Brief pause for user to see the message
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        running = false;
        logger.info("Menu navigator stopped");
    }

}