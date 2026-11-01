# payments-platform

A real-time payments platform, built the way a bank would build it, from the domain model up.

```
ledger-service (Java 25, Spring Boot 4)        double-entry ledger, idempotent transfers, Postgres, outbox
      │  Debezium CDC (no dual writes)
      ▼
Kafka 4 (Avro + schema registry)               ledger events, partitioned by account
      │
      ├── streams-app (Kafka Streams)          spend windows, velocity alerts
      ▼
Iceberg + dbt (DuckDB)                         staging → marts, SCD2 customers, reconciliation vs ledger
      │
Dagster                                        freshness and volume checks → incidents for the dataops-agent
```

Status: being built through November 2026. Each piece links to the write-up of the concept behind it.

## Run it
```
cd ../infra && docker compose up -d     # Postgres, Kafka, schema registry
./gradlew test                          # all tests
./gradlew :ledger-service:bootRun       # http://localhost:8080/health
```

## Design decisions
- Money is never a double: `Money` wraps a `BigDecimal` scaled to the currency.
- Products are compositions of policies (interest, fees, limits), not a class hierarchy. See `domain/` and the `legacy/` package for why.
