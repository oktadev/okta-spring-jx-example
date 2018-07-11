import { Component } from '@angular/core';
import { App, IonicPage, NavController } from 'ionic-angular';
import { HoldingsProvider } from '../../providers/holdings/holdings';
import { UserProvider } from '../../providers/user/user';

@IonicPage()
@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
  name;

  constructor(private navCtrl: NavController, private holdingsProvider: HoldingsProvider,
              private userProvider: UserProvider, private app: App) {
  }

  ionViewDidLoad(): void {
    this.userProvider.getUser().subscribe((user: any) => {
      if (user === null) {
        this.navCtrl.push('LoginPage');
      } else {
        this.name = user.name;
        this.holdingsProvider.loadHoldings();
      }
    })
  }

  addHolding(): void {
    this.navCtrl.push('AddHoldingPage');
  }

  goToCryptonator(): void {
    window.open('https://www.cryptonator.com/api', '_system');
  }

  refreshPrices(refresher): void {
    this.holdingsProvider.fetchPrices(refresher);
  }

  logout() {
    this.userProvider.logout().subscribe((response: any) => {
      console.log(response);
      if (response.logoutUrl) {
        location.href = response.logoutUrl + "?id_token_hint=" + response.idToken + "&post_logout_redirect_uri=" + window.location.origin;
      } else {
        this.app.getRootNavs()[0].setRoot('LoginPage')
      }
    });
  }
}
