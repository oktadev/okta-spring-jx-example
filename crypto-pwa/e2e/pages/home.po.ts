import { by, element } from 'protractor';
import { Page } from './app.po';

export class HomePage extends Page {
  addCoinsButton = element(by.buttonText('Add Coins'));
  deleteButton = element.all(by.css('button[color=danger]')).last();

  clickAddCoinsButton() {
    this.addCoinsButton.click();
  }
}
