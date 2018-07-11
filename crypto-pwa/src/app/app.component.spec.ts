import { async, TestBed } from '@angular/core/testing';
import { IonicModule, Platform } from 'ionic-angular';
import { StatusBar } from '@ionic-native/status-bar';
import { SplashScreen } from '@ionic-native/splash-screen';
import { PlatformMock, SplashScreenMock, StatusBarMock } from 'ionic-mocks-jest';
import { MyApp } from './app.component';
import { UserProvider } from '../providers/user/user';
import { Observable } from 'rxjs/Rx';

describe('MyApp Component', () => {
  let fixture;
  let component;
  let userProvider = {
    getUser() {
      return Observable.of(null);
    }
  };

  beforeEach(
    async(() => {
      TestBed.configureTestingModule({
        declarations: [MyApp],
        imports: [IonicModule.forRoot(MyApp)],
        providers: [
          {provide: StatusBar, useFactory: () => StatusBarMock.instance()},
          {provide: SplashScreen, useFactory: () => SplashScreenMock.instance()},
          {provide: Platform, useFactory: () => PlatformMock.instance()},
          {provide: UserProvider, useFactory: () => userProvider}
        ]
      });
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(MyApp);
    component = fixture.componentInstance;
  });

  it('should be created', () => {
    expect(component instanceof MyApp).toBe(true);
  });

  it('should show login page', () => {
    expect(component.rootPage).toEqual('LoginPage');
  });
});
