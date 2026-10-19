import { Component, inject, signal, computed, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ProveedorService } from '../../services/proveedor.service';
import { Proveedor } from '../../models/proveedor.models';

type ToastTipo = 'success' | 'error';
type ConfirmAccion = 'eliminar' | 'reactivar';
type OrdenCampo = 'razonSocial' | 'ruc' | 'fecha';
type OrdenDireccion = 'asc' | 'desc';

interface Toast {
  tipo: ToastTipo;
  mensaje: string;
  accionLabel?: string;
  accionFn?: () => void;
}

interface Confirmacion {
  accion: ConfirmAccion;
  proveedor: Proveedor;
}

function rucValidator(control: AbstractControl): ValidationErrors | null {
  const valor = control.value;
  if (!valor) return null;
  const limpio = String(valor).trim();
  if (!/^\d{11}$/.test(limpio)) {
    return { rucInvalido: 'El RUC debe tener 11 dígitos numéricos' };
  }
  if (!/^(10|15|17|20)/.test(limpio)) {
    return { rucInvalido: 'El RUC debe empezar con 10, 15, 17 o 20' };
  }
  return null;
}

function telefonoValidator(control: AbstractControl): ValidationErrors | null {
  const valor = control.value;
  if (!valor) return null;
  const limpio = String(valor).trim();
  if (limpio === '') return null;
  if (!/^\d{6,15}$/.test(limpio)) {
    return { telefonoInvalido: 'El teléfono debe tener entre 6 y 15 dígitos' };
  }
  return null;
}

@Component({
  selector: 'app-proveedores',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './proveedores.component.html',
  styleUrl: './proveedores.component.css'
})
export class ProveedoresComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly proveedorService = inject(ProveedorService);

  readonly proveedores = signal<Proveedor[]>([]);
  readonly cargando = signal(false);
  readonly mostrarInactivos = signal(false);
  readonly formVisible = signal(false);
  readonly editandoId = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly busqueda = signal('');
  readonly toast = signal<Toast | null>(null);
  readonly confirmacion = signal<Confirmacion | null>(null);
  readonly procesando = signal(false);
  readonly buscandoRuc = signal(false);
  readonly rucError = signal<string | null>(null);
  readonly proveedorViendo = signal<Proveedor | null>(null);

  readonly ordenCampo = signal<OrdenCampo>('razonSocial');
  readonly ordenDireccion = signal<OrdenDireccion>('asc');

  private toastTimeout: any = null;
  private toastAccionTimeout: any = null;

  readonly tituloFormulario = computed(() =>
    this.editandoId() !== null ? 'Editar proveedor' : 'Nuevo proveedor'
  );

  readonly tituloConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    return c.accion === 'eliminar' ? 'Eliminar proveedor' : 'Reactivar proveedor';
  });

  readonly mensajeConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    if (c.accion === 'reactivar') {
      return `¿Deseas reactivar el proveedor "${c.proveedor.razonSocial}"? Volverá a estar disponible para asignar a productos y comprobantes.`;
    }
    return `¿Estás seguro de eliminar el proveedor "${c.proveedor.razonSocial}"? Podrás reactivarlo después. Los productos y comprobantes ya asociados mantienen la referencia.`;
  });

  readonly proveedoresFiltrados = computed(() => {
    const t = this.busqueda().trim().toLowerCase();
    let lista = this.proveedores();

    if (t) {
      lista = lista.filter((p) =>
        p.ruc.includes(t) ||
        p.razonSocial.toLowerCase().includes(t) ||
        (p.nombreComercial?.toLowerCase().includes(t) ?? false) ||
        (p.email?.toLowerCase().includes(t) ?? false)
      );
    }

    const campo = this.ordenCampo();
    const dir = this.ordenDireccion();
    const factor = dir === 'asc' ? 1 : -1;
    return [...lista].sort((a, b) => {
      let va: any;
      let vb: any;
      if (campo === 'razonSocial') {
        va = a.razonSocial.toLowerCase();
        vb = b.razonSocial.toLowerCase();
      } else if (campo === 'ruc') {
        va = a.ruc;
        vb = b.ruc;
      } else {
        va = a.fechaRegistro;
        vb = b.fechaRegistro;
      }
      if (va < vb) return -1 * factor;
      if (va > vb) return 1 * factor;
      return 0;
    });
  });

  readonly form = this.fb.nonNullable.group({
    ruc: ['', [Validators.required, rucValidator]],
    razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
    nombreComercial: ['', [Validators.maxLength(150)]],
    direccion: ['', [Validators.maxLength(200)]],
    telefono: ['', [telefonoValidator]],
    email: ['', [Validators.email, Validators.maxLength(150)]]
  });

  readonly rucLargo = () => this.form.controls.ruc.value.length;
  readonly razonLargo = () => this.form.controls.razonSocial.value.length;
  readonly nombreComercialLargo = () => this.form.controls.nombreComercial.value.length;
  readonly direccionLargo = () => this.form.controls.direccion.value.length;
  readonly telefonoLargo = () => this.form.controls.telefono.value.length;
  readonly emailLargo = () => this.form.controls.email.value.length;

  get ruc() { return this.form.controls.ruc; }
  get razonSocial() { return this.form.controls.razonSocial; }
  get nombreComercial() { return this.form.controls.nombreComercial; }
  get direccion() { return this.form.controls.direccion; }
  get telefono() { return this.form.controls.telefono; }
  get email() { return this.form.controls.email; }

  ngOnInit(): void {
    this.cargar();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.proveedorViendo()) {
      this.cerrarVista();
      return;
    }
    if (this.confirmacion()) {
      this.cancelarConfirmacion();
      return;
    }
    if (this.formVisible()) {
      this.cancelarFormulario();
    }
  }

  cargar(): void {
    this.cargando.set(true);
    const peticion = this.mostrarInactivos()
      ? this.proveedorService.listarTodos()
      : this.proveedorService.listarActivos();
    peticion.subscribe({
      next: (data) => {
        this.proveedores.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.mostrarToast('error', 'Error al cargar proveedores');
        this.cargando.set(false);
      }
    });
  }

  onBuscar(event: Event): void {
    this.busqueda.set((event.target as HTMLInputElement).value);
  }

  limpiarBusqueda(): void {
    this.busqueda.set('');
  }

  toggleInactivos(): void {
    this.mostrarInactivos.update((v) => !v);
    this.cargar();
  }

  ordenarPor(campo: OrdenCampo): void {
    if (this.ordenCampo() === campo) {
      this.ordenDireccion.update((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      this.ordenCampo.set(campo);
      this.ordenDireccion.set('asc');
    }
  }

  iconoOrden(campo: OrdenCampo): string {
    if (this.ordenCampo() !== campo) return '';
    return this.ordenDireccion() === 'asc' ? '↑' : '↓';
  }

  onRucInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const soloDigitos = input.value.replace(/\D/g, '').slice(0, 11);
    if (input.value !== soloDigitos) {
      input.value = soloDigitos;
      this.form.controls.ruc.setValue(soloDigitos, { emitEvent: false });
    }
  }

  onTelefonoInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const soloDigitos = input.value.replace(/\D/g, '').slice(0, 15);
    if (input.value !== soloDigitos) {
      input.value = soloDigitos;
      this.form.controls.telefono.setValue(soloDigitos, { emitEvent: false });
    }
  }

  buscarRuc(): void {
    const ruc = this.form.controls.ruc.value.trim();
    if (!/^\d{11}$/.test(ruc)) {
      this.rucError.set('El RUC debe tener 11 dígitos');
      return;
    }
    if (!/^(10|15|17|20)/.test(ruc)) {
      this.rucError.set('El RUC debe empezar con 10, 15, 17 o 20');
      return;
    }
    this.buscandoRuc.set(true);
    this.rucError.set(null);
    this.proveedorService.consultarRuc(ruc).subscribe({
      next: (info) => {
        this.buscandoRuc.set(false);
        this.form.patchValue({
          razonSocial: info.razonSocial ?? '',
          nombreComercial: info.nombreComercial ?? '',
          direccion: info.direccion ?? ''
        });
        if (info.estado && info.estado.toUpperCase() !== 'ACTIVO') {
          this.mostrarToast('error', `SUNAT indica que el RUC está ${info.estado}`);
        } else if (info.condicion && info.condicion.toUpperCase() !== 'HABIDO') {
          this.mostrarToast('error', `SUNAT indica que la condición es ${info.condicion}`);
        } else {
          this.mostrarToast('success', 'Datos del RUC cargados correctamente');
        }
      },
      error: (err) => {
        this.buscandoRuc.set(false);
        this.rucError.set(err.error?.message ?? 'No se pudo consultar el RUC');
      }
    });
  }

  verProveedor(p: Proveedor): void {
    this.proveedorViendo.set(p);
  }

  cerrarVista(): void {
    this.proveedorViendo.set(null);
  }

  abrirFormularioNuevo(): void {
    this.editandoId.set(null);
    this.form.reset({
      ruc: '',
      razonSocial: '',
      nombreComercial: '',
      direccion: '',
      telefono: '',
      email: ''
    });
    this.rucError.set(null);
    this.formVisible.set(true);
  }

  abrirFormularioEditar(p: Proveedor): void {
    this.editandoId.set(p.id);
    this.form.reset({
      ruc: p.ruc,
      razonSocial: p.razonSocial,
      nombreComercial: p.nombreComercial ?? '',
      direccion: p.direccion ?? '',
      telefono: p.telefono ?? '',
      email: p.email ?? ''
    });
    this.rucError.set(null);
    this.formVisible.set(true);
  }

  cancelarFormulario(): void {
    this.formVisible.set(false);
    this.editandoId.set(null);
    this.rucError.set(null);
  }

  campoValido(control: 'ruc' | 'razonSocial' | 'nombreComercial' | 'direccion' | 'telefono' | 'email'): boolean {
    const c = this.form.controls[control];
    return c.value.trim().length > 0 && c.valid;
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.guardando()) return;
    this.guardando.set(true);
    const raw = this.form.getRawValue();
    const request = {
      ruc: raw.ruc.trim(),
      razonSocial: raw.razonSocial.trim(),
      nombreComercial: raw.nombreComercial?.trim() || null,
      direccion: raw.direccion?.trim() || null,
      telefono: raw.telefono?.trim() || null,
      email: raw.email?.trim() || null
    };
    const id = this.editandoId();
    if (id !== null) {
      this.proveedorService.actualizar(id, request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Proveedor actualizado correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al actualizar');
        }
      });
    } else {
      this.proveedorService.crear(request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Proveedor creado correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al crear');
        }
      });
    }
  }

  pedirEliminar(p: Proveedor): void {
    this.confirmacion.set({ accion: 'eliminar', proveedor: p });
  }

  pedirReactivar(p: Proveedor): void {
    this.confirmacion.set({ accion: 'reactivar', proveedor: p });
  }

  cancelarConfirmacion(): void {
    this.confirmacion.set(null);
  }

  confirmarAccion(): void {
    const c = this.confirmacion();
    if (!c) return;
    this.procesando.set(true);

    if (c.accion === 'eliminar') {
      this.proveedorService.desactivar(c.proveedor.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast(
            'success',
            `Proveedor ${c.proveedor.razonSocial} eliminado`,
            'Deshacer',
            () => this.reactivarSilencioso(c.proveedor.id, c.proveedor.razonSocial)
          );
        },
        error: (err) => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.mostrarToast('error', err.error?.message ?? 'Error al eliminar');
        }
      });
    } else {
      this.proveedorService.reactivar(c.proveedor.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast('success', 'Proveedor reactivado correctamente');
        },
        error: (err) => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.mostrarToast('error', err.error?.message ?? 'Error al reactivar');
        }
      });
    }
  }

  private reactivarSilencioso(id: number, razon: string): void {
    this.proveedorService.reactivar(id).subscribe({
      next: () => {
        this.cargar();
        this.mostrarToast('success', `Proveedor ${razon} restaurado`);
      },
      error: () => {
        this.mostrarToast('error', 'No se pudo restaurar el proveedor');
      }
    });
  }

  private mostrarToast(tipo: ToastTipo, mensaje: string, accionLabel?: string, accionFn?: () => void): void {
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
    if (this.toastAccionTimeout) clearTimeout(this.toastAccionTimeout);

    this.toast.set({ tipo, mensaje, accionLabel, accionFn });

    if (accionLabel && accionFn) {
      this.toastAccionTimeout = setTimeout(() => this.toast.set(null), 6000);
    } else {
      this.toastTimeout = setTimeout(() => this.toast.set(null), 3000);
    }
  }

  ejecutarAccionToast(): void {
    const t = this.toast();
    if (!t || !t.accionFn) return;
    const fn = t.accionFn;
    this.toast.set(null);
    fn();
  }

  cerrarToast(): void {
    if (this.toastTimeout) clearTimeout(this.toastTimeout);
    if (this.toastAccionTimeout) clearTimeout(this.toastAccionTimeout);
    this.toast.set(null);
  }

  trackById(_: number, item: Proveedor): number {
    return item.id;
  }
}
