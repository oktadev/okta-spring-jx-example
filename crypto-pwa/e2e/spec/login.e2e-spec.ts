import { browser, by, element, ExpectedConditions as ec } from 'protractor';
import { LoginPage } from '../pages/login.po';

describe('Login', () => {

  let loginPage;

  beforeAll(() => {
    loginPage = new LoginPage();
    loginPage.navigateTo('/');
    browser.waitForAngular();
  });

  it('should show a login button', () => {
    expect(loginPage.getHeader()).toMatch(/Login/);
    expect(loginPage.loginButton.isPresent());
  });

  it('should fail to log in with bad password', () => {
    browser.sleep(1000);
    loginPage.clickLoginButton();
    loginPage.login('admin', 'foo');
    const error = element.all(by.css('.infobox-error')).first();
    browser.wait(ec.visibilityOf(error), 2000).then(() => {
      expect(error.getText()).toMatch("Sign in failed!");
    });
  });

  it('should log in successfully with demo account', () => {
    loginPage.clearUserName();
    loginPage.setUserName(process.env.E2E_USERNAME);
    loginPage.clearPassword();
    loginPage.setPassword(process.env.E2E_PASSWORD);
    loginPage.oktaLoginButton.click();

    const welcome = /Welcome/; // Use /Welcome, First Last/ if you want to verify full name
    const success = element.all(by.css('h1')).first();
    browser.wait(ec.visibilityOf(success), 5000).then(() => {
      expect(success.getText()).toMatch(welcome);
    });
  });

  it('should log out successfully', () => {
    browser.sleep(1000);
    loginPage.logout();
    browser.wait(ec.urlContains('/#/login'), 5000);
    expect(loginPage.loginButton.isPresent());
  })
});
