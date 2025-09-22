package com.syos.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Category Entity")
class CategoryTest {

    @Test
    @DisplayName("create root and sub categories with status transitions")
    void categories() {
        Category root = Category.createRootCategory("BEV", "Beverages", "Drinks", 1);
        assertThat(root.isRootCategory()).isTrue();
        assertThat(root.isActive()).isTrue();

        Category sub = Category.createSubCategory(1L, "SOFT", "Soft Drinks", "Sodas", 1);
        assertThat(sub.isSubCategory()).isTrue();
        Category deactivated = sub.deactivate();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.activate().isActive()).isTrue();
    }

    @Test
    @DisplayName("validation of codes and names")
    void validation() {
        assertThatThrownBy(() -> Category.createRootCategory(" ", "Name", null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Category.createRootCategory("CODE", " ", null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Category.createSubCategory(null, "CODE", "Name", null, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
