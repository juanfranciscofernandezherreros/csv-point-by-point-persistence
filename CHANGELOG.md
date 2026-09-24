# Changelog

## 1.0.0 - 2026-09-24
- Separa la persistencia de POINT_BY_POINT del micro monolítico.
- Consume `point-by-point.parsed` mediante Avro.
- Conserva Flyway, tablas, API de progreso e idempotencia por `processed_file_event`.
