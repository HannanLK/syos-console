package com.syos.domain.specifications;

import com.syos.domain.aggregates.UserAggregate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AndAggregatesSmokeTest {

    @Test
    void userAuthenticatedSpecification_existsAndInstantiates() {
        UserAuthenticatedSpecification spec = new UserAuthenticatedSpecification();
        assertNotNull(spec);
        assertEquals(UserAuthenticatedSpecification.class, spec.getClass());
    }

    @Test
    void userAggregate_existsAndInstantiates() {
        UserAggregate agg = new UserAggregate();
        assertNotNull(agg);
        assertEquals(UserAggregate.class, agg.getClass());
    }
}
