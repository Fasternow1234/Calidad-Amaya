import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { UsuarioService } from '../../services/usuario.service';
import { Rol, Usuario } from '../../models/usuario.models';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirm = control.get('passwordConfirm')?.value;
  if (password !== confirm) {
    return { mismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);

  readonly usuarios = signal<Usuario[]>([]);
  readonly roles = signal<Rol[]>([]);
  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly formularioVisible = signal(false);
  readonly editando = signal<Usuario | null>(null);
  readonly mostrarInactivos = signal(false);
  readonly mensaje = signal<string | null>(null);
  readonly error = signal<string | null>(null);

  // Signals para el ojito
  readonly showPassword = signal(false);
  readonly showPasswordConfirm = signal(false);

  readonly form = this.fb.nonNullable.group({
    nombreEmpleado: ['', [Validators.required, Validators.maxLength(100)]],
    nombreUsuario: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    idRol: [0, [Validators.required, Validators.min(1)]],
    password: ['', [Validators.minLength(8), Validators.maxLength(72)]],
    passwordConfirm: ['', [Validators.minLength(8), Validators.maxLength(72)]]
  }, { validators: passwordMatchValidator });

  ngOnInit(): void {
    this.cargar();
  }

  get usuariosVisibles(): Usuario[] {
    return this.usuarios().filter(u => this.mostrarInactivos() || u.estado === 1);
  }

  cargar(): void {
    this.cargando.set(true);
    this.usuarioService.listarRoles().subscribe({
      next: roles => {
        this.roles.set(roles);
        this.usuarioService.listar().subscribe({
          next: usuarios => this.usuarios.set(usuarios),
          error: err => {
            this.mostrarError(err);
            this.cargando.set(false);
          },
          complete: () => this.cargando.set(false)
        });
      },
      error: err => {
        this.mostrarError(err);
        this.cargando.set(false);
      }
    });
  }

  nuevo(): void {
    this.editando.set(null);
    this.mensaje.set(null);
    this.error.set(null);
    this.showPassword.set(false);
    this.showPasswordConfirm.set(false);
    this.form.reset({ nombreEmpleado: '', nombreUsuario: '', email: '', idRol: 0, password: '', passwordConfirm: '' });
    this.form.controls.password.setValidators([Validators.required, Validators.minLength(8), Validators.maxLength(72)]);
    this.form.controls.passwordConfirm.setValidators([Validators.required, Validators.minLength(8), Validators.maxLength(72)]);
    this.form.controls.password.updateValueAndValidity();
    this.form.controls.passwordConfirm.updateValueAndValidity();
    this.formularioVisible.set(true);
  }

  editar(usuario: Usuario): void {
    this.editando.set(usuario);
    this.mensaje.set(null);
    this.error.set(null);
    this.showPassword.set(false);
    this.showPasswordConfirm.set(false);
    this.form.reset({
      nombreEmpleado: usuario.nombreEmpleado,
      nombreUsuario: usuario.nombreUsuario,
      email: usuario.email ?? '',
      idRol: usuario.idRol,
      password: '',
      passwordConfirm: ''
    });
    this.form.controls.password.setValidators([Validators.minLength(8), Validators.maxLength(72)]);
    this.form.controls.passwordConfirm.setValidators([Validators.minLength(8), Validators.maxLength(72)]);
    this.form.controls.password.updateValueAndValidity();
    this.form.controls.passwordConfirm.updateValueAndValidity();
    this.formularioVisible.set(true);
  }

  cancelar(): void {
    this.formularioVisible.set(false);
    this.editando.set(null);
    this.error.set(null);
  }

  togglePassword(): void {
    this.showPassword.update(v => !v);
  }

  togglePasswordConfirm(): void {
    this.showPasswordConfirm.update(v => !v);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    
    // Validación extra manual por si acaso (aunque el validador de grupo ya lo hace)
    if (v.password !== v.passwordConfirm) {
      this.error.set('Las contraseñas no coinciden.');
      return;
    }

    const request = {
      nombreUsuario: v.nombreUsuario,
      nombreEmpleado: v.nombreEmpleado,
      email: v.email.trim() || null,
      idRol: Number(v.idRol),
      password: v.password || null
    };

    this.guardando.set(true);
    this.error.set(null);
    const usuario = this.editando();
    
    const peticion = usuario
      ? this.usuarioService.actualizar(usuario.id, request)
      : this.usuarioService.crear(request);
      
    peticion.subscribe({
      next: resultado => {
        this.usuarios.update(lista => {
          const actualizada = lista.filter(u => u.id !== resultado.id);
          return [...actualizada, resultado].sort((a, b) => a.nombreEmpleado.localeCompare(b.nombreEmpleado));
        });
        this.mensaje.set(usuario ? 'Usuario actualizado correctamente.' : 'Usuario creado correctamente.');
        this.cancelar();
      },
      error: err => this.mostrarError(err),
      complete: () => this.guardando.set(false)
    });
  }

  cambiarEstado(usuario: Usuario): void {
    const nuevoEstado = usuario.estado === 1 ? 0 : 1;
    const accion = nuevoEstado === 1 ? 'reactivar' : 'desactivar';
    if (!confirm(`¿Deseas ${accion} la cuenta de ${usuario.nombreEmpleado}?`)) return;
    this.usuarioService.cambiarEstado(usuario.id, nuevoEstado).subscribe({
      next: actualizado => {
        this.usuarios.update(lista => lista.map(u => u.id === actualizado.id ? actualizado : u));
        this.mensaje.set(`Cuenta ${nuevoEstado === 1 ? 'reactivada' : 'desactivada'} correctamente.`);
      },
      error: err => this.mostrarError(err)
    });
  }

  private mostrarError(err: HttpErrorResponse): void {
    this.error.set(err.error?.message ?? 'No se pudo completar la operación. Revisa los datos e inténtalo nuevamente.');
    this.guardando.set(false);
  }
}