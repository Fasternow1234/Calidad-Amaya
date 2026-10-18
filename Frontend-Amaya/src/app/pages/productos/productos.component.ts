import { Component, inject, signal, computed, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProductoService } from '../../services/producto.service';
import { CategoriaService } from '../../services/categoria.service';
import { AlmacenService } from '../../services/almacen.service';
import { Producto } from '../../models/producto.models';
import { Categoria } from '../../models/categoria.models';
import { Almacen } from '../../models/almacen.models';
import { AuthService } from '../../services/auth.service';

type ToastTipo = 'success' | 'error';
type ConfirmAccion = 'eliminar' | 'reactivar';
type OrdenCampo = 'nombre' | 'stock' | 'precio' | 'fecha';
type OrdenDireccion = 'asc' | 'desc';

interface Toast {
  tipo: ToastTipo;
  mensaje: string;
  accionLabel?: string;
  accionFn?: () => void;
}

interface Confirmacion {
  accion: ConfirmAccion;
  producto: Producto;
}

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './productos.component.html',
  styleUrl: './productos.component.css'
})
export class ProductosComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly productoService = inject(ProductoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly almacenService = inject(AlmacenService);
  private readonly authService = inject(AuthService);

  readonly productos = signal<Producto[]>([]);
  readonly categorias = signal<Categoria[]>([]);
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
  readonly productoViendo = signal<Producto | null>(null);
  readonly ordenCampo = signal<OrdenCampo>('nombre');
  readonly ordenDireccion = signal<OrdenDireccion>('asc');
  readonly puedeGestionarCatalogo = computed(() => this.authService.usuario()?.rol !== 'VENDEDOR');

  private toastTimeout: any = null;
  private toastAccionTimeout: any = null;

  readonly unidadesMedida = ['UNIDAD', 'KILO', 'LITRO', 'GRAMO'];

  readonly tituloFormulario = computed(() =>
    this.editandoId() !== null ? 'Editar producto' : 'Nuevo producto'
  );

  readonly tituloConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    return c.accion === 'eliminar' ? 'Eliminar producto' : 'Reactivar producto';
  });

  readonly mensajeConfirmacion = computed(() => {
    const c = this.confirmacion();
    if (!c) return '';
    if (c.accion === 'reactivar') {
      return `¿Deseas reactivar el producto "${c.producto.nombre}"? Volverá a estar disponible para vender.`;
    }
    return `¿Estás seguro de eliminar el producto "${c.producto.nombre}"? Podrás reactivarlo después.`;
  });

  readonly productosFiltrados = computed(() => {
    const t = this.busqueda().trim().toLowerCase();
    let lista = this.productos();
    if (t) {
      lista = lista.filter((p) =>
        (p.codigo ?? '').toLowerCase().includes(t) ||
        (p.nombre ?? '').toLowerCase().includes(t) ||
        (p.descripcion ?? '').toLowerCase().includes(t) ||
        (p.categoriaNombre ?? '').toLowerCase().includes(t) ||
        (p.nombreAlmacenDefault ?? '').toLowerCase().includes(t)
      );
    }
    const campo = this.ordenCampo();
    const dir = this.ordenDireccion();
    const factor = dir === 'asc' ? 1 : -1;

    return [...lista].sort((a, b) => {
      let va: any;
      let vb: any;
      if (campo === 'nombre') {
        va = (a.nombre ?? '').toLowerCase();
        vb = (b.nombre ?? '').toLowerCase();
      } else if (campo === 'stock') {
        va = Number(a.stockTotal ?? 0);
        vb = Number(b.stockTotal ?? 0);
      } else if (campo === 'precio') {
        va = Number(a.precioVenta ?? 0);
        vb = Number(b.precioVenta ?? 0);
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
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: ['', [Validators.maxLength(300)]],
    precioCompra: [0, [Validators.required, Validators.min(0.01)]],
    precioVenta: [0, [Validators.required, Validators.min(0.01)]],
    idCategoria: [0, [Validators.required, Validators.min(1)]],
    unidadMedida: ['UNIDAD', [Validators.required]],
    idAlmacen: [0, [Validators.required, Validators.min(1)]]
  });

  readonly nombreLargo = computed(() => this.form.controls.nombre.value.length);
  readonly descripcionLargo = computed(() => this.form.controls.descripcion.value.length);

  get nombre() { return this.form.controls.nombre; }
  get descripcion() { return this.form.controls.descripcion; }
  get precioCompra() { return this.form.controls.precioCompra; }
  get precioVenta() { return this.form.controls.precioVenta; }
  get idCategoria() { return this.form.controls.idCategoria; }
  get unidadMedida() { return this.form.controls.unidadMedida; }
  get idAlmacen() { return this.form.controls.idAlmacen; }

  ngOnInit(): void {
    this.cargarTodo();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.productoViendo()) {
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

  cargarTodo(): void {
    this.cargando.set(true);
    if (this.puedeGestionarCatalogo()) {
      this.categoriaService.listarActivas().subscribe({
        next: (data) => this.categorias.set(data ?? []),
        error: () => this.categorias.set([])
      });
      this.almacenService.listarActivos().subscribe({
        next: (data) => this.almacenes.set(data ?? []),
        error: () => this.almacenes.set([])
      });
    }
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    const peticion = this.puedeGestionarCatalogo() && this.mostrarInactivos()
      ? this.productoService.listarTodos()
      : this.productoService.listarActivos();
    peticion.subscribe({
      next: (data) => {
        this.productos.set(data ?? []);
        this.cargando.set(false);
      },
      error: () => {
        this.mostrarToast('error', 'Error al cargar productos');
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

  verProducto(p: Producto): void {
    this.productoViendo.set(p);
  }

  cerrarVista(): void {
    this.productoViendo.set(null);
  }

  abrirFormularioNuevo(): void {
    this.editandoId.set(null);
    this.form.reset({
      nombre: '',
      descripcion: '',
      precioCompra: 0,
      precioVenta: 0,
      idCategoria: this.categorias().length > 0 ? this.categorias()[0].id : 0,
      unidadMedida: 'UNIDAD',
      idAlmacen: this.almacenes().length > 0 ? this.almacenes()[0].id : 0
    });
    this.formVisible.set(true);
  }

  abrirFormularioEditar(p: Producto): void {
    this.editandoId.set(p.id);
    this.form.reset({
      nombre: p.nombre,
      descripcion: p.descripcion ?? '',
      precioCompra: Number(p.precioCompra ?? 0),
      precioVenta: Number(p.precioVenta ?? 0),
      idCategoria: p.idCategoria,
      unidadMedida: p.unidadMedida,
      idAlmacen: p.idAlmacenDefault ?? 0
    });
    this.formVisible.set(true);
  }

  cancelarFormulario(): void {
    this.formVisible.set(false);
    this.editandoId.set(null);
  }

  campoValido(control: 'nombre' | 'precioCompra' | 'precioVenta' | 'idCategoria' | 'unidadMedida' | 'idAlmacen'): boolean {
    const c = this.form.controls[control];
    if (typeof c.value === 'number') {
      return c.value > 0 && c.valid;
    }
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
    const request = {
      nombre: raw.nombre.trim(),
      descripcion: raw.descripcion?.trim() || null,
      precioCompra: Number(raw.precioCompra),
      precioVenta: Number(raw.precioVenta),
      idCategoria: Number(raw.idCategoria),
      unidadMedida: raw.unidadMedida,
      idAlmacen: Number(raw.idAlmacen)
    };

    const id = this.editandoId();
    if (id !== null) {
      this.productoService.actualizar(id, request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Producto actualizado correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al actualizar');
        }
      });
    } else {
      this.productoService.crear(request).subscribe({
        next: () => {
          this.guardando.set(false);
          this.cancelarFormulario();
          this.cargar();
          this.mostrarToast('success', 'Producto creado correctamente');
        },
        error: (err) => {
          this.guardando.set(false);
          this.mostrarToast('error', err.error?.message ?? 'Error al crear');
        }
      });
    }
  }

  pedirEliminar(p: Producto): void {
    const stock = Number(p.stockTotal ?? 0);
    if (stock > 0) {
      const cantidadFormateada = this.formatearCantidadConUnidad(stock, p.unidadMedida);
      this.mostrarToast(
        'error',
        `No se puede eliminar "${p.nombre}": tiene ${cantidadFormateada} en stock. Vende o retira el stock antes de eliminarlo.`
      );
      return;
    }
    this.confirmacion.set({ accion: 'eliminar', producto: p });
  }

  pedirReactivar(p: Producto): void {
    this.confirmacion.set({ accion: 'reactivar', producto: p });
  }

  cancelarConfirmacion(): void {
    this.confirmacion.set(null);
  }

  confirmarAccion(): void {
    const c = this.confirmacion();
    if (!c) return;
    this.procesando.set(true);

    if (c.accion === 'eliminar') {
      this.productoService.desactivar(c.producto.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast(
            'success',
            `Producto ${c.producto.nombre} eliminado`,
            'Deshacer',
            () => this.reactivarSilencioso(c.producto.id, c.producto.nombre)
          );
        },
        error: (err) => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.mostrarToast('error', err.error?.message ?? 'Error al eliminar');
        }
      });
    } else {
      this.productoService.reactivar(c.producto.id).subscribe({
        next: () => {
          this.procesando.set(false);
          this.confirmacion.set(null);
          this.cargar();
          this.mostrarToast('success', 'Producto reactivado correctamente');
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
    this.productoService.reactivar(id).subscribe({
      next: () => {
        this.cargar();
        this.mostrarToast('success', `Producto ${nombre} restaurado`);
      },
      error: () => this.mostrarToast('error', 'No se pudo restaurar el producto')
    });
  }

  /**
   * Formatea cantidad + unidad con singular/plural correcto.
   * Ej: 1 + UNIDAD -> "1 unidad" | 2 + UNIDAD -> "2 unidades"
   *     1 + KILO   -> "1 kilo"   | 3.5 + KILO -> "3.5 kilos"
   */
  formatearCantidadConUnidad(cantidad: number | null | undefined, unidadMedida: string | null | undefined): string {
    const num = Number(cantidad ?? 0);
    if (!isFinite(num)) return '0';

    // quitar ceros decimales innecesarios (5.00 -> 5, 3.50 -> 3.5)
    const limpio = num.toString();

    if (!unidadMedida) return limpio;

    const singular = num === 1;
    const unidad = unidadMedida.toLowerCase().trim();

    if (singular) {
      return `${limpio} ${unidad}`;
    }

    const plurales: Record<string, string> = {
      unidad: 'unidades',
      kilo: 'kilos',
      litro: 'litros',
      gramo: 'gramos'
    };
    return `${limpio} ${plurales[unidad] ?? unidad}`;
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

  stockClase(p: Producto): string {
    return Number(p.stockTotal ?? 0) <= 0 ? 'agotado' : 'disponible';
  }

  trackById(_: number, item: Producto): number {
    return item.id;
  }
}
