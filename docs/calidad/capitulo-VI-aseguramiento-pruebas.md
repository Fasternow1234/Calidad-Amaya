# CAPÍTULO VI. ASEGURAMIENTO Y PRUEBAS DE CALIDAD

Este capítulo describe la estrategia aplicada para verificar la historia de usuario HU-08, los casos de prueba definidos, los resultados obtenidos, el seguimiento de un defecto funcional y la relación entre los criterios de aceptación y las pruebas. La ejecución combinó comprobaciones manuales en la aplicación con pruebas automatizadas en un ambiente aislado. TestLink se utilizó para organizar los requisitos, los casos, el plan y el registro de resultados.

## 6.1. Estrategia / plan de pruebas

### 6.1.1. Propósito y alcance

El propósito de las pruebas fue comprobar que el sistema permite registrar comprobantes de venta con datos válidos, impide vender una cantidad superior al stock disponible y actualiza correctamente las existencias y el Kardex. También se verificó una regla complementaria definida durante el proyecto: las cantidades de los productos deben ser enteras.

La prueba funcional se concentró en la historia HU-08, «Registro de comprobante de venta». Se verificaron los tres criterios de aceptación originales documentados para esa historia. La validación de cantidades enteras se incorporó como criterio complementario del equipo, por lo que se identifica por separado y no se presenta como uno de los criterios originales.

### 6.1.2. Enfoque y técnicas

Se aplicaron dos formas de ejecución:

- **Pruebas manuales:** se realizaron directamente en la aplicación para registrar una venta válida y comprobar el rechazo de una venta que excedía las existencias. El usuario observó los mensajes y consultó el stock y el Kardex después de las operaciones.
- **Pruebas automatizadas de interfaz:** se utilizó Selenium WebDriver en un ambiente de pruebas aislado para verificar el rechazo de una cantidad fraccionaria y el registro válido de una venta, incluyendo el comprobante, el cambio de stock y el movimiento en el Kardex.

Además, se ejecutaron pruebas unitarias del frontend y del backend para comprobar la validación de cantidades enteras y el comportamiento relacionado del DTO de detalle del comprobante. Estas pruebas complementan las verificaciones funcionales, pero no sustituyen la comprobación de los flujos completos en la interfaz.

### 6.1.3. Ambiente y herramientas

Las pruebas automatizadas utilizaron el perfil de pruebas de la aplicación y una base de datos separada denominada `bd_amaya_test`, con datos ficticios preparados para las ejecuciones. Esta separación permitió comprobar los flujos automatizados sin usar los datos de la base de datos funcional del proyecto.

Las herramientas empleadas fueron:

- Aplicación Amaya, con frontend Angular y backend Spring Boot.
- Selenium WebDriver para la automatización de los flujos de interfaz.
- TestLink 1.9.20 para registrar la especificación de requisitos, los casos de prueba, el plan, la versión de ejecución y los resultados.
- Pruebas unitarias del frontend y del backend, con sus reportes de cobertura correspondientes.

En TestLink se creó el proyecto «Calidad de Software - Distribuidora Amaya», el plan «Plan de pruebas HU08» y la versión de ejecución «Ejecución HU08 - 18/10/2026».

### 6.1.4. Condiciones de aceptación de las pruebas

Un caso se consideró aprobado cuando el comportamiento observado coincidió con el resultado esperado y no se detectó el efecto que el caso debía impedir. Por ejemplo, para la prueba de stock insuficiente, el resultado correcto era que el sistema rechazara la operación y no descontara existencias; para una venta válida, debía registrar el comprobante y reflejar la salida en stock y Kardex.

La ejecución se consideró completa cuando los cuatro casos funcionales definidos para HU-08 fueron registrados en TestLink y contaron con un resultado. Los estados se asignaron a partir de las ejecuciones realizadas y de la evidencia observada, no únicamente de los resultados esperados escritos en los casos.

## 6.2. Casos de prueba

Se definieron cuatro casos de prueba para cubrir los criterios de HU-08. Los identificadores `AMA-1` a `AMA-4` son los asignados por TestLink durante la importación; `CP-HU08-01` a `CP-HU08-04` son los identificadores funcionales utilizados en el proyecto.

| ID del proyecto / TestLink | Caso y datos principales | Procedimiento resumido | Resultado esperado |
|---|---|---|---|
| CP-HU08-01 / AMA-1 | Registrar una venta de 2 unidades de Coca Cola 2L cuando hay 180 unidades disponibles. | Abrir un nuevo comprobante de venta, completar los datos obligatorios, seleccionar el producto e ingresar 2 unidades; registrar la operación y consultar el stock y el Kardex. | Se registra el comprobante; el stock disminuye de 180 a 178 y el Kardex muestra una salida de 2 unidades asociada a la venta. |
| CP-HU08-02 / AMA-2 | Intentar vender 179 unidades de Coca Cola 2L cuando hay 178 disponibles. | Preparar un comprobante de venta con el producto y solicitar 179 unidades; intentar registrarlo. | El sistema rechaza la operación, informa que hay 178 unidades disponibles y que se solicitan 179; no registra la venta ni descuenta stock. |
| CP-HU08-03 / AMA-3 | En el ambiente aislado, ingresar `1.5` unidades de `Producto Selenium Prueba`. | Ejecutar el flujo automatizado `reject-fractional-quantity.mjs` y comprobar el mensaje de validación y la lista de comprobantes. | El formulario rechaza la cantidad fraccionaria, solicita una cantidad entera y no crea un comprobante nuevo. |
| CP-HU08-04 / AMA-4 | En el ambiente aislado, registrar una venta válida de 1 unidad de `Producto Selenium Prueba`. | Ejecutar `register-valid-sale.mjs`; comprobar el comprobante creado, el stock y el Kardex. | Se registra el comprobante; el stock disminuye de 19 a 18 y el Kardex muestra la salida de 1 unidad y el saldo actualizado. |

Los casos CP-HU08-01 y CP-HU08-02 se ejecutaron manualmente. CP-HU08-03 y CP-HU08-04 se automatizaron con Selenium WebDriver. La definición completa de precondiciones, pasos y resultados esperados se conserva en [`HU08-TestLink.xml`](HU08-TestLink.xml). En TestLink, las fichas importadas muestran el tipo de ejecución «Manual», aunque CP-HU08-03 y CP-HU08-04 se ejecutaron con Selenium; conviene corregir ese dato en las fichas antes de cerrar la versión final del informe, o conservar esta aclaración si el campo no se modifica.

## 6.3. Resultados de las pruebas

### 6.3.1. Resultados funcionales

Los cuatro casos de HU-08 se registraron en el plan y la versión de ejecución de TestLink. Al finalizar, la pantalla de ejecución mostró cuatro casos aprobados, cero fallidos y cero bloqueados.

| Caso | Resultado observado | Método | Estado en TestLink | Evidencia |
|---|---|---|---|---|
| CP-HU08-01 / AMA-1 | Venta F001-00000004 registrada por 2 unidades; stock de 180 a 178; Kardex con salida de 2. | Manual | Aprobado (Passed) | Resultado informado durante la prueba en la aplicación. |
| CP-HU08-02 / AMA-2 | Al solicitar 179 unidades con 178 disponibles, la aplicación informó stock insuficiente y rechazó la operación. | Manual | Aprobado (Passed) | Resultado informado durante la prueba en la aplicación. |
| CP-HU08-03 / AMA-3 | Selenium comprobó el rechazo de `1.5`; no se creó un comprobante nuevo. | Automatizado con Selenium | Aprobado (Passed) | [`cantidad-fraccionaria-rechazada.png`](../../Frontend-Amaya/e2e/selenium/evidencias/cantidad-fraccionaria-rechazada.png). |
| CP-HU08-04 / AMA-4 | Selenium registró F001-00000002; el stock bajó de 19 a 18 y el Kardex registró una salida de 1. | Automatizado con Selenium | Aprobado (Passed) | [Comprobante](../../Frontend-Amaya/e2e/selenium/evidencias/venta-valida-comprobante.png), [stock](../../Frontend-Amaya/e2e/selenium/evidencias/venta-valida-stock.png) y [Kardex](../../Frontend-Amaya/e2e/selenium/evidencias/venta-valida-kardex.png). |

La captura del resultado final del plan en TestLink se conserva en [`HU08-TestLink-ejecucion.png`](../../Frontend-Amaya/e2e/selenium/evidencias/HU08-TestLink-ejecucion.png). Las pruebas manuales se basan en los resultados observados y reportados por el usuario; no se dispone en este registro de capturas independientes para CP-HU08-01 y CP-HU08-02.

### 6.3.2. Pruebas unitarias y cobertura

Como complemento de las pruebas funcionales, se ejecutaron seis pruebas del frontend y ocho pruebas del backend. Los reportes disponibles indican cobertura completa en las clases objetivo relacionadas con la validación, mientras que la cobertura general del producto es menor.

| Alcance medido | Líneas | Ramas | Otras métricas |
|---|---:|---:|---|
| Frontend completo (`src/**/*.ts`, sin archivos `.spec.ts`) | 0.27 % | 1.76 % | Sentencias 0.47 %; funciones 0.32 %. |
| Validador Angular `cantidad-entera.validator.ts` | 100 % | 100 % | Sentencias, funciones y líneas: 100 %. |
| Backend completo, con los tests disponibles | 7.88 % | 3.04 % | Instrucciones 9.19 %; métodos 11.34 %. |
| DTO `DetalleComprobanteRequest` | 100 % | 100 % | Instrucciones 23/23; líneas 4/4; ramas 6/6. |

La cobertura mide qué partes del código instrumentado fueron recorridas por las pruebas; no representa por sí sola el porcentaje de requisitos funcionales satisfechos. Por ello, la cobertura completa del validador y del DTO debe interpretarse junto con los resultados funcionales de los cuatro casos. Las pruebas Selenium verificaron flujos visibles de la aplicación y no se contabilizaron en los reportes unitarios de Vitest ni JaCoCo.

## 6.4. Registro y seguimiento de defectos

Durante las comprobaciones se identificó un defecto relacionado con las cantidades de los productos: la interfaz permitía ingresar valores fraccionarios, aunque los productos evaluados se manejan en unidades enteras. El comportamiento podía permitir registrar cantidades no válidas para la operación del negocio.

Se incorporó una validación para exigir cantidades enteras. Después del cambio, el caso CP-HU08-03 se ejecutó con Selenium en el ambiente aislado. La prueba confirmó que `1.5` es rechazado, que se muestra la validación correspondiente y que no se crea un comprobante nuevo. En TestLink, el caso quedó como **Passed**, por lo que la corrección se considera verificada en el alcance de esta prueba.

No se asignó un código formal de incidencia a este defecto en un gestor de defectos. Por ese motivo, se registra aquí descriptivamente y no se inventa un identificador. Los demás casos ejecutados no reportaron defectos dentro del alcance cubierto.

| Defecto observado | Acción realizada | Verificación | Estado al cierre de esta ejecución |
|---|---|---|---|
| El formulario permitía ingresar cantidades fraccionarias. | Se agregó validación para aceptar cantidades enteras. | CP-HU08-03: Selenium ingresó `1.5`; la aplicación lo rechazó y no creó un comprobante. | Corregido y verificado para el escenario probado. |

## 6.5. Matriz de trazabilidad

La matriz relaciona los criterios originales de aceptación de HU-08 con los casos que aportan evidencia para verificarlos. CA-01, CA-02 y CA-03 corresponden a los criterios originales del informe. CA-04 es un criterio complementario incorporado por el equipo para documentar la regla de cantidades enteras.

| Criterio | Descripción | Casos relacionados | Cobertura y resultado |
|---|---|---|---|
| CA-01 (original) | El sistema debe permitir registrar un comprobante de venta indicando los productos y cantidades correspondientes. | CP-HU08-01 y CP-HU08-04 | Venta válida registrada manualmente y mediante Selenium. Ambos casos aprobados. |
| CA-02 (original) | El sistema debe verificar que exista stock suficiente antes de registrar la venta. | CP-HU08-02 | La solicitud de 179 unidades con 178 disponibles fue rechazada. Caso aprobado. |
| CA-03 (original) | Al registrar correctamente la venta, el sistema debe generar una salida y disminuir el stock según las cantidades vendidas. | CP-HU08-01 y CP-HU08-04 | Se comprobó la disminución de stock y la salida correspondiente en el Kardex. Ambos casos aprobados. |
| CA-04 (complementario) | El sistema debe rechazar cantidades fraccionarias y no registrar el comprobante. | CP-HU08-03 | Selenium verificó el rechazo de `1.5` y la ausencia de un comprobante nuevo. Caso aprobado. |

En TestLink, CA-01 se relacionó con AMA-1 y AMA-4; CA-02 con AMA-2; CA-03 con AMA-1 y AMA-4; y CA-04 con AMA-3. La trazabilidad facilita identificar qué pruebas respaldan cada criterio y qué evidencia debe revisarse para confirmar su cumplimiento.
