package dev.aqib.payments.ledger.domain;

import java.util.Objects;

/** A product is nothing but a name plus three behaviours. New product = new combination, no new class. */
public record Product(String code, InterestPolicy interest, FeePolicy fees, LimitPolicy limits) {
    public Product {
        Objects.requireNonNull(code);
        Objects.requireNonNull(interest);
        Objects.requireNonNull(fees);
        Objects.requireNonNull(limits);
    }
}
