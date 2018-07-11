import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable()
export class UserProvider {

  constructor(public http: HttpClient) {
  }

  login() {
    let port = (location.port ? ':' + location.port : '');
    if (port === ':8100') {
      port = ':8080';
    }
    location.href = '//' + location.hostname + port + '/login';
  }

  getUser() {
    return this.http.get('/api/user');
  }

  logout() {
    return this.http.post('/api/logout', {});
  }
}
