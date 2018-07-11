import { Page } from '../pages/app.po';
import { browser, ExpectedConditions as ec } from 'protractor';

describe('App', () => {
  let page: Page;

  beforeEach(() => {
    page = new Page();
  });

  describe('default screen', () => {
    beforeEach(() => {
      page.navigateTo('/#/home');
    });

    it('should redirect to login', () => {
      browser.wait(ec.urlContains('/#/login'), 5000);
    });

    it('should have the correct title', () => {
      page.getTitle().then(title => {
        expect(title).toEqual('Cryptocurrency PWA with Authentication');
      });
    });
  });
});
