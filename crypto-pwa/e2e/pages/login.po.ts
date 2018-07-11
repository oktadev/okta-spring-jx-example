import { browser, by, element } from 'protractor';
import { Page } from './app.po';

export class LoginPage extends Page {
  username = element(by.name('username'));
  password = element(by.name('password'));
  oktaLoginButton = element(by.css('input[type=submit]'));
  loginButton = element.all(by.css('#login')).last();
  logoutButton = element.all(by.css('#logout')).last();
  header = element.all(by.css('ion-title')).first();

  getHeader() {
    return this.header.getText();
  }

  setUserName(username) {
    this.username.sendKeys(username);
  }

  getUserName() {
    return this.username.getAttribute('value');
  }

  clearUserName() {
    this.username.clear();
  }

  setPassword(password) {
    this.password.sendKeys(password);
  }

  getPassword() {
    return this.password.getAttribute('value');
  }

  clearPassword() {
    this.password.clear();
  }

  login(username: string, password: string) {
    // Entering non angular site, tell webdriver to switch to synchronous mode.
    browser.waitForAngularEnabled(false);
    this.username.isPresent().then(() => {
      this.username.sendKeys(username);
      this.password.sendKeys(password);
      this.oktaLoginButton.click();
    }).catch(error => {
      browser.waitForAngularEnabled(true);
    });
  }

  clickLoginButton() {
    return this.loginButton.click();
  }

  logout() {
    return this.logoutButton.click();
  }
}
