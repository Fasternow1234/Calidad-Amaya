import assert from 'node:assert/strict';
import { mkdir, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import { Builder, By, Select, until } from 'selenium-webdriver';

const baseUrl = process.env.TEST_BASE_URL ?? 'http://localhost:4200';
const username = process.env.TEST_USERNAME;
const password = process.env.TEST_PASSWORD;
const productName = process.env.TEST_PRODUCT_NAME;

for (const [name, value] of Object.entries({
  TEST_USERNAME: username,
  TEST_PASSWORD: password,
  TEST_PRODUCT_NAME: productName,
})) {
  if (!value) throw new Error(`Configura la variable ${name} antes de ejecutar Selenium.`);
}

const driver = await new Builder().forBrowser('chrome').build();

try {
  await driver.manage().window().maximize();
  await driver.get(`${baseUrl}/login`);

  await driver.wait(until.elementLocated(By.id('nombreUsuario')), 15000);
  await driver.findElement(By.id('nombreUsuario')).sendKeys(username);
  await driver.findElement(By.id('password')).sendKeys(password);
  await driver.findElement(By.css('button[type="submit"]')).click();

  const comprobantesNav = By.xpath(
    "//button[contains(@class,'nav-item')][.//span[contains(@class,'nav-label') and normalize-space()='Comprobantes']]",
  );
  await driver.wait(until.elementLocated(comprobantesNav), 15000);
  await driver.findElement(comprobantesNav).click();

  const tabla = By.css('table.tabla');
  const contarComprobantes = async () => {
    const tablas = await driver.findElements(tabla);
    if (tablas.length === 0) return 0;
    return (await driver.findElements(By.css('table.tabla tbody tr'))).length;
  };
  const filasAntes = await contarComprobantes();

  const nuevoComprobante = By.css('app-comprobantes .page-header button.btn-primary');
  await driver.wait(until.elementLocated(nuevoComprobante), 15000, 'No se abrió la sección Comprobantes.');
  await driver.findElement(nuevoComprobante).click();
  await driver.wait(until.elementLocated(By.css('app-nuevo-comprobante form')), 10000);

  const tipoOperacion = await driver.findElement(By.id('tipo'));
  await new Select(tipoOperacion).selectByValue('VENTA');

  const clienteSelect = await driver.findElement(By.id('idCliente'));
  await driver.wait(async () => {
    const options = await clienteSelect.findElements(By.css('option'));
    for (const option of options) {
      if ((await option.getText()).includes('Cliente Prueba Automatizada')) return true;
    }
    return false;
  }, 10000, 'No se encontró el cliente ficticio del ambiente de pruebas.');
  const clienteOptions = await clienteSelect.findElements(By.css('option'));
  const clientePrueba = await Promise.all(clienteOptions.map(async option => ({
    option,
    text: (await option.getText()).trim(),
  }))).then(options => options.find(({ text }) => text.includes('Cliente Prueba Automatizada')));
  assert.ok(clientePrueba, 'Debe existir el cliente ficticio para registrar la venta.');
  await new Select(clienteSelect).selectByVisibleText(clientePrueba.text);
  const clienteSeleccionado = await new Select(clienteSelect).getFirstSelectedOption();
  assert.match(await clienteSeleccionado.getText(), /Cliente Prueba Automatizada/);

  const productoSelect = await driver.findElement(By.css('select[formcontrolname="idProducto"]'));
  let productoValue;
  await driver.wait(async () => {
    const options = await productoSelect.findElements(By.css('option'));
    for (const option of options) {
      const text = (await option.getText()).trim();
      const value = await option.getAttribute('value');
      if (value && value !== '0' && text.toLocaleLowerCase().startsWith(productName.toLocaleLowerCase())) {
        productoValue = value;
        return true;
      }
    }
    return false;
  }, 10000, `No se encontró el producto de prueba: ${productName}.`);
  await new Select(productoSelect).selectByValue(productoValue);

  const cantidad = await driver.findElement(By.css('input[formcontrolname="cantidad"]'));
  await cantidad.clear();
  await cantidad.sendKeys('1.5');
  await driver.findElement(By.css('app-nuevo-comprobante button[type="submit"]')).click();

  const errorCantidad = By.xpath(
    "//small[contains(@class,'error') and normalize-space()='Ingresa una cantidad entera mayor que cero.']",
  );
  await driver.wait(until.elementLocated(errorCantidad), 5000);
  const filasDespues = await contarComprobantes();
  assert.equal(filasDespues, filasAntes, 'La validación no debe crear un comprobante.');

  const evidenceDirectory = resolve('e2e/selenium/evidencias');
  await mkdir(evidenceDirectory, { recursive: true });
  const evidencePath = resolve(evidenceDirectory, 'cantidad-fraccionaria-rechazada.png');
  await writeFile(evidencePath, await driver.takeScreenshot(), 'base64');
  console.log(`APROBADO: Selenium detectó la cantidad fraccionaria, no creó un comprobante y guardó evidencia en ${evidencePath}.`);
} finally {
  await driver.quit();
}
