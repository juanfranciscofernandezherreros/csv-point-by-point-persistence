# Changelog

## 1.0.2 - 2026-09-25

- [patch] Exige confirmar rama y nivel SemVer antes de cualquier cambio.
- [patch] Alinea Maven CI-friendly con revision, sha1 y changelist.

## 1.0.2 - 2026-09-25

- [patch] Exige confirmar rama y nivel SemVer antes de cualquier cambio.

## 1.0.1 - 2026-09-24
- Añade auto-merge tras pasar los checks del PR y elimina la rama origen tras fusionar.

## 1.0.0 - 2026-09-24
- Separa la persistencia de POINT_BY_POINT del micro monolítico.
- Consume `point-by-point.parsed` mediante Avro.
- Conserva Flyway, tablas, API de progreso e idempotencia por `processed_file_event`.
