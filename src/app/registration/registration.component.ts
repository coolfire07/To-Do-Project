import {Component, ElementRef, EventEmitter, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-registration',
  standalone: false,
  templateUrl: './registration.component.html',
  styleUrl: './registration.component.css'
})
export class RegistrationComponent {
  @ViewChild('usernameInput', { static: false }) usernameInput!: ElementRef;
  @ViewChild('passwordInput', { static: false }) passwordInput!: ElementRef;
  @ViewChild('confirmPasswordInput', { static: false }) confirmPasswordInput!: ElementRef;
  registrationForm!: FormGroup;

  @Output() registrationSuccess: EventEmitter<void> = new EventEmitter();

  @Output() toggle: EventEmitter<void> = new EventEmitter();

  constructor(private authService:AuthService, private fb:FormBuilder, private router: Router) {
    this.registrationForm = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      confirmPassword: ['', [Validators.required]]
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
    if (this.confirmPasswordInput) {
      console.log('Confirm Password Input доступен');
    }
  }

  toggleForm() {
    console.log('Переключение на регистрацию');
    this.router.navigate(['/login']).then(success => {
      if (success) {
        this.toggle.emit();
        console.log('Navigation to /login successful');
      } else {
        console.log('Navigation to /login failed');
      }
    });
  }

  register(){
    console.log("registration")
    const username = this.usernameInput.nativeElement.value;
    const password = this.passwordInput.nativeElement.value;
    const confirmPassword = this.confirmPasswordInput.nativeElement.value;
    console.log("Отправка запроса на регистрацию");

    console.log(username);
    console.log(password);
    console.log(confirmPassword);

    this.authService.register(username, password, confirmPassword).subscribe(
      response => {
        console.log('Решистрация выполнена успешно:', response);
        this.router.navigate(['/login']).then(success => {
          if (success) {
            this.registrationSuccess.emit();
            console.log('Navigation to /login successful');
          } else {
            console.log('Navigation to /login failed');
          }
        });

      }
    );
  }
}
