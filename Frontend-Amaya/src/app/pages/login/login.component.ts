import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly showPassword = signal(false);

  readonly form = this.fb.nonNullable.group({
    nombreUsuario: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  get nombreUsuario() {
    return this.form.controls.nombreUsuario;
  }

  get password() {
    return this.form.controls.password;
  }

  ngOnInit(): void {
    if (this.authService.isAuthenticated() && !this.authService.isTokenExpired()) {
      this.router.navigateByUrl('/home');
    }
  }

  togglePassword(): void {
    this.showPassword.update((v) => !v);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/home'),
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message ?? 'Usuario o contraseña incorrectos');
      },
      complete: () => this.loading.set(false)
    });
  }
}