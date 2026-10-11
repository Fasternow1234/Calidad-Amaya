import { Component, inject, signal, computed, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MovimientoService } from '../../services/movimiento.service';
import { Movimiento } from '../../models/movimiento.models';

type FiltroTipo = 'TODOS' | 'ENTRADA' | 'SALIDA';
type OrdenCampo = 'fecha' | 'producto' | 'cantidad';
type OrdenDireccion = 'asc' | 'desc';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './movimientos.component.html',
  styleUrl: './movimientos.component.css'
})
export class MovimientosComponent implements OnInit {

  private readonly movimientoService = inject(MovimientoService);

  readonly movimientos = signal<Movimiento[]>([]);
  readonly cargando = signal(false);
  readonly busqueda = signal('');
  readonly filtroTipo = signal<FiltroTipo>('TODOS');
  readonly movimientoViendo = signal<Movimiento | null>(null);

  readonly ordenCampo = signal<OrdenCampo>('fecha');
  readonly ordenDireccion = signal<OrdenDireccion>('desc');

  readonly movimientosFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const tipo = this.filtroTipo();
    let lista = this.movimientos();

    if (texto) {
      lista = lista.filter((m) =>
        m.nombreProducto?.toLowerCase().includes(texto) ||
        m.codigoProducto?.toLowerCase().includes(texto) ||
        m.nombreAlmacen?.toLowerCase().includes(texto) ||
        (m.usuario?.toLowerCase().includes(texto) ?? false) ||
        (m.observacion?.toLowerCase().includes(texto) ?? false)
      );
    }

    if (tipo !== 'TODOS') {
      lista = lista.filter((m) => m.tipo === tipo);
    }

    const campo = this.ordenCampo();
    const dir = this.ordenDireccion();
    const factor = dir === 'asc' ? 1 : -1;
    return [...lista].sort((a, b) => {
      let va: any;
      let vb: any;
      if (campo === 'producto') {
        va = (a.nombreProducto ?? '').toLowerCase();
        vb = (b.nombreProducto ?? '').toLowerCase();
      } else if (campo === 'cantidad') {
        va = Number(a.cantidad ?? 0);
        vb = Number(b.cantidad ?? 0);
      } else {
        va = a.fecha;
        vb = b.fecha;
      }
      if (va < vb) return -1 * factor;
      if (va > vb) return 1 * factor;
      return 0;
    });
  });

  readonly conteos = computed(() => {
    const lista = this.movimientos();
    return {
      todos: lista.length,
      entradas: lista.filter((m) => m.tipo === 'ENTRADA').length,
      salidas: lista.filter((m) => m.tipo === 'SALIDA').length
    };
  });

  ngOnInit(): void {
    this.cargar();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.movimientoViendo()) {
      this.cerrarDetalle();
    }
  }

  cargar(): void {
    this.cargando.set(true);
    this.movimientoService.listar().subscribe({
      next: (data) => {
        this.movimientos.set(data ?? []);
        this.cargando.set(false);
      },
      error: () => {
        this.movimientos.set([]);
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

  cambiarFiltro(tipo: FiltroTipo): void {
    this.filtroTipo.set(tipo);
  }

  ordenarPor(campo: OrdenCampo): void {
    if (this.ordenCampo() === campo) {
      this.ordenDireccion.update((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      this.ordenCampo.set(campo);
      this.ordenDireccion.set(campo === 'fecha' ? 'desc' : 'asc');
    }
  }

  iconoOrden(campo: OrdenCampo): string {
    if (this.ordenCampo() !== campo) return '';
    return this.ordenDireccion() === 'asc' ? '↑' : '↓';
  }

  verDetalle(m: Movimiento): void {
    this.movimientoViendo.set(m);
  }

  cerrarDetalle(): void {
    this.movimientoViendo.set(null);
  }

  trackById(_: number, item: Movimiento): number {
    return item.id;
  }
}