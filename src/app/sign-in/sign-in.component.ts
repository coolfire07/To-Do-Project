import { Component, EventEmitter, Output } from '@angular/core';
import { ViewChild, ElementRef } from '@angular/core';
import { AuthService } from '../auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sign-in',
  standalone: false,
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.css'],
})
export class SignInComponent {
  @ViewChild('usernameInput', { static: false }) usernameInput!: ElementRef;
  @ViewChild('passwordInput', { static: false }) passwordInput!: ElementRef;
  loginForm!: FormGroup;

  @Output() loginSuccess: EventEmitter<void> = new EventEmitter();

  constructor(private authService: AuthService, private fb: FormBuilder, private router: Router) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  ngAfterViewInit(): void {
    console.log('Элементы инициализированы');
    if (this.usernameInput) {
      console.log('Username Input доступен');
    }
    if (this.passwordInput) {
      console.log('Password Input доступен');
    }
  }



  login() {
    console.log("login")
    const username = this.usernameInput.nativeElement.value;
    const password = this.passwordInput.nativeElement.value;
    console.log("Отправка запроса на авторизацию");

    console.log(username);
    console.log(password);
    this.authService.login(username, password).subscribe(
      response => {
        console.log('Вход выполнен успешно:', response);
        const token = response.token;
        localStorage.setItem('jwt-token', token);
        console.log('Token saved:', token);
        this.router.navigate(['/tasks']).then(success => {
          if (success) {
            this.loginSuccess.emit();
            console.log('Navigation to /tasks successful');
          } else {
            console.log('Navigation to /tasks failed');
          }
        });

      }
    );
  }

  @Output() toggle: EventEmitter<void> = new EventEmitter();

  toggleForm() {
    console.log('Переключение на регистрацию');
    this.router.navigate(['/register']).then(success => {
      if (success) {
        this.toggle.emit();
        console.log('Navigation to /register successful');
      } else {
        console.log('Navigation to /register failed');
      }
    });
  }
}
