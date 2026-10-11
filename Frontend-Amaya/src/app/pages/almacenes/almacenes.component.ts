import { Component, inject, signal, computed, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AlmacenService } from '../../services/almacen.service';
import { Almacen } from '../../models/almacen.models';
import { StockInfo } from '../../models/producto.models';

type ToastTipo = 'success' | 'error';
type ConfirmAccion = 'eliminar' | 'reactivar';
type FiltroRapido = 'todos' | 'con-productos' | 'vacios';
type OrdenCampo = 'nombre' | 'productos' | 'fecha';
type OrdenDireccion = 'asc' | 'desc';

interface Toast {
  tipo: ToastTipo;
  mensaje: string;
  accionLabel?: string;
  accionFn?: () => void;
}

interface Confirmacion {
  accion: ConfirmAccion;
  almacen: Almacen;
}

@Component({
  selector: 'app-almacenes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './almacenes.component.html',
  styleUrl: './almacenes.component.css'
})
export class AlmacenesComponent implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly almacenService = inject(AlmacenService);

  readonly almacenes = signal<Almacen[]>([]);
  readonly cargando = signal(false);
  readonly mostrarInactivos = signal(false);
  readonly formVisible = signal(false);
  readonly editandoId = signal<number | null>(null);
  readonly guardando = signal(false);
  readonly busqueda = signal('');
  readonly toast = signal<Toast | null>(null);
  readonly confirmacion = signal<Confirmacion | null>(null);
  readonly procesando = signal(false);
  readonly almacenViendo = signal<Almacen | null>(null);
  readonly stockViendo = signal<{ almacen: Almacen; stocks: StockInfo[] } | null>(null);
  readonly cargandoStock = signal(false);

  readonly filtroRapido = signal<FiltroRapido>('todos');
  readonly ordenCampo = signal<OrdenCampo>('nombre');
  readonly ordenDireccion = signal<OrdenDireccion>('asc');

  readonly busquedaStockModal = signal('');
  readonly ordenStockModal = signal<'nombre' | 'cantidad'>('nombre');

  private toastTimeout: any = null;
  private toastAccionTimeout: any = null;

  readonly tituloFormulario = computed(() =>
    this.editandoId() !== null ? 'Editar almacén' : 'Nuevo almacén'
  );

  readonly tituloConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    return c.accion === 'eliminar' ? 'Eliminar almacén' : 'Reactivar almacén';
  });

  readonly mensajeConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    if (c.accion === 'reactivar') {
      return `¿Deseas reactivar el almacén "${c.almacen.nombre}"? Volverá a estar disponible en el sistema.`;
    }
    if (c.almacen.totalProductos > 0) {
      const n = c.almacen.totalProductos;
      const plural = n === 1 ? 'producto' : 'productos';
      return `Este almacén tiene ${n} ${plural} con stock. Si continúas, se desactivará pero NO podrás reactivarlo hasta que transfieras o retires todo el inventario.`;
    }
    return `¿Estás seguro de eliminar el almacén "${c.almacen.nombre}"? Podrás reactivarlo después.`;
  });

  readonly almacenesFiltrados = computed(() => {
    const t = this.busqueda().trim().toLowerCase();
    const filtro = this.filtroRapido();
    let lista = this.almacenes();

    if (t) {
      lista = lista.filter((a) =>
        a.nombre.toLowerCase().includes(t) ||
        (a.direccion?.toLowerCase().includes(t) ?? false)
      );
    }

    if (filtro === 'con-productos') {
      lista = lista.filter((a) => a.totalProductos > 0);
    } else if (filtro === 'vacios') {
      lista = lista.filter((a) => a.totalProductos === 0);
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
      } else if (campo === 'productos') {
        va = a.totalProductos;
        vb = b.totalProductos;
      } else {
        va = a.fechaRegistro;
        vb = b.fechaRegistro;
      }
      if (va < vb) return -1 * factor;
      if (va > vb) return 1 * factor;
      return 0;
    });
  });

  readonly maxProductos = computed(() => {
    const lista = this.almacenes();
    if (lista.length === 0) return 1;
    return Math.max(...lista.map((a) => a.totalProductos), 1);
  });

  readonly stockViendoFiltrado = computed(() => {
    const sv = this.stockViendo();
    if (!sv) return [];
    const texto = this.busquedaStockModal().trim().toLowerCase();
    let lista = sv.stocks;
    if (texto) {
      lista = lista.filter((s) =>
        s.nombreProducto.toLowerCase().includes(texto) ||
        s.codigoProducto.toLowerCase().includes(texto)
      );
    }
    const orden = this.ordenStockModal();
    return [...lista].sort((a, b) => {
      if (orden === 'cantidad') return b.cantidad - a.cantidad;
      return a.nombreProducto.localeCompare(b.nombreProducto);
    });
  });

  readonly stockViendoTotal = computed(() => {
    const lista = this.stockViendoFiltrado();
    const productos = lista.length;
    const unidades = lista.reduce((sum, s) => sum + Number(s.cantidad || 0), 0);
    return { productos, unidades };
  });

  readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    direccion: ['', [Validators.maxLength(200)]]
  });

  readonly nombreLargo = computed(() => this.form.controls.nombre.value.length);
  readonly direccionLargo = computed(() => this.form.controls.direccion.value.length);

  readonly sugerenciasNombre = computed(() => {
    const base = ['Almacén Casa', 'Almacén Principal', 'Almacén Secundario', 'Almacén Unicachi', 'Almacén Tránsito'];
    const existentes = new Set(this.almacenes().map((a) => a.nombre.toLowerCase()));
    return base.filter((s) => !existentes.has(s.toLowerCase()));
  });

  get nombre() { return this.form.controls.nombre; }
  get direccion() { return this.form.controls.direccion; }

  ngOnInit(): void {
    this.cargar();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.confirmacion()) {
      this.cancelarConfirmacion();
      return;
    }
    if (this.stockViendo()) {
      this.cerrarStock();
      return;
    }
    if (this.almacenViendo()) {
      this.cerrarVista();
      return;
    }
    if (this.formVisible()) {
      this.cancelarFormulario();
    }
  }

  cargar(): void {
    this.cargando.set(true);
    const peticion = this.mostrarInactivos()
      ? this.almacenService.listarTodos()
      : this.almacenService.listarActivos();
    peticion.subscribe({
      next: (data) => {
        this.almacenes.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.mostrarToast('error', 'Error al cargar almacenes');
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

  cambiarFiltroRapido(f: FiltroRapido): void {
    this.filtroRapido.set(f);
  }

  contarPorFiltro(f: FiltroRapido): number {
    const lista = this.almacenes();
    if (f === 'todos') return lista.length;
    if (f === 'con-productos') return lista.filter((a) => a.totalProductos > 0).length;
    return lista.filter((a) => a.totalProductos === 0).length;
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

  verAlmacen(a: Almacen): void {
    this.almacenViendo.set(a);
  }

  cerrarVista(): void {
    this.almacenViendo.set(null);
  }

  verStock(a: Almacen): void {
    this.busquedaStockModal.set('');
    this.ordenStockModal.set('nombre');
    this.stockViendo.set({ almacen: a, stocks: [] });
    this.cargandoStock.set(true);
    this.almacenService.stockPorAlmacen(a.id).subscribe({
      next: (stocks) => {
        const conStock = (stocks ?? []).filter((s) => Number(s.cantidad) > 0);
        this.stockViendo.set({ almacen: a, stocks: conStock });
        this.cargandoStock.set(false);
      },
      error: () => {
        this.mostrarToast('error', 'Error al cargar stock');
        this.cargandoStock.set(false);
        this.stockViendo.set(null);
      }
    });
  }

  cerrarStock(): void {
    this.stockViendo.set(null);
    this.busquedaStockModal.set('');
  }

  onBuscarStock(event: Event): void {
    this.busquedaStockModal.set((event.target as HTMLInputElement).value);
  }

  limpiarBusquedaStock(): void {
    this.busquedaStockModal.set('');
  }

  cambiarOrdenStock(o: 'nombre' | 'cantidad'): void {
    this.ordenStockModal.set(o);
  }

  abrirFormularioNuevo(): void {
    this.editandoId.set(null);
    this.form.reset({ nombre: '', direccion: '' });
    this.formVisible.set(true);
  }

  abrirFormularioEditar(a: Almacen): void {
    this.editandoId.set(a.id);
    this.form.reset({ nombre: a.nombre, direccion: a.direccion ?? '' });
    this.formVisible.set(true);
  }

  cancelarFormulario(): void {
    this.formVisible.set(false);
    this.editandoId.set(null);
  }

  campoValido(control: 'nombre' | 'direccion'): boolean {
    const c = this.form.controls[control];
    return c.value.length > 0 && c.valid;
  }

  campoInvalido(control: 'nombre' | 'direccion'): boolean {
    const c = this.form.controls[control];
    return c.touched && c.invalid;
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
      nombre: raw.nombre.trim(),
      direccion: raw.direccion?.trim() || null
    };
    const id = this.editandoId();
    if (id !== null) {
      this.almacenService.actualizar(id, request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Almacén actualizado correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al actualizar');
        }
      });
    } else {
      this.almacenService.crear(request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Almacén creado correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al crear');
        }
      });
    }
  }

  pedirEliminar(a: Almacen): void {
    this.confirmacion.set({ accion: 'eliminar', almacen: a });
  }

  pedirReactivar(a: Almacen): void {
    this.confirmacion.set({ accion: 'reactivar', almacen: a });
  }

  cancelarConfirmacion(): void {
    this.confirmacion.set(null);
  }

  confirmarAccion(): void {
    const c = this.confirmacion();
    if (!c) return;
    this.procesando.set(true);

    if (c.accion === 'eliminar') {
      this.almacenService.desactivar(c.almacen.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast(
            'success',
            `Almacén ${c.almacen.nombre} eliminado`,
            'Deshacer',
            () => this.reactivarSilencioso(c.almacen.id, c.almacen.nombre)
          );
        },
        error: (err) => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.mostrarToast('error', err.error?.message ?? 'Error al eliminar');
        }
      });
    } else {
      this.almacenService.reactivar(c.almacen.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast('success', 'Almacén reactivado correctamente');
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
    this.almacenService.reactivar(id).subscribe({
      next: () => {
        this.cargar();
        this.mostrarToast('success', `Almacén ${nombre} restaurado`);
      },
      error: () => {
        this.mostrarToast('error', 'No se pudo restaurar el almacén');
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

  badgeClase(a: Almacen): string {
    const n = a.totalProductos;
    if (n === 0) return 'vacio';
    if (n <= 5) return 'bajo';
    if (n <= 15) return 'medio';
    return 'alto';
  }

  badgeProgreso(a: Almacen): number {
    const max = this.maxProductos();
    if (max === 0) return 0;
    return Math.min(100, Math.round((a.totalProductos / max) * 100));
  }

  trackById(_: number, item: Almacen): number {
    return item.id;
  }
}