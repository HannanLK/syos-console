package com.syos.application.ports.out;

import com.syos.domain.entities.Batch;

public interface BatchRepository {
    Batch save(Batch batch);
}
