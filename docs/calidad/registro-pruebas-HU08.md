# Registro de pruebas — HU-08: registrar una venta

Este registro reúne los casos ejecutados hasta ahora. Los resultados manuales CP-HU08-01 y CP-HU08-02 fueron comunicados por el usuario durante la prueba en la aplicación. CP-HU08-03 y CP-HU08-04 se ejecutaron con Selenium WebDriver en el perfil de pruebas; las capturas se conservan en el repositorio.

## Casos de prueba

La especificación XML usada para importar los cuatro casos en TestLink está en [`HU08-TestLink.xml`](HU08-TestLink.xml). El XML contiene definiciones de pruebas, pasos, precondiciones y resultados esperados; los resultados de ejecución se registraron posteriormente en TestLink.

## Registro en TestLink

- Proyecto: `Calidad de Software - Distribuidora Amaya`.
- Especificación de requisitos: `HU08: Registro de comprobante de venta`; criterios CA-01 a CA-04 relacionados con los casos correspondientes.
- Plan: `Plan de pruebas HU08`, activo y público; contiene los cuatro casos.
- Build: `Ejecución HU08 - 18/10/2026`, activo y abierto.
- Resultado registrado el 18/10/2026: los cuatro casos aparecen como **Passed**; la pantalla de ejecución mostró 4 aprobados, 0 fallidos y 0 bloqueados.
- Identificadores asignados por TestLink: AMA-1 = CP-HU08-01, AMA-2 = CP-HU08-02, AMA-3 = CP-HU08-03 y AMA-4 = CP-HU08-04.
- Para CP-HU08-01 y CP-HU08-02 se registraron los resultados manuales reportados durante la prueba. CP-HU08-03 y CP-HU08-04 se documentaron como ejecuciones con Selenium en el ambiente de pruebas aislado.
- La captura del resultado final de TestLink se guardó en [`HU08-TestLink-ejecucion.png`](../../Frontend-Amaya/e2e/selenium/evidencias/HU08-TestLink-ejecucion.png). La confirmación de importación se observó en TestLink; si se desea incluirla como evidencia independiente, guardar también esa captura.

| ID | Escenario y datos | Resultado esperado | Resultado observado | Estado / evidencia |
|---|---|---|---|---|
| CP-HU08-01 | Registrar una venta de 2 unidades de Coca Cola 2L con 180 disponibles. | Se registra el comprobante, el stock baja en 2 y el Kardex muestra una salida de 2. | El usuario reportó venta exitosa F001-00000004; stock de 180 a 178; Kardex con salida de 2. | Aprobado, ejecución manual. Adjuntar al informe las capturas que el usuario tomó de comprobante, stock y Kardex. |
| CP-HU08-02 | Intentar vender 179 unidades de Coca Cola 2L cuando hay 178 disponibles. | El sistema rechaza la operación e informa el stock disponible y solicitado. | El usuario reportó el mensaje: disponible 178, solicitado 179. | Aprobado, ejecución manual. Adjuntar la captura del mensaje si se conserva. |
| CP-HU08-03 | En ambiente aislado, elegir el cliente ficticio y `Producto Selenium Prueba`; ingresar cantidad `1.5` y enviar el formulario. | El sistema indica que la cantidad debe ser entera y no crea un comprobante. | Selenium encontró el mensaje de validación y verificó que el número de filas de comprobantes no cambiara. | Aprobado, Selenium WebDriver. Evidencia: [`cantidad-fraccionaria-rechazada.png`](../../Frontend-Amaya/e2e/selenium/evidencias/cantidad-fraccionaria-rechazada.png). |
| CP-HU08-04 | En ambiente aislado, registrar la venta de 1 unidad de `Producto Selenium Prueba` con stock disponible. | Se crea un comprobante de venta; el stock baja en 1; el Kardex registra una salida asociada al comprobante y presenta el saldo actualizado. | Selenium registró F001-00000002; el stock bajó de 19 a 18; el Kardex muestra la salida de 1 para ese comprobante y saldo final 18. | Aprobado, Selenium WebDriver. Capturas: [comprobante](../../Frontend-Amaya/e2e/selenium/evidencias/venta-valida-comprobante.png), [stock](../../Frontend-Amaya/e2e/selenium/evidencias/venta-valida-stock.png), [Kardex](../../Frontend-Amaya/e2e/selenium/evidencias/venta-valida-kardex.png). |

## Pruebas unitarias relacionadas

- Frontend: 6 pruebas aprobadas (4 del validador y 2 del componente raíz). La prueba antigua del texto `Hello, Frontend-Amaya` se actualizó para comprobar el `router-outlet` real.
- Backend: 8 pruebas aprobadas (7 del DTO y 1 de carga del contexto Spring con el perfil `test`). Los casos del DTO incluyen valor entero mayor que el límite de precisión.

## Cobertura medida

Los reportes se generaron con Vitest/V8 para Angular y JaCoCo para Spring. Son porcentajes del código instrumentado por cada ejecución; los reportes completos incluyen código sin pruebas.

| Alcance | Líneas | Ramas | Otras métricas |
|---|---:|---:|---|
| Frontend completo (`src/**/*.ts`, excluidos los `.spec.ts`) | 0.27% | 1.76% | Sentencias 0.47%; funciones 0.32%. |
| Validador Angular `cantidad-entera.validator.ts` | 100% | 100% | Sentencias 100%; funciones 100%. |
| Backend completo, todos los tests actuales | 7.88% | 3.04% | Instrucciones 9.19%; métodos 11.34%. |
| DTO `DetalleComprobanteRequest` | 100% | 100% | Instrucciones 100% (23/23); líneas 100% (4/4); ramas 100% (6/6). |

Interpretación: la validación de cantidad sí tiene cobertura completa en las dos clases objetivo, pero la cobertura general del producto sigue baja. Los tests Selenium verifican el comportamiento funcional de la venta; no se suman a la cobertura unitaria de Angular ni a JaCoCo en esta ejecución.

Reportes HTML locales: `Frontend-Amaya/coverage/Frontend-Amaya/index.html` y `Backend-Amaya/target/site/jacoco/index.html`. Las carpetas se generan al ejecutar las pruebas y están excluidas del control de versiones.

Comandos de reproducción:

```powershell
# Frontend: cobertura de todo el TypeScript
npm run test -- --coverage --coverage-include="src/**/*.ts" --coverage-exclude="src/**/*.spec.ts" --coverage-reporters=text --coverage-reporters=html

# Backend: definir TEST_DB_USERNAME y TEST_DB_PASSWORD para bd_amaya_test,
# y SPRING_PROFILES_ACTIVE=test antes de ejecutar
./mvnw.cmd verify
```

## Trazabilidad con los criterios originales de HU-08

En la sección 4.9.1 del informe compartido, “Criterios de aceptación H-08: Registro de comprobante de venta”, aparecen estos tres criterios:

| ID original | Criterio del informe | Casos relacionados | Evidencia actual |
|---|---|---|---|
| CA-01 | El sistema debe permitir registrar un comprobante de venta indicando los productos y cantidades correspondientes. | CP-HU08-01 y CP-HU08-04. | Comprobantes de venta registrados manualmente y con Selenium. |
| CA-02 | El sistema debe verificar que exista stock suficiente antes de registrar la venta. | CP-HU08-02. | El usuario reportó el rechazo al pedir 179 unidades con 178 disponibles. |
| CA-03 | Al registrar correctamente la venta, el sistema debe generar una salida y disminuir el stock según las cantidades vendidas. | CP-HU08-01 y CP-HU08-04. | Se verificaron el descenso del stock y la salida correspondiente en Kardex. |

La regla de que las cantidades sean enteras no aparece en los tres criterios originales. Fue una regla adicional definida por el equipo y luego implementada y probada en CP-HU08-03. Para que su verificación también tenga trazabilidad formal en el informe, agregarla como un criterio complementario, por ejemplo: “El sistema debe rechazar cantidades fraccionarias y no registrar el comprobante”. No presentarla como texto original de CA-01, CA-02 o CA-03.

## Trazabilidad de la automatización funcional

- Script: [`reject-fractional-quantity.mjs`](../../Frontend-Amaya/e2e/selenium/reject-fractional-quantity.mjs).
- Script: [`register-valid-sale.mjs`](../../Frontend-Amaya/e2e/selenium/register-valid-sale.mjs).
- Instrucciones y variables de entorno: [`README.md`](../../Frontend-Amaya/e2e/selenium/README.md).
- Perfil de base de datos de pruebas: `spring.profiles.active=test`, con base separada `bd_amaya_test`.
- CP-HU08-03 comprueba el rechazo de una fracción. CP-HU08-04 automatiza el flujo de venta y sus efectos en comprobantes, stock y Kardex. CP-HU08-01 y CP-HU08-02 siguen siendo pruebas manuales reportadas por el usuario.
