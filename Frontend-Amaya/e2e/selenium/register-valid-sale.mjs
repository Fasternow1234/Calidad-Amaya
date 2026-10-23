import assert from 'node:assert/strict';
import { mkdir, writeFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import { Builder, By, Select, until } from 'selenium-webdriver';

const baseUrl = process.env.TEST_BASE_URL ?? 'http://localhost:4200';
const username = process.env.TEST_USERNAME;
const password = process.env.TEST_PASSWORD;
const productName = process.env.TEST_PRODUCT_NAME ?? 'Producto Selenium Prueba';
const productCode = process.env.TEST_PRODUCT_CODE ?? 'E2E-PRD-001';

for (const [name, value] of Object.entries({ TEST_USERNAME: username, TEST_PASSWORD: password })) {
  if (!value) throw new Error(`Configura la variable ${name} antes de ejecutar Selenium.`);
}

const driver = await new Builder().forBrowser('chrome').build();
const evidenceDirectory = resolve('e2e/selenium/evidencias');

async function saveEvidence(name) {
  await mkdir(evidenceDirectory, { recursive: true });
  await driver.executeScript("document.body.style.zoom = '75%';");
  await writeFile(resolve(evidenceDirectory, name), await driver.takeScreenshot(), 'base64');
}

async function selectCustomer(selectElement) {
  await driver.wait(async () => {
    const options = await selectElement.findElements(By.css('option'));
    for (const option of options) {
      if ((await option.getText()).includes('Cliente Prueba Automatizada')) return true;
    }
    return false;
  }, 10000, 'No se encontro el cliente ficticio del ambiente de pruebas.');

  const options = await selectElement.findElements(By.css('option'));
  for (const option of options) {
    const text = (await option.getText()).trim();
    if (!text.includes('Cliente Prueba Automatizada')) continue;
    await new Select(selectElement).selectByVisibleText(text);
    const selected = await new Select(selectElement).getFirstSelectedOption();
    assert.match(await selected.getText(), /Cliente Prueba Automatizada/);
    return;
  }
  throw new Error('No se pudo seleccionar el cliente ficticio.');
}

async function selectTestProduct(selectElement) {
  await driver.wait(async () => {
    const options = await selectElement.findElements(By.css('option'));
    for (const option of options) {
      if ((await option.getText()).toLowerCase().startsWith(productName.toLowerCase())) return true;
    }
    return false;
  }, 10000, `No se encontro el producto de prueba: ${productName}.`);

  const options = await selectElement.findElements(By.css('option'));
  for (const option of options) {
    const text = (await option.getText()).trim();
    if (!text.toLowerCase().startsWith(productName.toLowerCase())) continue;
    const match = text.match(/Stock:\s*([\d.]+)/i);
    assert.ok(match, `No se pudo leer el stock en la opcion del producto: ${text}`);
    const stockAntes = Number(match[1]);
    assert.ok(stockAntes >= 1, `Stock insuficiente para la prueba: ${stockAntes}`);
    await new Select(selectElement).selectByVisibleText(text);
    return stockAntes;
  }
  throw new Error(`No se pudo seleccionar el producto ${productName}.`);
}

async function openSection(label) {
  const navButton = By.xpath(`//button[contains(@class,'nav-item')][.//span[contains(@class,'nav-label') and normalize-space()='${label}']]`);
  await driver.wait(until.elementLocated(navButton), 10000, `No aparece la opcion ${label} en el menu.`);
  await driver.findElement(navButton).click();
}

try {
  await driver.manage().window().setRect({ width: 1440, height: 1000, x: 0, y: 0 });
  await driver.get(`${baseUrl}/login`);
  await driver.wait(until.elementLocated(By.id('nombreUsuario')), 15000);
  await driver.findElement(By.id('nombreUsuario')).sendKeys(username);
  await driver.findElement(By.id('password')).sendKeys(password);
  await driver.findElement(By.css('button[type="submit"]')).click();

  await openSection('Comprobantes');
  const tableRows = By.css('app-comprobantes table.tabla tbody tr');
  await driver.wait(until.elementLocated(By.css('app-comprobantes .page-header button.btn-primary')), 15000);
  const beforeRows = await driver.findElements(tableRows);
  const countBefore = beforeRows.length;
  let nextCorrelativo = 1;
  for (const row of beforeRows) {
    const number = await row.findElement(By.css('td.col-numero')).getText();
    const match = number.match(/^F001-(\d+)$/);
    if (match) nextCorrelativo = Math.max(nextCorrelativo, Number(match[1]) + 1);
  }

  await driver.findElement(By.css('app-comprobantes .page-header button.btn-primary')).click();
  await driver.wait(until.elementLocated(By.css('app-nuevo-comprobante form')), 10000);
  await new Select(await driver.findElement(By.id('tipo'))).selectByValue('VENTA');
  const correlativo = await driver.findElement(By.id('correlativo'));
  await correlativo.clear();
  await correlativo.sendKeys(String(nextCorrelativo));
  await selectCustomer(await driver.findElement(By.id('idCliente')));

  const productSelect = await driver.findElement(By.css('select[formcontrolname="idProducto"]'));
  const stockBefore = await selectTestProduct(productSelect);
  const quantity = await driver.findElement(By.css('input[formcontrolname="cantidad"]'));
  await quantity.clear();
  await quantity.sendKeys('1');

  await driver.findElement(By.css('app-nuevo-comprobante button[type="submit"]')).click();
  await driver.wait(async () => (await driver.findElements(By.css('app-nuevo-comprobante form'))).length === 0, 15000,
    'No se cerro el formulario luego de registrar la venta.');
  await driver.wait(async () => (await driver.findElements(tableRows)).length === countBefore + 1, 15000,
    'El comprobante nuevo no aparecio en el listado.');

  const rowsAfterSale = await driver.findElements(tableRows);
  const invoiceNumber = await rowsAfterSale[0].findElement(By.css('td.col-numero')).getText();
  const newestRowText = await rowsAfterSale[0].getText();
  assert.match(newestRowText, /Venta/);
  assert.match(newestRowText, /Cliente Prueba Automatizada/);
  assert.match(invoiceNumber, /^F001-/);
  await saveEvidence('venta-valida-comprobante.png');

  await openSection('Productos');
  const productRow = By.xpath(`//app-productos//table[contains(@class,'tabla')]//tr[.//span[contains(@class,'producto-nombre') and normalize-space()='${productName}']]`);
  await driver.wait(until.elementLocated(productRow), 15000, 'No aparece el producto en el catalogo.');
  const stockCell = await driver.findElement(productRow).findElement(By.css('td.col-stock .stock-badge'));
  const stockAfterText = await stockCell.getText();
  const stockAfterMatch = stockAfterText.match(/[\d.]+/);
  assert.ok(stockAfterMatch, `No se pudo leer el stock actualizado: ${stockAfterText}`);
  const stockAfter = Number(stockAfterMatch[0]);
  assert.equal(stockAfter, stockBefore - 1, 'El stock debe disminuir exactamente una unidad.');
  await saveEvidence('venta-valida-stock.png');

  await openSection('Kardex');
  const kardexProduct = await driver.findElement(By.css('app-kardex select'));
  await driver.wait(async () => {
    const options = await kardexProduct.findElements(By.css('option'));
    for (const option of options) if ((await option.getText()).includes(productCode)) return true;
    return false;
  }, 10000, 'No aparece el producto de prueba en el selector del Kardex.');
  const kardexOptions = await kardexProduct.findElements(By.css('option'));
  for (const option of kardexOptions) {
    const text = (await option.getText()).trim();
    if (text.includes(productCode)) {
      await new Select(kardexProduct).selectByVisibleText(text);
      break;
    }
  }
  await driver.findElement(By.css('app-kardex button.btn-primary')).click();
  await driver.wait(until.elementLocated(By.css('app-kardex section.product-summary')), 15000,
    'No se cargo el resumen del Kardex.');
  const saleMovement = By.xpath(`//app-kardex//tbody/tr[contains(.,'${invoiceNumber}')]`);
  await driver.wait(until.elementLocated(saleMovement), 15000, 'El Kardex no muestra el movimiento de la venta.');
  const saleMovementRow = await driver.findElement(saleMovement);
  assert.match(await saleMovementRow.getText(), /Salida/i);
  assert.match(await saleMovementRow.findElement(By.css('td:nth-child(5)')).getText(), /^1(?:\.0+)?$/);
  const finalBalance = await driver.findElement(By.css('app-kardex .summary-grid article.balance strong')).getText();
  assert.match(finalBalance, new RegExp(`^${stockAfter}\\b`), 'El saldo final del Kardex debe coincidir con el stock.');
  await saveEvidence('venta-valida-kardex.png');

  console.log(`APROBADO: venta ${invoiceNumber}; stock ${stockBefore} a ${stockAfter}; Kardex registra una salida de 1. Evidencias guardadas en ${evidenceDirectory}.`);
} finally {
  await driver.quit();
}
