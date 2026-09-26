# Changelog

## 1.1.0 - 2026-09-26

- [minor] KAN-111 aplica la estrategia común de errores Kafka de KAN-18.
- [minor] Clasifica errores permanentes de protocolo/datos como non-retryable y fallos transitorios de PostgreSQL como retryable.
- [minor] Configura retries/backoff y DLT `point-by-point.parsed.DLT`.
- [minor] Añade tests de error permanente y transitorio.


## 1.0.5 - 2026-09-25

- [patch] KAN-85 sustituye los schemas locales PointByPoint por `basketball-event-contracts:1.0.2`.
- [patch] Elimina la generación Avro local y configura Maven/CI con lectura autenticada de GitHub Packages.
- [patch] Mantiene sin cambios el protocolo START/ROW/COMPLETED/FAILED y la persistencia existente.

## 1.0.4 - 2026-09-25

- [patch] Refuerza AGENTS.md: lectura obligatoria por tarea, flujo autónomo y prohibición absoluta de escrituras directas en main.

## 1.0.3

- [patch] Estandariza la automatización del repositorio con el flujo autónomo de csv-results-parser.

## 1.0.2 - 2026-09-25

- [patch] Exige confirmar rama y nivel SemVer antes de cualquier cambio.
- [patch] Alinea Maven CI-friendly con revision, sha1 y changelist.

## 1.0.1 - 2026-09-24
- Añade auto-merge tras pasar los checks del PR y elimina la rama origen tras fusionar.

## 1.0.0 - 2026-09-24
- Separa la persistencia de POINT_BY_POINT del micro monolítico.
- Consume `point-by-point.parsed` mediante Avro.
- Conserva Flyway, tablas, API de progreso e idempotencia por `processed_file_event`.
