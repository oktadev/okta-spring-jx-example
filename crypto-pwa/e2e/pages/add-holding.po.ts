import { $, by, element } from 'protractor';
import { Page } from './app.po';

export class AddHoldingPage extends Page {
  cryptoCode = element.all(by.css('input[type=text]')).first();
  displayCurrency = element.all(by.css('input[type=text]')).get(1);
  amountHolding = element.all(by.css('input[type=number]'));
  addHoldingButton = element(by.buttonText('Add Holding'));
  pageTitle = $('ion-title');

  setCryptoCode(code) {
    this.cryptoCode.sendKeys(code);
  }

  setCurrency(currency) {
    this.displayCurrency.sendKeys(currency);
  }

  setAmount(amount) {
    this.amountHolding.sendKeys(amount);
  }

  clickAddHoldingButton() {
    this.addHoldingButton.click();
  }

  getPageTitle() {
    return this.pageTitle;
  }
}
