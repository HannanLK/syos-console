package com.syos.adapter.in.cli.menu;

import com.syos.adapter.in.cli.commands.Command;

/**
 * Represents a single menu item with key, label and associated command
 */
public class MenuItem {
    private final String key;
    private final String label;
    private final Command command;
    private final boolean visible;

    public MenuItem(String key, String label, Command command) {
        this(key, label, command, true);
    }

    public MenuItem(String key, String label, Command command, boolean visible) {
        this.key = key;
        this.label = label;
        this.command = command;
        this.visible = visible;
    }

    public String getKey() { 
        return key; 
    }
    
    public String getLabel() { 
        return label; 
    }
    
    public Command getCommand() { 
        return command; 
    }
    
    public boolean isVisible() { 
        return visible; 
    }
    
    public void execute() {
        if (command != null) {
            command.execute();
        }
    }
}