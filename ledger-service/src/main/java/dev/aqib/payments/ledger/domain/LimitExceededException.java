package dev.aqib.payments.ledger.domain;

public class LimitExceededException extends RuntimeException {
    public LimitExceededException(String message) { super(message); }
}
