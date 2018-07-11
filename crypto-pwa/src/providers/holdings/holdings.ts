import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { forkJoin } from 'rxjs/observable/forkJoin';
import { timeoutWith } from 'rxjs/operators';
import 'rxjs/add/observable/throw';

interface Holding {
  crypto: string,
  currency: string,
  amount: number,
  value?: number
}

@Injectable()
export class HoldingsProvider {

  public HOLDINGS_API = '/api/holdings';
  public holdings: Holding[] = [];
  public pricesUnavailable: boolean = false;

  constructor(private http: HttpClient) {
  }

  addHolding(holding: Holding): void {
    this.holdings.push(holding);
    this.fetchPrices();
    this.saveHoldings();
  }

  removeHolding(holding): void {
    this.holdings.splice(this.holdings.indexOf(holding), 1);
    this.fetchPrices();
    this.saveHoldings();
  }

  onError(error): void {
    console.error('ERROR: ', error);
  }

  saveHoldings(): void {
    this.http.post(this.HOLDINGS_API, this.holdings).subscribe(data => {
      console.log('holdings', data);
    }, this.onError);
  }

  loadHoldings(): void {
    this.http.get(this.HOLDINGS_API).subscribe((holdings: Holding[]) => {
      if (holdings !== null) {
        this.holdings = holdings;
        this.fetchPrices();
      }
    }, this.onError);
  }

  verifyHolding(holding): Observable<any> {
    return this.http.get('https://api.cryptonator.com/api/ticker/' + holding.crypto + '-' + holding.currency).pipe(
      timeoutWith(5000, Observable.throw(new Error('Failed to verify holding.')))
    );
  }

  fetchPrices(refresher?): void {

    this.pricesUnavailable = false;
    let requests = [];

    for (let holding of this.holdings) {
      let request = this.http.get('https://api.cryptonator.com/api/ticker/' + holding.crypto + '-' + holding.currency);
      requests.push(request);
    }

    forkJoin(requests).pipe(
      timeoutWith(5000, Observable.throw(new Error('Failed to fetch prices.')))
    ).subscribe(results => {

      results.forEach((result: any, index) => {
        this.holdings[index].value = result.ticker.price;
      });

      if (typeof(refresher) !== 'undefined') {
        refresher.complete();
      }

    }, err => {

      this.pricesUnavailable = true;
      if (typeof(refresher) !== 'undefined') {
        refresher.complete();
      }
    });
  }
}
