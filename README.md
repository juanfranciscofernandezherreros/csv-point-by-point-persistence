![version](https://img.shields.io/badge/version-1.2.0-blue)
# csv-point-by-point-persistence

Persistencia separada de `csv-point-by-point`.

```text
point-by-point.parsed -> ParsedPointByPointConsumer -> PostgreSQL
```

Consume el protocolo Avro `START / ROW / COMPLETED / FAILED`.

## Política de idempotencia y recuperación

KAN-59 y KAN-60 aplican la estrategia común de KAN-19 al protocolo POINT-BY-POINT.

Cada import se identifica por `sourceEventId`. Cada fila se identifica dentro de ese import por:

```text
(source_event_id, match_id, quarter, sequence)
```

PostgreSQL respalda esa identidad con la primary key de `point_by_point_event`. La escritura de ROW usa `INSERT ... ON CONFLICT (...) DO UPDATE`, por lo que redeliveries del mismo ROW no generan duplicados ni carreras read-then-write.

La recuperación queda definida así:

- `START` nuevo crea el import en `IMPORTING`;
- `START` repetido para un import no finalizado **reanuda** el mismo import y conserva `rowsRead` / `rowsPersisted`;
- `ROW` repetida hace upsert dentro del mismo `sourceEventId`;
- tras cada ROW, `rowsPersisted` se calcula a partir de las filas realmente almacenadas para ese import;
- `COMPLETED` verifica únicamente `count(sourceEventId)`, nunca todas las filas históricas del mismo `matchId`;
- un `sourceEventId` ya completado queda registrado en `processed_file_event`, por lo que redeliveries posteriores de START/ROW/COMPLETED son no-op;
- dos imports diferentes del mismo partido conservan filas separadas y se verifican de forma independiente.

La migración V5 asigna a filas históricas anteriores un identificador `legacy:<match_id>` antes de convertir `source_event_id` en NOT NULL, preservando los datos existentes.

## Contratos Avro compartidos

`PointByPointKey` y `PointByPointValue` se consumen desde:

```text
com.fernandez.basketball:basketball-event-contracts:1.0.2
```

Este repositorio ya no mantiene copias locales de esos schemas ni genera clases Avro durante su propia build. Fuera de GitHub Actions, Maven necesita credenciales con `read:packages` para resolver el artefacto desde GitHub Packages.

Se conserva la API `GET /imports/{eventId}` y la tabla `point_by_point_import`.

Variables: `DB_URL`, `DB_USER`, `DB_PASS`, `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SCHEMA_REGISTRY_URL`, `KAFKA_PARSED_POINT_BY_POINT_TOPIC`.

Tests rápidos: `mvn -B test`. Integración PostgreSQL: `mvn -B verify -Pintegration`.

## Estrategia de errores Kafka

KAN-111 aplica la política de KAN-18 al consumo de `point-by-point.parsed`.

- errores permanentes del protocolo START/ROW/COMPLETED/FAILED y de integridad: non-retryable;
- fallos transitorios de PostgreSQL: retryable;
- mensajes agotados: `point-by-point.parsed.DLT`;
- `KAFKA_RETRY_MAX_ATTEMPTS`: intentos totales, default `3`;
- `KAFKA_RETRY_BACKOFF_MS`: backoff fijo, default `1000`;
- `KAFKA_POINT_BY_POINT_PERSISTENCE_DLT_TOPIC`: topic DLT configurable.

La DLT conserva el registro original y los headers de diagnóstico generados por Spring Kafka. Los deserializadores Avro están envueltos con `ErrorHandlingDeserializer`, de forma que un payload corrupto o incompatible también llega al flujo de recuperación. La publicación DLT acepta tanto objetos Avro como `byte[]` crudos y deja que Kafka seleccione una partición válida.
