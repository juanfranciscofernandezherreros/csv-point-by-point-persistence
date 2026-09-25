# AGENTS.md

Estas reglas son obligatorias para cualquier agente, asistente o automatización que modifique este repositorio.

## Confirmación obligatoria antes de empezar

Antes de realizar cualquier cambio, el agente debe preguntar al usuario y esperar respuesta explícita sobre:

1. **Nombre de la rama**, proponiendo uno por defecto.
2. **Tipo SemVer** del cambio: `major`, `minor` o `patch`.

No se debe modificar ningún archivo, crear commits ni abrir una Pull Request hasta disponer de ambas respuestas.

## Flujo de trabajo

1. Partir del `main` actualizado.
2. Confirmar nombre de rama.
3. Confirmar `major`, `minor` o `patch`.
4. Crear la rama dedicada; no hacer commits ni push directamente a `main`.
5. Aplicar el incremento SemVer sobre `revision`.
6. Realizar el cambio.
7. Actualizar `CHANGELOG.md` con la nueva versión funcional.
8. Actualizar `README.md` cuando el cambio o la política del repositorio lo requiera.
9. Ejecutar los checks y tests aplicables.
10. Abrir una Pull Request hacia `main`.
11. No fusionar una Pull Request salvo autorización explícita del usuario.
12. Si el usuario autoriza merge automático, fusionar únicamente con los checks requeridos en verde.
13. Eliminar la rama origen después del merge cuando exista automatización para ello.

## Versionado Maven CI-friendly

```xml
<version>${revision}${sha1}${changelist}</version>
```

- `revision`: versión SemVer funcional.
- `sha1`: `-<short-sha>`, generado por CI; no modificar manualmente.
- `changelist`: vacío o `-SNAPSHOT`.
- DEV, INT y QA deben promover el mismo artefacto y conservar la misma versión.

## SemVer

- `patch`: `X.Y.Z` -> `X.Y.(Z+1)`
- `minor`: `X.Y.Z` -> `X.(Y+1).0`
- `major`: `X.Y.Z` -> `(X+1).0.0`

El `CHANGELOG.md` usa `revision`, nunca la versión de build con SHA.

## Tests

- Baseline Java: JDK 21.
- Ejecutar como mínimo `mvn -B test`.
- Para persistencia/integración, ejecutar también los perfiles definidos por el repositorio.

## Pull Requests

Antes de abrir o actualizar una PR comprobar que rama y SemVer fueron confirmados, `revision` es correcta, `sha1` no está persistido manualmente, `changelist` refleja SNAPSHOT correctamente y la PR indica nivel SemVer y versión anterior/nueva.
