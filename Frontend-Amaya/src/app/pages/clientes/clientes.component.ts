import { Component, inject, signal, computed, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ClienteService } from '../../services/cliente.service';
import { Cliente, TipoDocumento } from '../../models/cliente.models';

type ToastTipo = 'success' | 'error';
type ConfirmAccion = 'eliminar' | 'reactivar';
type FiltroTipo = 'TODOS' | 'PERSONA' | 'EMPRESA';
type OrdenCampo = 'documento' | 'nombre' | 'fecha';
type OrdenDireccion = 'asc' | 'desc';

interface Toast {
  tipo: ToastTipo;
  mensaje: string;
  accionLabel?: string;
  accionFn?: () => void;
}

interface Confirmacion {
  accion: ConfirmAccion;
  cliente: Cliente;
}

function dniValidator(control: AbstractControl): ValidationErrors | null {
  const valor = control.value;
  if (!valor) return null;
  const limpio = String(valor).trim();
  if (!/^\d{8}$/.test(limpio)) {
    return { dniInvalido: 'El DNI debe tener 8 dígitos numéricos' };
  }
  return null;
}

function rucValidator(control: AbstractControl): ValidationErrors | null {
  const valor = control.value;
  if (!valor) return null;
  const limpio = String(valor).trim();
  if (!/^\d{11}$/.test(limpio)) {
    return { rucInvalido: 'El RUC debe tener 11 dígitos numéricos' };
  }
  if (!/^(10|20)/.test(limpio)) {
    return { rucInvalido: 'El RUC debe empezar con 10 o 20' };
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
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.css'
})
export class ClientesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);

  readonly clientes = signal<Cliente[]>([]);
  readonly cargando = signal(false);
  readonly mostrarInactivos = signal(false);
  readonly formVisible = signal(false);
  readonly editandoId = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly busqueda = signal('');
  readonly toast = signal<Toast | null>(null);
  readonly confirmacion = signal<Confirmacion | null>(null);
  readonly procesando = signal(false);
  readonly buscandoDni = signal(false);
  readonly buscandoRuc = signal(false);
  readonly dniError = signal<string | null>(null);
  readonly rucError = signal<string | null>(null);
  readonly clienteViendo = signal<Cliente | null>(null);
  readonly filtroTipo = signal<FiltroTipo>('TODOS');
  readonly ordenCampo = signal<OrdenCampo>('documento');
  readonly ordenDireccion = signal<OrdenDireccion>('asc');

  // El tipo de cliente determina el formulario (Persona = DNI, Empresa = RUC)
  readonly tipoFormulario = signal<TipoDocumento>('DNI');

  private toastTimeout: any = null;
  private toastAccionTimeout: any = null;

  readonly tituloFormulario = computed(() =>
    this.editandoId() !== null ? 'Editar cliente' : 'Nuevo cliente'
  );

  readonly tituloConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    return c.accion === 'eliminar' ? 'Eliminar cliente' : 'Reactivar cliente';
  });

  readonly mensajeConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    if (c.accion === 'reactivar') {
      return `¿Deseas reactivar el cliente "${c.cliente.nombreMostrar}"? Volverá a estar disponible.`;
    }
    return `¿Estás seguro de eliminar el cliente "${c.cliente.nombreMostrar}"? Podrás reactivarlo después.`;
  });

  readonly conteos = computed(() => {
    const lista = this.clientes();
    return {
      todos: lista.length,
      personas: lista.filter(c => c.tipoDocumento === 'DNI').length,
      empresas: lista.filter(c => c.tipoDocumento === 'RUC').length
    };
  });

  readonly clientesFiltrados = computed(() => {
    const t = this.busqueda().trim().toLowerCase();
    const filtro = this.filtroTipo();
    let lista = this.clientes();

    if (t) {
      lista = lista.filter((c) =>
        c.numeroDocumento.toLowerCase().includes(t) ||
        c.nombreMostrar.toLowerCase().includes(t) ||
        (c.razonSocial?.toLowerCase().includes(t) ?? false) ||
        (c.nombreComercial?.toLowerCase().includes(t) ?? false) ||
        (c.email?.toLowerCase().includes(t) ?? false)
      );
    }

    if (filtro === 'PERSONA') {
      lista = lista.filter(c => c.tipoDocumento === 'DNI');
    } else if (filtro === 'EMPRESA') {
      lista = lista.filter(c => c.tipoDocumento === 'RUC');
    }

    const campo = this.ordenCampo();
    const dir = this.ordenDireccion();
    const factor = dir === 'asc' ? 1 : -1;

    return [...lista].sort((a, b) => {
      let va: any;
      let vb: any;
      if (campo === 'documento') {
        va = a.numeroDocumento;
        vb = b.numeroDocumento;
      } else if (campo === 'nombre') {
        va = a.nombreMostrar.toLowerCase();
        vb = b.nombreMostrar.toLowerCase();
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
    numeroDocumento: ['', [Validators.required]],
    nombres: ['', [Validators.maxLength(100)]],
    apellidoPaterno: ['', [Validators.maxLength(100)]],
    apellidoMaterno: ['', [Validators.maxLength(100)]],
    razonSocial: ['', [Validators.maxLength(200)]],
    nombreComercial: ['', [Validators.maxLength(200)]],
    direccion: ['', [Validators.maxLength(250)]],
    telefono: ['', [telefonoValidator]],
    email: ['', [Validators.email, Validators.maxLength(150)]]
  });

  get numeroDocumento() { return this.form.controls.numeroDocumento; }
  get nombres() { return this.form.controls.nombres; }
  get apellidoPaterno() { return this.form.controls.apellidoPaterno; }
  get apellidoMaterno() { return this.form.controls.apellidoMaterno; }
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
    if (this.clienteViendo()) {
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
      ? this.clienteService.listarTodos()
      : this.clienteService.listarActivos();
    peticion.subscribe({
      next: (data) => {
        this.clientes.set(data ?? []);
        this.cargando.set(false);
      },
      error: () => {
        this.mostrarToast('error', 'Error al cargar clientes');
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
    this.mostrarInactivos.update(v => !v);
    this.cargar();
  }

  cambiarFiltro(f: FiltroTipo): void {
    this.filtroTipo.set(f);
  }

  ordenarPor(campo: OrdenCampo): void {
    if (this.ordenCampo() === campo) {
      this.ordenDireccion.update(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      this.ordenCampo.set(campo);
      this.ordenDireccion.set('asc');
    }
  }

  iconoOrden(campo: OrdenCampo): string {
    if (this.ordenCampo() !== campo) return '';
    return this.ordenDireccion() === 'asc' ? '↑' : '↓';
  }

  // ---------- Formulario ----------

  private aplicarValidadoresSegunTipo(): void {
    const tipo = this.tipoFormulario();
    const num = this.form.controls.numeroDocumento;

    if (tipo === 'DNI') {
      num.setValidators([Validators.required, dniValidator]);
    } else {
      num.setValidators([Validators.required, rucValidator]);
    }
    num.updateValueAndValidity({ emitEvent: false });

    if (tipo === 'DNI') {
      this.form.controls.nombres.setValidators([Validators.required, Validators.maxLength(100)]);
      this.form.controls.apellidoPaterno.setValidators([Validators.required, Validators.maxLength(100)]);
      this.form.controls.apellidoMaterno.setValidators([Validators.required, Validators.maxLength(100)]);
      this.form.controls.razonSocial.clearValidators();
      this.form.controls.nombreComercial.clearValidators();
    } else {
      this.form.controls.nombres.clearValidators();
      this.form.controls.apellidoPaterno.clearValidators();
      this.form.controls.apellidoMaterno.clearValidators();
      this.form.controls.razonSocial.setValidators([Validators.required, Validators.maxLength(200)]);
      this.form.controls.nombreComercial.setValidators([Validators.maxLength(200)]);
    }

    this.form.controls.nombres.updateValueAndValidity({ emitEvent: false });
    this.form.controls.apellidoPaterno.updateValueAndValidity({ emitEvent: false });
    this.form.controls.apellidoMaterno.updateValueAndValidity({ emitEvent: false });
    this.form.controls.razonSocial.updateValueAndValidity({ emitEvent: false });
    this.form.controls.nombreComercial.updateValueAndValidity({ emitEvent: false });
  }

  cambiarTipoFormulario(t: TipoDocumento): void {
    if (this.tipoFormulario() === t) return;
    this.tipoFormulario.set(t);
    this.dniError.set(null);
    this.rucError.set(null);
    this.aplicarValidadoresSegunTipo();
    // Limpiar los campos que no aplican al nuevo tipo
    if (t === 'DNI') {
      this.form.controls.razonSocial.setValue('');
      this.form.controls.nombreComercial.setValue('');
    } else {
      this.form.controls.nombres.setValue('');
      this.form.controls.apellidoPaterno.setValue('');
      this.form.controls.apellidoMaterno.setValue('');
    }
    this.form.controls.numeroDocumento.setValue('');
  }

  onNumeroDocumentoInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const maxLen = this.tipoFormulario() === 'DNI' ? 8 : 11;
    const soloDigitos = input.value.replace(/\D/g, '').slice(0, maxLen);
    if (input.value !== soloDigitos) {
      input.value = soloDigitos;
      this.form.controls.numeroDocumento.setValue(soloDigitos, { emitEvent: false });
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

  consultarDni(): void {
    const dni = this.form.controls.numeroDocumento.value.trim();
    if (!/^\d{8}$/.test(dni)) {
      this.dniError.set('El DNI debe tener 8 dígitos');
      return;
    }
    this.buscandoDni.set(true);
    this.dniError.set(null);
    this.clienteService.consultarDni(dni).subscribe({
      next: (info) => {
        this.buscandoDni.set(false);
        this.form.patchValue({
          nombres: info.nombres ?? '',
          apellidoPaterno: info.apellidoPaterno ?? '',
          apellidoMaterno: info.apellidoMaterno ?? ''
        });
        this.mostrarToast('success', 'Datos del DNI cargados correctamente');
      },
      error: (err) => {
        this.buscandoDni.set(false);
        this.dniError.set(err.error?.message ?? 'No se pudo consultar el DNI');
      }
    });
  }

  consultarRuc(): void {
    const ruc = this.form.controls.numeroDocumento.value.trim();
    if (!/^\d{11}$/.test(ruc)) {
      this.rucError.set('El RUC debe tener 11 dígitos');
      return;
    }
    if (!/^(10|20)/.test(ruc)) {
      this.rucError.set('El RUC debe empezar con 10 o 20');
      return;
    }
    this.buscandoRuc.set(true);
    this.rucError.set(null);
    this.clienteService.consultarRuc(ruc).subscribe({
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

  abrirFormularioNuevo(): void {
    this.editandoId.set(null);
    this.tipoFormulario.set('DNI');
    this.form.reset({
      numeroDocumento: '',
      nombres: '',
      apellidoPaterno: '',
      apellidoMaterno: '',
      razonSocial: '',
      nombreComercial: '',
      direccion: '',
      telefono: '',
      email: ''
    });
    this.dniError.set(null);
    this.rucError.set(null);
    this.aplicarValidadoresSegunTipo();
    this.formVisible.set(true);
  }

  abrirFormularioEditar(c: Cliente): void {
    this.editandoId.set(c.id);
    this.tipoFormulario.set(c.tipoDocumento);
    this.form.reset({
      numeroDocumento: c.numeroDocumento,
      nombres: c.nombres ?? '',
      apellidoPaterno: c.apellidoPaterno ?? '',
      apellidoMaterno: c.apellidoMaterno ?? '',
      razonSocial: c.razonSocial ?? '',
      nombreComercial: c.nombreComercial ?? '',
      direccion: c.direccion ?? '',
      telefono: c.telefono ?? '',
      email: c.email ?? ''
    });
    this.dniError.set(null);
    this.rucError.set(null);
    this.aplicarValidadoresSegunTipo();
    this.formVisible.set(true);
  }

  cancelarFormulario(): void {
    this.formVisible.set(false);
    this.editandoId.set(null);
    this.dniError.set(null);
    this.rucError.set(null);
  }

  campoValido(control: 'numeroDocumento' | 'nombres' | 'apellidoPaterno' | 'apellidoMaterno' | 'razonSocial' | 'nombreComercial' | 'direccion' | 'telefono' | 'email'): boolean {
    const c = this.form.controls[control];
    return String(c.value).trim().length > 0 && c.valid;
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.guardando()) return;
    this.guardando.set(true);

    const raw = this.form.getRawValue();
    const tipo = this.tipoFormulario();

    const request = {
      tipoDocumento: tipo,
      numeroDocumento: raw.numeroDocumento.trim(),
      nombres: tipo === 'DNI' ? (raw.nombres?.trim() || null) : null,
      apellidoPaterno: tipo === 'DNI' ? (raw.apellidoPaterno?.trim() || null) : null,
      apellidoMaterno: tipo === 'DNI' ? (raw.apellidoMaterno?.trim() || null) : null,
      razonSocial: tipo === 'RUC' ? (raw.razonSocial?.trim() || null) : null,
      nombreComercial: tipo === 'RUC' ? (raw.nombreComercial?.trim() || null) : null,
      direccion: raw.direccion?.trim() || null,
      telefono: raw.telefono?.trim() || null,
      email: raw.email?.trim() || null
    };

    const id = this.editandoId();
    if (id !== null) {
      this.clienteService.actualizar(id, request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Cliente actualizado correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al actualizar');
        }
      });
    } else {
      this.clienteService.crear(request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Cliente creado correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al crear');
        }
      });
    }
  }

  verCliente(c: Cliente): void {
    this.clienteViendo.set(c);
  }

  cerrarVista(): void {
    this.clienteViendo.set(null);
  }

  pedirEliminar(c: Cliente): void {
    this.confirmacion.set({ accion: 'eliminar', cliente: c });
  }

  pedirReactivar(c: Cliente): void {
    this.confirmacion.set({ accion: 'reactivar', cliente: c });
  }

  cancelarConfirmacion(): void {
    this.confirmacion.set(null);
  }

  confirmarAccion(): void {
    const c = this.confirmacion();
    if (!c) return;
    this.procesando.set(true);
    if (c.accion === 'eliminar') {
      this.clienteService.desactivar(c.cliente.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast(
            'success',
            `Cliente ${c.cliente.nombreMostrar} eliminado`,
            'Deshacer',
            () => this.reactivarSilencioso(c.cliente.id, c.cliente.nombreMostrar)
          );
        },
        error: (err) => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.mostrarToast('error', err.error?.message ?? 'Error al eliminar');
        }
      });
    } else {
      this.clienteService.reactivar(c.cliente.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast('success', 'Cliente reactivado correctamente');
        },
        error: (err) => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.mostrarToast('error', err.error?.message ?? 'Error al reactivar');
        }
      });
    }
  }

  private reactivarSilencioso(id: number, nombre: string): void {
    this.clienteService.reactivar(id).subscribe({
      next: () => {
        this.cargar();
        this.mostrarToast('success', `Cliente ${nombre} restaurado`);
      },
      error: () => this.mostrarToast('error', 'No se pudo restaurar el cliente')
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

  trackById(_: number, item: Cliente): number {
    return item.id;
  }
}