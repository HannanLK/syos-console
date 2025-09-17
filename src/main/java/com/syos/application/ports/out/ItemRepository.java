package com.syos.application.ports.out;

import com.syos.domain.entities.Item;
import com.syos.domain.valueobjects.ItemCode;

import java.util.Optional;

public interface ItemRepository {
    boolean existsByCode(String code);
    Optional<Item> findByCode(String code);
    Item save(Item item);
}
