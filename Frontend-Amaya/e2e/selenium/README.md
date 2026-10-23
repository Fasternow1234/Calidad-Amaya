# Selenium WebDriver

La prueba `reject-fractional-quantity.mjs` automatiza el inicio de sesión, abre el formulario de venta e intenta ingresar 1.5 unidades. Comprueba que la pantalla muestre el error de cantidad entera y que el número de comprobantes no cambie. No registra ventas.

Ejecuta la prueba contra la aplicación iniciada con el perfil de pruebas y con datos de prueba cargados. El inicializador crea el producto `Producto Selenium Prueba` y un cliente ficticio. Al pasar, la prueba guarda una captura real en `e2e/selenium/evidencias/cantidad-fraccionaria-rechazada.png`. Selenium Manager puede necesitar conexión de red la primera vez para obtener el controlador de Chrome.

En PowerShell, configura las variables de entorno en la terminal antes de iniciar la prueba:

```powershell
$env:TEST_BASE_URL = 'http://localhost:4200'
$env:TEST_USERNAME = 'usuario_de_prueba'
$env:TEST_PASSWORD = 'contraseña_de_prueba'
$env:TEST_PRODUCT_NAME = 'Producto Selenium Prueba'
npm run test:e2e
```

No guardes credenciales en este archivo ni en el repositorio. La base de datos de pruebas debe contener al menos un cliente activo y el producto elegido.

La prueba `register-valid-sale.mjs` registra una venta de 1 unidad en la base aislada. Comprueba que aparezca un comprobante nuevo, que el stock disminuya en 1 y que el Kardex muestre la salida vinculada al número de comprobante. Guarda capturas de los tres resultados en `e2e/selenium/evidencias/`.

Con el backend en perfil `test` y Angular en `localhost:4200`, ejecuta `npm run test:e2e:sale` con las mismas variables de entorno anteriores. Esta prueba agrega una venta a `bd_amaya_test`; no ejecutarla contra una base de producción ni contra la base cotidiana.
