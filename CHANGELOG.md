# Changelog

## 1.3.0 - 2026-09-26

- [minor] KAN-128 activa consumo Kafka batch con un máximo configurable de 500 registros por poll.
- [minor] Agrupa ROW consecutivas por `sourceEventId` y las persiste con `JdbcTemplate.batchUpdate`.
- [minor] Activa `reWriteBatchedInserts` para PostgreSQL y actualiza el progreso una vez por grupo.
- [minor] Mantiene START/COMPLETED/FAILED como barreras transaccionales e idempotentes.
- [minor] Añade tests de rollback del batch y una medición de throughput secuencial vs JDBC batch sobre 1.000 filas.


## 1.2.0 - 2026-09-26

- [minor] KAN-59 hace START reanudable y ROW idempotente mediante upsert atómico por import.
- [minor] KAN-60 añade `source_event_id` a la identidad de `point_by_point_event` y verifica COMPLETED solo contra filas del import actual.
- [minor] Conserva imports históricos mediante backfill `legacy:<match_id>` durante la migración V5.
- [minor] Añade tests de recuperación parcial, ROW/COMPLETED redelivered, imports múltiples del mismo match y entrega dispersa.

## 1.1.2 - 2026-09-26

- [patch] KAN-111 añade cobertura del flujo real de recuperación de errores de deserialización hacia DLT.
- [patch] Verifica que `ErrorHandlingDeserializer` conserva los bytes originales y que el recoverer los publica en `point-by-point.parsed.DLT`.
- [patch] Verifica que la publicación DLT no fuerza la partición del topic origen.


## 1.1.1 - 2026-09-26

- [patch] KAN-111 captura errores de deserialización Avro mediante `ErrorHandlingDeserializer`.
- [patch] Permite publicar en DLT tanto objetos Avro como bytes crudos usando `DelegatingByTypeSerializer`.
- [patch] Deja que Kafka elija una partición DLT válida en lugar de forzar la partición del topic origen.
- [patch] Clasifica los errores por campos nulos obligatorios como non-retryable y amplía sus tests.


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
