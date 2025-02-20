import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: false,
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  showLoginForm: boolean = true;
  isLoggedIn: boolean = false;

  toggleForms() {
    this.showLoginForm = !this.showLoginForm;
  }

  onLoginSuccess() {
    this.isLoggedIn = true;
    this.showLoginForm = false;
  }
}
