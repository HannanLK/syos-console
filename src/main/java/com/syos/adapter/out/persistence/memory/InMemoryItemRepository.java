package com.syos.adapter.out.persistence.memory;

import com.syos.application.ports.out.ItemRepository;
import com.syos.domain.entities.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryItemRepository implements ItemRepository {
    private final AtomicLong seq = new AtomicLong(1);
    private final Map<Long, Item> byId = new HashMap<>();
    private final Map<String, Long> idByCode = new HashMap<>();

    @Override
    public boolean existsByCode(String code) {
        return idByCode.containsKey(code);
    }

    @Override
    public Optional<Item> findByCode(String code) {
        Long id = idByCode.get(code);
        if (id == null) return Optional.empty();
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Item save(Item item) {
        Long id = item.getId();
        if (id == null) {
            id = seq.getAndIncrement();
            Item withId = item.withId(id);
            byId.put(id, withId);
            idByCode.put(withId.getCode().getValue(), id);
            return withId;
        } else {
            byId.put(id, item);
            idByCode.put(item.getCode().getValue(), id);
            return item;
        }
    }
}
