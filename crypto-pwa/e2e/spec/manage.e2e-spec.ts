import { browser, by, element, ExpectedConditions as ec } from 'protractor';
import { LoginPage } from '../pages/login.po';
import { AddHoldingPage } from '../pages/add-holding.po';
import { HomePage } from '../pages/home.po';

describe('Manage Holdings', () => {

  let loginPage, homePage, addHoldingPage;

  beforeAll(() => {
    loginPage = new LoginPage();
    homePage = new HomePage();
    addHoldingPage = new AddHoldingPage();
    loginPage.navigateTo('/');
    browser.waitForAngular();
  });

  beforeEach(() => {
    browser.sleep(1000); // to prevent button not clickable error
    loginPage.clickLoginButton();
    loginPage.login(process.env.E2E_USERNAME, process.env.E2E_PASSWORD);
    loginPage.oktaLoginButton.click();

    const success = element.all(by.css('h1')).first();
    browser.wait(ec.visibilityOf(success), 5000).then(() => {
      expect(success.getText()).toMatch(/Welcome/);
    });
  });

  afterEach(() => {
    loginPage.logout();
  });

  it('should add and remove a holding', () => {
    browser.sleep(1000);
    homePage.clickAddCoinsButton();

    browser.wait(ec.urlContains('add-holding'), 1000);

    addHoldingPage.setCryptoCode('BTC');
    addHoldingPage.setCurrency('USD');
    addHoldingPage.setAmount(3);
    addHoldingPage.clickAddHoldingButton();

    // wait for everything to happen
    browser.wait(ec.urlContains('home'), 5000);

    // verify message is removed and holding shows up
    element.all(by.css('.message')).then((message) => {
      expect(message.length).toBe(0);
    });

    // wait for holding to show up
    const addedHolding = element.all(by.css('ion-item')).last();
    browser.wait(ec.presenceOf(addedHolding), 5000).then(() => {

      // delete the holding - https://forum.ionicframework.com/t/move-ion-item-sliding-by-protractor/106918
      browser.actions().mouseDown(addedHolding)
        .mouseMove({x: -50, y: 0})
        .mouseMove({x: -50, y: 0})
        .mouseMove({x: -50, y: 0})
        .mouseUp()
        .perform();

      homePage.deleteButton.click();
      element.all(by.css('.message')).then((message) => {
        expect(message.length).toBe(1);
      });
    });
  });
});
