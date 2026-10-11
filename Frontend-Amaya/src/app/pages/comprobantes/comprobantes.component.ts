import { Component, inject, signal, computed, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ComprobanteService } from '../../services/comprobante.service';
import { Comprobante } from '../../models/comprobante.models';
import { NuevoComprobanteComponent } from './nuevo-comprobante.component';

type ToastTipo = 'success' | 'error';
type FiltroTipo = 'TODOS' | 'COMPRA' | 'VENTA';
type OrdenCampo = 'fecha' | 'numero' | 'total';
type OrdenDireccion = 'asc' | 'desc';

interface Toast {
  tipo: ToastTipo;
  mensaje: string;
  accionLabel?: string;
  accionFn?: () => void;
}

@Component({
  selector: 'app-comprobantes',
  standalone: true,
  imports: [CommonModule, FormsModule, NuevoComprobanteComponent],
  templateUrl: './comprobantes.component.html',
  styleUrl: './comprobantes.component.css'
})
export class ComprobantesComponent implements OnInit {
  private readonly comprobanteService = inject(ComprobanteService);

  readonly comprobantes = signal<Comprobante[]>([]);
  readonly cargando = signal(false);
  readonly busqueda = signal('');
  readonly filtroTipo = signal<FiltroTipo>('TODOS');
  readonly mostrarAnulados = signal(false);
  readonly formVisible = signal(false);
  readonly comprobanteViendo = signal<Comprobante | null>(null);
  readonly comprobanteAnulando = signal<Comprobante | null>(null);
  readonly procesando = signal(false);
  readonly toast = signal<Toast | null>(null);
  readonly ordenCampo = signal<OrdenCampo>('fecha');
  readonly ordenDireccion = signal<OrdenDireccion>('desc');

  private toastTimeout: any = null;
  private toastAccionTimeout: any = null;

  readonly conteos = computed(() => {
    const lista = this.comprobantes();
    return {
      todos: lista.length,
      compras: lista.filter(c => c.tipo === 'COMPRA').length,
      ventas: lista.filter(c => c.tipo === 'VENTA').length
    };
  });

  readonly comprobantesFiltrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const tipo = this.filtroTipo();
    const verAnulados = this.mostrarAnulados();
    let lista = this.comprobantes();

    if (!verAnulados) {
      lista = lista.filter(c => c.estado === 1);
    }

    if (texto) {
      lista = lista.filter((c) =>
        c.numero.toLowerCase().includes(texto) ||
        (c.razonSocialProveedor?.toLowerCase().includes(texto) ?? false) ||
        (c.rucProveedor?.toLowerCase().includes(texto) ?? false) ||
        (c.observacion?.toLowerCase().includes(texto) ?? false) ||
        (c.usuario?.toLowerCase().includes(texto) ?? false)
      );
    }

    if (tipo !== 'TODOS') {
      lista = lista.filter(c => c.tipo === tipo);
    }

    const campo = this.ordenCampo();
    const dir = this.ordenDireccion();
    const factor = dir === 'asc' ? 1 : -1;

    return [...lista].sort((a, b) => {
      let va: any;
      let vb: any;
      if (campo === 'numero') {
        va = a.numero;
        vb = b.numero;
      } else if (campo === 'total') {
        va = Number(a.total ?? 0);
        vb = Number(b.total ?? 0);
      } else {
        va = a.fechaEmision;
        vb = b.fechaEmision;
      }
      if (va < vb) return -1 * factor;
      if (va > vb) return 1 * factor;
      return 0;
    });
  });

  ngOnInit(): void {
    this.cargar();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.comprobanteAnulando()) {
      this.cancelarAnulacion();
      return;
    }
    if (this.comprobanteViendo()) {
      this.cerrarDetalle();
      return;
    }
    if (this.formVisible()) {
      this.formVisible.set(false);
    }
  }

  cargar(): void {
    this.cargando.set(true);
    this.comprobanteService.listar().subscribe({
      next: (data) => {
        this.comprobantes.set(data ?? []);
        this.cargando.set(false);
      },
      error: () => {
        this.mostrarToast('error', 'Error al cargar comprobantes');
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

  toggleAnulados(): void {
    this.mostrarAnulados.update(v => !v);
  }

  ordenarPor(campo: OrdenCampo): void {
    if (this.ordenCampo() === campo) {
      this.ordenDireccion.update(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      this.ordenCampo.set(campo);
      this.ordenDireccion.set(campo === 'fecha' ? 'desc' : 'asc');
    }
  }

  iconoOrden(campo: OrdenCampo): string {
    if (this.ordenCampo() !== campo) return '';
    return this.ordenDireccion() === 'asc' ? '↑' : '↓';
  }

  verDetalle(c: Comprobante): void {
    this.comprobanteService.obtener(c.id).subscribe({
      next: (full) => this.comprobanteViendo.set(full),
      error: () => this.mostrarToast('error', 'No se pudo cargar el detalle')
    });
  }

  cerrarDetalle(): void {
    this.comprobanteViendo.set(null);
  }

  pedirAnular(c: Comprobante): void {
    if (c.estado === 0) return;
    this.comprobanteAnulando.set(c);
  }

  cancelarAnulacion(): void {
    this.comprobanteAnulando.set(null);
  }

  confirmarAnulacion(): void {
    const c = this.comprobanteAnulando();
    if (!c) return;
    this.procesando.set(true);
    this.comprobanteService.anular(c.id).subscribe({
      next: () => {
        this.procesando.set(false);
        this.comprobanteAnulando.set(null);
        this.cargar();
        this.mostrarToast('success', `Comprobante ${c.numero} anulado`);
      },
      error: (err) => {
        this.procesando.set(false);
        this.comprobanteAnulando.set(null);
        this.mostrarToast('error', err.error?.message ?? 'Error al anular');
      }
    });
  }

  onGuardado(): void {
    this.formVisible.set(false);
    this.cargar();
    this.mostrarToast('success', 'Comprobante registrado correctamente');
  }

  formatearCantidadConUnidad(cantidad: number | null | undefined, unidadMedida: string | null | undefined): string {
    const num = Number(cantidad ?? 0);
    if (!isFinite(num)) return '0';
    const limpio = num.toString();
    if (!unidadMedida) return limpio;
    if (num === 1) return `${limpio} ${unidadMedida.toLowerCase().trim()}`;
    const plurales: Record<string, string> = {
      unidad: 'unidades',
      kilo: 'kilos',
      litro: 'litros',
      gramo: 'gramos'
    };
    const u = unidadMedida.toLowerCase().trim();
    return `${limpio} ${plurales[u] ?? u}`;
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
}