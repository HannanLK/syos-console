package com.syos.adapter.in.cli.menu;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a menu with title and items
 */
public class Menu {
    @Getter
    private final String title;
    private final List<MenuItem> items;
    @Getter
    private final String prompt;

    private Menu(Builder builder) {
        this.title = builder.title;
        this.items = new ArrayList<>(builder.items);
        this.prompt = builder.prompt != null ? builder.prompt : "Enter your choice: ";
    }

    public List<MenuItem> getItems() {
        return new ArrayList<>(items); 
    }

    public MenuItem findItem(String key) {
        return items.stream()
            .filter(item -> item.getKey().equalsIgnoreCase(key))
            .findFirst()
            .orElse(null);
    }

    /**
     * Builder pattern for Menu construction
     */
    public static class Builder {
        private String title;
        private List<MenuItem> items = new ArrayList<>();
        private String prompt;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder addItem(MenuItem item) {
            this.items.add(item);
            return this;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Menu build() {
            return new Menu(this);
        }
    }
}