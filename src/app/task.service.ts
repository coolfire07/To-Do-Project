import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Task } from './task.model';  // Импорт модели задачи

interface Filters {
  status?: string;
  date?: string;
  keyword?: string;
  sort?: string;
}

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private apiUrl = 'http://localhost:8080/api/tasks';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    console.log("Отправляемый токен:", token);

    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    });
  }

  getTasks(filters: Filters): Observable<Task[]> {
    let params = new HttpParams();

    if (filters.status) params = params.set('status', filters.status);
    if (filters.date) params = params.set('date', filters.date);
    if (filters.keyword) params = params.set('keyword', filters.keyword);

    if (filters.sort) {
      if (filters.sort === 'creationDate') {
        params = params.set('sortBy', 'creationDate');
      } else if (filters.sort === 'completionDate') {
        params = params.set('sortBy', 'completionDate');
      }
    }

    return this.http.get<Task[]>(this.apiUrl, { headers: this.getHeaders(), params }).pipe(
      catchError(this.handleError)
    );
  }


  getTask(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  addTask(task: Task): Observable<Task> {
    console.log("Отправляемый токен в addTask:", this.getHeaders().get('Authorization'));
    return this.http.post<Task>(this.apiUrl, task, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  updateTask(id: number, task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() }).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: any): Observable<never> {
    console.error('Error occurred:', error);
    return throwError(() => new Error('Something went wrong; please try again later.'));
  }
}
