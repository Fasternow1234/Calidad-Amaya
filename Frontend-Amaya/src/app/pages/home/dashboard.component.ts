import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Comprobante } from '../../models/comprobante.models';
import { Movimiento } from '../../models/movimiento.models';
import { Producto } from '../../models/producto.models';
import { AuthService } from '../../services/auth.service';
import { ComprobanteService } from '../../services/comprobante.service';
import { MovimientoService } from '../../services/movimiento.service';
import { ProductoService } from '../../services/producto.service';

interface DiaGrafico {
  etiqueta: string;
  valor: number;
  alto: number;
  detalle: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly productosApi = inject(ProductoService);
  private readonly comprobantesApi = inject(ComprobanteService);
  private readonly movimientosApi = inject(MovimientoService);

  readonly usuario = this.auth.usuario;
  readonly rol = computed(() => this.usuario()?.rol ?? '');
  readonly esComercial = computed(() => ['ADMINISTRADOR', 'VENDEDOR'].includes(this.rol()));
  readonly esInventario = computed(() => ['ADMINISTRADOR', 'ALMACENERO'].includes(this.rol()));
  readonly cargandoProductos = signal(true);
  readonly cargandoComprobantes = signal(false);
  readonly cargandoMovimientos = signal(false);
  readonly errorProductos = signal(false);
  readonly errorComprobantes = signal(false);
  readonly errorMovimientos = signal(false);
  readonly productos = signal<Producto[]>([]);
  readonly comprobantes = signal<Comprobante[]>([]);
  readonly movimientos = signal<Movimiento[]>([]);
  readonly umbralStockBajo = 5;

  readonly agotados = computed(() => this.productos().filter(p => Number(p.stockTotal ?? 0) <= 0));
  readonly stockBajo = computed(() => this.productos().filter(p => {
    const stock = Number(p.stockTotal ?? 0);
    return stock > 0 && stock <= this.umbralStockBajo;
  }));
  readonly ventasValidas = computed(() => this.comprobantes().filter(c => c.tipo === 'VENTA' && Number(c.estado) === 1));
  readonly ventasHoy = computed(() => this.ventasValidas().filter(c => this.esHoy(c.fechaEmision)));
  readonly totalVentasHoy = computed(() => this.ventasHoy().reduce((s, c) => s + Number(c.total ?? 0), 0));
  readonly ventasMes = computed(() => this.ventasValidas().filter(c => this.esMesActual(c.fechaEmision)));
  readonly totalVentasMes = computed(() => this.ventasMes().reduce((s, c) => s + Number(c.total ?? 0), 0));
  readonly movimientosHoy = computed(() => this.movimientos().filter(m => this.esHoy(m.fecha)));
  readonly entradasHoy = computed(() => this.movimientosHoy().filter(m => m.tipo === 'ENTRADA').length);
  readonly salidasHoy = computed(() => this.movimientosHoy().filter(m => m.tipo === 'SALIDA').length);
  readonly ventasRecientes = computed(() => [...this.ventasValidas()]
    .sort((a, b) => this.timestamp(b.fechaEmision) - this.timestamp(a.fechaEmision)).slice(0, 5));
  readonly movimientosRecientes = computed(() => [...this.movimientos()]
    .sort((a, b) => this.timestamp(b.fecha) - this.timestamp(a.fecha)).slice(0, 5));
  readonly fechaActual = new Intl.DateTimeFormat('es-PE', { dateStyle: 'full' }).format(new Date());
  readonly graficoVentas = computed(() => {
    const dias = this.ultimosDias();
    const filas = dias.map(d => ({
      etiqueta: d.etiqueta,
      valor: this.ventasValidas().filter(c => this.fechaKey(c.fechaEmision) === d.key)
        .reduce((s, c) => s + Number(c.total ?? 0), 0),
      detalle: ''
    }));
    return this.normalizarGrafico(filas);
  });
  readonly graficoMovimientos = computed(() => {
    const dias = this.ultimosDias();
    const filas = dias.map(d => ({
      etiqueta: d.etiqueta,
      valor: this.movimientos().filter(m => this.fechaKey(m.fecha) === d.key).length,
      detalle: ''
    }));
    return this.normalizarGrafico(filas);
  });

  ngOnInit(): void {
    this.productosApi.listarActivos().subscribe({
      next: data => { this.productos.set(data); this.cargandoProductos.set(false); },
      error: () => { this.errorProductos.set(true); this.cargandoProductos.set(false); }
    });
    if (this.esComercial()) {
      this.cargandoComprobantes.set(true);
      this.comprobantesApi.listar().subscribe({
        next: data => { this.comprobantes.set(data); this.cargandoComprobantes.set(false); },
        error: () => { this.errorComprobantes.set(true); this.cargandoComprobantes.set(false); }
      });
    }
    if (this.esInventario()) {
      this.cargandoMovimientos.set(true);
      this.movimientosApi.listar().subscribe({
        next: data => { this.movimientos.set(data); this.cargandoMovimientos.set(false); },
        error: () => { this.errorMovimientos.set(true); this.cargandoMovimientos.set(false); }
      });
    }
  }

  moneda(valor: number): string {
    return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(valor);
  }

  formatearFecha(fecha: string | null | undefined): string {
    if (!fecha) return '—';
    const d = new Date(fecha.length === 10 ? `${fecha}T12:00:00` : fecha);
    return Number.isNaN(d.getTime()) ? fecha : new Intl.DateTimeFormat('es-PE', { dateStyle: 'short' }).format(d);
  }

  private esHoy(fecha: string): boolean { return this.fechaKey(fecha) === this.fechaKey(new Date()); }
  private esMesActual(fecha: string): boolean {
    const d = new Date(fecha.length === 10 ? `${fecha}T12:00:00` : fecha);
    const hoy = new Date();
    return d.getFullYear() === hoy.getFullYear() && d.getMonth() === hoy.getMonth();
  }
  private timestamp(fecha: string): number { return new Date(fecha).getTime() || 0; }
  private fechaKey(fecha: string | Date): string {
    const valor = fecha instanceof Date ? fecha : fecha.length === 10 ? `${fecha}T12:00:00` : fecha;
    const d = new Date(valor);
    if (Number.isNaN(d.getTime())) return String(fecha).slice(0, 10);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }
  private ultimosDias(): { key: string; etiqueta: string }[] {
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(); d.setHours(12, 0, 0, 0); d.setDate(d.getDate() - (6 - i));
      return { key: this.fechaKey(d), etiqueta: new Intl.DateTimeFormat('es-PE', { weekday: 'short' }).format(d) };
    });
  }
  private normalizarGrafico(filas: { etiqueta: string; valor: number; detalle: string }[]): DiaGrafico[] {
    const max = Math.max(1, ...filas.map(f => f.valor));
    return filas.map(f => ({ ...f, alto: f.valor === 0 ? 3 : Math.max(8, Math.round((f.valor / max) * 100)), detalle: this.moneda(f.valor) }));
  }
}
