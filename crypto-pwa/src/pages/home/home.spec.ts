import { IonicModule, NavController } from 'ionic-angular';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { HomePage } from './home';
import { HoldingsProvider } from '../../providers/holdings/holdings';
import { By } from '@angular/platform-browser';
import { UserProvider } from '../../providers/user/user';
import { Observable } from 'rxjs/Rx';

describe('HomePage', () => {
  let fixture: ComponentFixture<HomePage>;
  let component: HomePage;
  let userProvider = {
    getUser() {
      return Observable.of({name: "Cool User"});
    }
  };
  let holdingsProvider = {
    holdings: [{crypto: 'BTC', currency: 'USD', amount: 5, value: '10000'}],
    loadHoldings() {
      return this.holdings;
    }
  };
  let loadHoldings;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [HomePage],
      imports: [IonicModule.forRoot(HomePage)],
      providers: [NavController,
        {provide: UserProvider, useValue: userProvider},
        {provide: HoldingsProvider, useValue: holdingsProvider}
      ]
    });
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HomePage);
    component = fixture.componentInstance;
    loadHoldings = jest.spyOn(holdingsProvider, 'loadHoldings');
  });

  it('should be created', () => {
    expect(component).toBeDefined()
  });

  it('should call loadHoldings', () => {
    component.ionViewDidLoad();
    fixture.detectChanges();
    expect(loadHoldings).toHaveBeenCalled();
  });

  it('should show list of currencies', () => {
    component.ionViewDidLoad();
    fixture.detectChanges();
    const list: HTMLDivElement = fixture.debugElement.query(By.css('ion-list')).nativeElement;
    expect(list.innerHTML).toMatch(/ion-item/);
    const amount = fixture.debugElement.query(By.css('.amount')).nativeElement;
    expect(amount.innerHTML).toMatch(/<strong>Coins:<\/strong> 5 <strong>Value:<\/strong> 10000/)
  });
});
