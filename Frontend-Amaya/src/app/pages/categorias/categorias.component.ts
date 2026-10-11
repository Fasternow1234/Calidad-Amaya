import { Component, inject, signal, computed, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoriaService } from '../../services/categoria.service';
import { Categoria } from '../../models/categoria.models';

type ToastTipo = 'success' | 'error';
type ConfirmAccion = 'eliminar' | 'reactivar';
type OrdenCampo = 'nombre' | 'fecha';
type OrdenDireccion = 'asc' | 'desc';

interface Toast {
  tipo: ToastTipo;
  mensaje: string;
  accionLabel?: string;
  accionFn?: () => void;
}

interface Confirmacion {
  accion: ConfirmAccion;
  categoria: Categoria;
}

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './categorias.component.html',
  styleUrl: './categorias.component.css'
})
export class CategoriasComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly categoriaService = inject(CategoriaService);

  readonly categorias = signal<Categoria[]>([]);
  readonly cargando = signal(false);
  readonly mostrarInactivas = signal(false);
  readonly formVisible = signal(false);
  readonly editandoId = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly busqueda = signal('');
  readonly toast = signal<Toast | null>(null);
  readonly confirmacion = signal<Confirmacion | null>(null);
  readonly procesando = signal(false);

  readonly ordenCampo = signal<OrdenCampo>('nombre');
  readonly ordenDireccion = signal<OrdenDireccion>('asc');

  private toastTimeout: any = null;
  private toastAccionTimeout: any = null;

  readonly tituloFormulario = computed(() =>
    this.editandoId() !== null ? 'Editar categoría' : 'Nueva categoría'
  );

  readonly tituloConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    return c.accion === 'eliminar' ? 'Eliminar categoría' : 'Reactivar categoría';
  });

  readonly mensajeConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    if (c.accion === 'reactivar') {
      return `¿Deseas reactivar la categoría "${c.categoria.nombre}"? Volverá a estar disponible para asignar a productos.`;
    }
    return `¿Estás seguro de eliminar la categoría "${c.categoria.nombre}"? Podrás reactivarla después. Los productos que ya la tienen asignada mantienen la referencia.`;
  });

  readonly categoriasFiltradas = computed(() => {
    const t = this.busqueda().trim().toLowerCase();
    let lista = this.categorias();

    if (t) {
      lista = lista.filter((c) => c.nombre.toLowerCase().includes(t));
    }

    const campo = this.ordenCampo();
    const dir = this.ordenDireccion();
    const factor = dir === 'asc' ? 1 : -1;
    return [...lista].sort((a, b) => {
      let va: any;
      let vb: any;
      if (campo === 'nombre') {
        va = a.nombre.toLowerCase();
        vb = b.nombre.toLowerCase();
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
    nombre: ['', [Validators.required, Validators.maxLength(100)]]
  });

  readonly nombreLargo = computed(() => this.form.controls.nombre.value.length);

  get nombre() { return this.form.controls.nombre; }

  ngOnInit(): void {
    this.cargar();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
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
    const peticion = this.mostrarInactivas()
      ? this.categoriaService.listarTodas()
      : this.categoriaService.listarActivas();
    peticion.subscribe({
      next: (data) => {
        this.categorias.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.mostrarToast('error', 'Error al cargar categorías');
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

  toggleInactivas(): void {
    this.mostrarInactivas.update((v) => !v);
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

  abrirFormularioNuevo(): void {
    this.editandoId.set(null);
    this.form.reset({ nombre: '' });
    this.formVisible.set(true);
  }

  abrirFormularioEditar(categoria: Categoria): void {
    this.editandoId.set(categoria.id);
    this.form.reset({ nombre: categoria.nombre });
    this.formVisible.set(true);
  }

  cancelarFormulario(): void {
    this.formVisible.set(false);
    this.editandoId.set(null);
    this.form.reset({ nombre: '' });
  }

  campoValido(): boolean {
    const c = this.form.controls.nombre;
    return c.value.trim().length > 0 && c.valid;
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.guardando()) return;
    this.guardando.set(true);
    const request = { nombre: this.form.controls.nombre.value.trim() };
    const id = this.editandoId();

    if (id !== null) {
      this.categoriaService.actualizar(id, request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Categoría actualizada correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al actualizar');
        }
      });
    } else {
      this.categoriaService.crear(request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Categoría creada correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al crear');
        }
      });
    }
  }

  pedirEliminar(c: Categoria): void {
    this.confirmacion.set({ accion: 'eliminar', categoria: c });
  }

  pedirReactivar(c: Categoria): void {
    this.confirmacion.set({ accion: 'reactivar', categoria: c });
  }

  cancelarConfirmacion(): void {
    this.confirmacion.set(null);
  }

  confirmarAccion(): void {
    const c = this.confirmacion();
    if (!c) return;
    this.procesando.set(true);

    if (c.accion === 'eliminar') {
      this.categoriaService.desactivar(c.categoria.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast(
            'success',
            `Categoría ${c.categoria.nombre} eliminada`,
            'Deshacer',
            () => this.reactivarSilencioso(c.categoria.id, c.categoria.nombre)
          );
        },
        error: (err) => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.mostrarToast('error', err.error?.message ?? 'Error al eliminar');
        }
      });
    } else {
      this.categoriaService.reactivar(c.categoria.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast('success', 'Categoría reactivada correctamente');
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
    this.categoriaService.reactivar(id).subscribe({
      next: () => {
        this.cargar();
        this.mostrarToast('success', `Categoría ${nombre} restaurada`);
      },
      error: () => {
        this.mostrarToast('error', 'No se pudo restaurar la categoría');
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

  trackById(_: number, item: Categoria): number {
    return item.id;
  }
}