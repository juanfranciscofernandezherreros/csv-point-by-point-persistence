![version](https://img.shields.io/badge/version-1.1.2-blue)
# csv-point-by-point-persistence

Persistencia separada de `csv-point-by-point`.

```text
point-by-point.parsed -> ParsedPointByPointConsumer -> JPA/Flyway -> PostgreSQL
```

Consume el protocolo Avro `START / ROW / COMPLETED / FAILED`.

## Contratos Avro compartidos

`PointByPointKey` y `PointByPointValue` se consumen desde:

```text
com.fernandez.basketball:basketball-event-contracts:1.0.2
```

Este repositorio ya no mantiene copias locales de esos schemas ni genera clases Avro durante su propia build. Fuera de GitHub Actions, Maven necesita credenciales con `read:packages` para resolver el artefacto desde GitHub Packages.

Las filas se identifican por `(match_id, quarter, sequence)` y se guardan de forma idempotente. `processed_file_event` marca un fichero terminado y evita reprocesarlo.

Se conserva la API `GET /imports/{eventId}` y la tabla `point_by_point_import`. En la arquitectura dividida, `rowsPersisted` avanza a medida que Kafka entrega filas; `COMPLETED` solo se acepta tras verificar el total esperado y el recuento de `point_by_point_event`.

Variables: `DB_URL`, `DB_USER`, `DB_PASS`, `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SCHEMA_REGISTRY_URL`, `KAFKA_PARSED_POINT_BY_POINT_TOPIC`.


## Estrategia de errores Kafka

KAN-111 aplica la política de KAN-18 al consumo de `point-by-point.parsed`.

- errores permanentes del protocolo START/ROW/COMPLETED/FAILED y de integridad: non-retryable;
- fallos transitorios de PostgreSQL: retryable;
- mensajes agotados: `point-by-point.parsed.DLT`;
- `KAFKA_RETRY_MAX_ATTEMPTS`: intentos totales, default `3`;
- `KAFKA_RETRY_BACKOFF_MS`: backoff fijo, default `1000`;
- `KAFKA_POINT_BY_POINT_PERSISTENCE_DLT_TOPIC`: topic DLT configurable.

La DLT conserva el registro original y los headers de diagnóstico generados por Spring Kafka. Los deserializadores Avro están envueltos con `ErrorHandlingDeserializer`, de forma que un payload corrupto o incompatible también llega al flujo de recuperación. La publicación DLT acepta tanto objetos Avro como `byte[]` crudos y deja que Kafka seleccione una partición válida. El flujo de deserialización fallida → DLT está cubierto por un test dedicado que verifica la conservación de los bytes originales.
