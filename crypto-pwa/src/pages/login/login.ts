import { Component } from '@angular/core';
import { App, IonicPage, NavController } from 'ionic-angular';
import { UserProvider } from '../../providers/user/user';

@IonicPage({
  name: 'LoginPage'
})
@Component({
  selector: 'page-login',
  templateUrl: 'login.html'
})
export class LoginPage {

  constructor(private userProvider: UserProvider, private app: App) {
    userProvider.getUser().subscribe((user) => {
      if (user !== null) {
        this.app.getRootNavs()[0].setRoot('HomePage');
      }
    });
  }

  login() {
    this.userProvider.login();
  }
}
