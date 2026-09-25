![version](https://img.shields.io/badge/version-1.0.5-blue)
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
