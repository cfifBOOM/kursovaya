// Снимает скриншоты страниц запущенного приложения (запускается в GitHub Actions,
// файл .github/workflows/screenshots.yml). Скриншоты сохраняются в docs/screenshots.
const { chromium } = require('playwright');

const BASE = 'http://localhost:8080';
const OUT = 'docs/screenshots/';

// Оборудование для примера. Добавляется по порядку, в списке новые записи будут сверху.
const ITEMS = [
    ['Коммутатор TP-Link TL-SG1016D', 'ИНВ-000045', 'Серверная', 'NETWORK'],
    ['Принтер Canon i-SENSYS LBP223', 'ИНВ-000092', '312', 'PRINTER'],
    ['Системный блок Dell OptiPlex 7010', 'ИНВ-000118', '214', 'COMPUTER'],
    ['МФУ HP LaserJet Pro M428', 'ИНВ-000131', '101', 'PRINTER'],
    ['Монитор Samsung S24R350', 'ИНВ-000145', '205', 'MONITOR'],
    ['Ноутбук Lenovo ThinkPad E14', 'ИНВ-000146', '205', 'COMPUTER'],
];

async function shot(page, name) {
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await page.screenshot({ path: OUT + name + '.png', fullPage: true });
    console.log('Скриншот: ' + name);
}

(async () => {
    // Ждем, пока приложение запустится
    for (let i = 0; i < 60; i++) {
        try {
            const r = await fetch(BASE + '/login');
            if (r.ok) break;
        } catch (e) { }
        await new Promise(r => setTimeout(r, 2000));
    }

    const browser = await chromium.launch();
    const page = await browser.newPage({ viewport: { width: 760, height: 300 }, deviceScaleFactor: 2 });

    // Регистрация
    await page.goto(BASE + '/register');
    await page.fill('#login', 'ivanov');
    await page.fill('#password', 'qwerty12');
    await shot(page, 'register');
    await page.click('main button[type=submit]');
    await page.waitForURL(/\/login\?registered$/);

    // Вход
    await page.fill('#username', 'ivanov');
    await page.fill('#password', 'qwerty12');
    await shot(page, 'login');
    await page.click('main button[type=submit]');
    await page.waitForURL(/\/equipment$/);

    // Добавление оборудования через форму
    await page.setViewportSize({ width: 1000, height: 300 });
    for (const [name, inv, room, type] of ITEMS) {
        await page.goto(BASE + '/equipment/new');
        await page.fill('#name', name);
        await page.fill('#inventoryNumber', inv);
        await page.fill('#room', room);
        await page.selectOption('#equipmentType', type);
        await page.click('main button[type=submit]');
        await page.waitForURL(/\/equipment$/);
    }

    // Список и фильтр по типу
    await page.goto(BASE + '/equipment');
    await shot(page, 'list');
    await page.goto(BASE + '/equipment?type=PRINTER');
    await shot(page, 'list-printers');

    // Ошибка в форме: название не заполнено
    await page.goto(BASE + '/equipment/new');
    await page.fill('#inventoryNumber', 'ИНВ-000147');
    await page.fill('#room', '210');
    await page.selectOption('#equipmentType', 'MONITOR');
    await page.click('main button[type=submit]');
    await page.waitForSelector('.error');
    await shot(page, 'form-error');

    // Редактирование записи
    await page.goto(BASE + '/equipment');
    await page.locator('tr', { hasText: 'МФУ HP LaserJet Pro M428' }).locator('a', { hasText: 'Изменить' }).click();
    await page.waitForURL(/\/edit$/);
    await shot(page, 'edit');

    await browser.close();
})();
