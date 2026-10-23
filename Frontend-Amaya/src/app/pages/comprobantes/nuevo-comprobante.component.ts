import { Component, inject, signal, output, input, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { ComprobanteService } from '../../services/comprobante.service';
import { ProveedorService } from '../../services/proveedor.service';
import { ProductoService } from '../../services/producto.service';
import { Proveedor } from '../../models/proveedor.models';
import { Producto } from '../../models/producto.models';
import { Comprobante } from '../../models/comprobante.models';
<<<<<<< Updated upstream
=======
import { AuthService } from '../../services/auth.service';
import { ClienteService } from '../../services/cliente.service';
import { Cliente } from '../../models/cliente.models';
import { cantidadEnteraPositiva } from './cantidad-entera.validator';
>>>>>>> Stashed changes

function hoyLocal(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dia}`;
}

@Component({
  selector: 'app-nuevo-comprobante',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './nuevo-comprobante.component.html',
  styleUrl: './nuevo-comprobante.component.css'
})
export class NuevoComprobanteComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly comprobanteService = inject(ComprobanteService);
  private readonly proveedorService = inject(ProveedorService);
  private readonly productoService = inject(ProductoService);

  readonly comprobantesExistentes = input<Comprobante[]>([]);

  readonly cerrado = output<void>();
  readonly guardado = output<void>();

  readonly proveedores = signal<Proveedor[]>([]);
  readonly productos = signal<Producto[]>([]);
  readonly guardando = signal(false);
  readonly errorMsg = signal<string | null>(null);

  // Tipo de comprobante SUNAT: F = Factura, B = Boleta
  readonly tipoComprobante = signal<'F' | 'B'>('F');
  // Correlativo editable (8 dígitos)
  readonly correlativo = signal<number>(1);

  readonly maxFecha = hoyLocal();

  // Preview: F001-00000034
  readonly numeroPreview = computed(() => {
    const t = this.tipoComprobante();
    const n = Math.max(1, Math.floor(this.correlativo()) || 1);
    return `${t}001-${n.toString().padStart(8, '0')}`;
  });

  private readonly _refreshLineas = signal(0);

  readonly form = this.fb.nonNullable.group({
    tipo: ['COMPRA' as 'COMPRA' | 'VENTA', [Validators.required]],
    fechaEmision: [hoyLocal(), [Validators.required]],
    idProveedor: [0],
    observacion: ['', [Validators.maxLength(300)]],
    detalles: this.fb.array<FormGroup>([])
  });

  get detalles(): FormArray { return this.form.controls.detalles; }
  get tipo() { return this.form.controls.tipo; }
  get fechaEmision() { return this.form.controls.fechaEmision; }
  get observacion() { return this.form.controls.observacion; }

  readonly observacionLargo = computed(() => this.form.controls.observacion.value.length);

  readonly lineas = computed<FormGroup[]>(() => {
    this._refreshLineas();
    return [...(this.detalles.controls as FormGroup[])];
  });

  readonly productosDisponibles = computed(() => {
    const lista = this.productos();
    if (this.tipo.value === 'VENTA') {
      return lista.filter(p => Number(p.stockTotal ?? 0) > 0);
    }
    return lista;
  });

  readonly total = computed(() => {
    this._refreshLineas();
    return this.lineas().reduce((sum, ctrl) => {
      const cant = Number(ctrl.get('cantidad')?.value ?? 0);
      const prec = Number(ctrl.get('precioUnitario')?.value ?? 0);
      return sum + cant * prec;
    }, 0);
  });

  ngOnInit(): void {
    this.proveedorService.listarActivos().subscribe({
      next: (data) => this.proveedores.set(data ?? []),
      error: () => this.errorMsg.set('Error al cargar proveedores')
    });
    this.productoService.listarActivos().subscribe({
      next: (data) => this.productos.set(data ?? []),
      error: () => this.errorMsg.set('Error al cargar productos')
    });

    this.agregarDetalle();
    this.sugerirCorrelativo();

    this.detalles.valueChanges.subscribe(() => {
      this._refreshLineas.update(v => v + 1);
    });

    this.form.controls.tipo.valueChanges.subscribe((tipoActual) => {
      if (tipoActual === 'VENTA') {
        this.form.controls.idProveedor.setValue(0);
      }
      this.detalles.controls.forEach((ctrl) => {
        const idProducto = Number(ctrl.get('idProducto')?.value ?? 0);
        const producto = this.productos().find((p) => p.id === idProducto);
        if (producto) {
          const precio = tipoActual === 'COMPRA' ? producto.precioCompra : producto.precioVenta;
          ctrl.get('precioUnitario')?.setValue(Number(precio ?? 0));
        }
      });
      this._refreshLineas.update(v => v + 1);
    });
  }

  // ---------- Correlativo SUNAT ----------

  cambiarTipoComprobante(t: 'F' | 'B'): void {
    this.tipoComprobante.set(t);
    this.sugerirCorrelativo();
  }

  private sugerirCorrelativo(): void {
    const tipo = this.tipoComprobante();
    const prefijo = `${tipo}001-`;
    const nums = this.comprobantesExistentes()
      .map(c => c.numero)
      .filter(n => n && n.startsWith(prefijo))
      .map(n => parseInt(n.substring(prefijo.length), 10))
      .filter(n => !isNaN(n));
    const siguiente = nums.length > 0 ? Math.max(...nums) + 1 : 1;
    this.correlativo.set(siguiente);
  }

  onCorrelativoInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const soloDigitos = input.value.replace(/\D/g, '').slice(0, 8);
    if (input.value !== soloDigitos) {
      input.value = soloDigitos;
    }
    const num = soloDigitos === '' ? 1 : parseInt(soloDigitos, 10);
    this.correlativo.set(num);
  }

  // ---------- Líneas ----------

  agregarDetalle(): void {
    const grupo = this.fb.nonNullable.group({
      idProducto: [0, [Validators.required, this.noCero]],
      cantidad: [1, [Validators.required, Validators.min(1), cantidadEnteraPositiva]],
      precioUnitario: [0, [Validators.required, Validators.min(0)]]
    });
    this.detalles.push(grupo);
    this._refreshLineas.update(v => v + 1);
  }

  eliminarDetalle(index: number): void {
    this.detalles.removeAt(index);
    this._refreshLineas.update(v => v + 1);
  }

  onProductoChange(index: number): void {
    const ctrl = this.detalles.at(index);
    const idProducto = Number(ctrl.get('idProducto')?.value ?? 0);
    const producto = this.productos().find((p) => p.id === idProducto);
    if (!producto) return;
    const precio = this.tipo.value === 'COMPRA' ? producto.precioCompra : producto.precioVenta;
    ctrl.get('precioUnitario')?.setValue(Number(precio ?? 0));
  }

  productoDeLinea(index: number): Producto | null {
    const ctrl = this.detalles.at(index);
    const idProducto = Number(ctrl?.get('idProducto')?.value ?? 0);
    if (!idProducto) return null;
    return this.productos().find(p => p.id === idProducto) ?? null;
  }

  stockInsuficiente(index: number): boolean {
    if (this.tipo.value !== 'VENTA') return false;
    const ctrl = this.detalles.at(index);
    if (!ctrl) return false;
    const cantidad = Number(ctrl.get('cantidad')?.value ?? 0);
    const producto = this.productoDeLinea(index);
    if (!producto || cantidad <= 0) return false;
    return cantidad > Number(producto.stockTotal ?? 0);
  }

  private noCero(control: AbstractControl): { [key: string]: any } | null {
    return control.value === 0 || control.value === null ? { requerido: true } : null;
  }

  // ---------- Guardar ----------

  guardar(): void {
    this.errorMsg.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMsg.set('Revisa los campos marcados en rojo.');
      return;
    }

    const raw = this.form.getRawValue();
    const detallesRaw = raw.detalles as Array<{
      idProducto: number;
      cantidad: number;
      precioUnitario: number;
    }>;

    if (detallesRaw.length === 0) {
      this.errorMsg.set('Debe agregar al menos un producto');
      return;
    }
    if (raw.tipo === 'COMPRA' && (!raw.idProveedor || raw.idProveedor === 0)) {
      this.errorMsg.set('Debe seleccionar un proveedor para la compra');
      return;
    }

    if (raw.tipo === 'VENTA') {
      for (const d of detallesRaw) {
        const producto = this.productos().find(p => p.id === Number(d.idProducto));
        if (!producto) continue;
        if (Number(d.cantidad) > Number(producto.stockTotal ?? 0)) {
          this.errorMsg.set(
            `Stock insuficiente de "${producto.nombre}": disponible ` +
            `${producto.stockTotal} ${producto.unidadMedida?.toLowerCase()}, ` +
            `solicitado ${d.cantidad}.`
          );
          return;
        }
      }
    }

    // Validación local de duplicado (feedback inmediato)
    const numeroFinal = this.numeroPreview();
    const yaExiste = this.comprobantesExistentes()
      .some(c => c.numero.toUpperCase() === numeroFinal.toUpperCase());
    if (yaExiste) {
      this.errorMsg.set(`El número ${numeroFinal} ya existe. Cambia el correlativo o la serie.`);
      return;
    }

    this.guardando.set(true);

    this.comprobanteService.crear({
      tipo: raw.tipo,
      numero: numeroFinal,
      fechaEmision: raw.fechaEmision,
      idProveedor: raw.tipo === 'COMPRA' ? raw.idProveedor : null,
      observacion: raw.observacion?.trim() || null,
      detalles: detallesRaw.map((d) => ({
        idProducto: Number(d.idProducto),
        idAlmacen: null,
        cantidad: Number(d.cantidad),
        precioUnitario: Number(d.precioUnitario)
      }))
    }).subscribe({
      next: () => {
        this.guardando.set(false);
        this.guardado.emit();
      },
      error: (err) => {
        this.guardando.set(false);
        const mensaje =
          err?.error?.message
          || (typeof err?.error === 'string' ? err.error : null)
          || err?.error?.error
          || err?.message
          || `Error ${err?.status ?? ''}: ${err?.statusText ?? 'No se pudo guardar el comprobante'}`;
        this.errorMsg.set(mensaje);
      }
    });
  }

  cancelar(): void {
    this.cerrado.emit();
  }
}