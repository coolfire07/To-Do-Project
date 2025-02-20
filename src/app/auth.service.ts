import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import { Router} from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient, private router: Router) { }

  register(username: string, password: string, confirmPassword: string): Observable<any> {
    const user = { username, password, confirmPassword};
    return this.http.post(`${this.apiUrl}/register`, user)
  }

  login(username: string, password: string): Observable<any> {
    const loginDetails = { username, password};
    return this.http.post(`${this.apiUrl}/login`, loginDetails).pipe(
      tap((response: any) => {
        if (response.token) {
          localStorage.setItem('token', response.token);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }

  isAutenticated(): boolean {
    return localStorage.getItem('token') !== null;
  }
}
