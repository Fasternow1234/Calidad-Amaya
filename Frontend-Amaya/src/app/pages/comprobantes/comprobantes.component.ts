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
type FormatoImpresion = 'a4' | 'voucher';

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

  imprimirComprobante(formato: FormatoImpresion): void {
    const comprobante = this.comprobanteViendo();
    if (!comprobante) return;

    const ventana = window.open('', '_blank', 'width=900,height=700');
    if (!ventana) {
      this.mostrarToast('error', 'El navegador bloqueó la ventana de impresión. Permite las ventanas emergentes para este sitio.');
      return;
    }

    ventana.document.open();
    ventana.document.write(this.crearDocumentoImprimible(comprobante, formato));
    ventana.document.close();
  }

  private crearDocumentoImprimible(comprobante: Comprobante, formato: FormatoImpresion): string {
    const voucher = formato === 'voucher';
    const entidades: Record<string, string> = {
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    };
    const escapar = (valor: unknown): string => String(valor ?? '').replace(/[&<>"']/g, caracter => entidades[caracter] ?? caracter);
    const dinero = (valor: number): string => new Intl.NumberFormat('es-PE', {
      minimumFractionDigits: 2, maximumFractionDigits: 2
    }).format(Number(valor ?? 0));
    const fecha = (valor: string, incluirHora = false): string => {
      const coincidencia = valor?.match(/^(\d{4})-(\d{2})-(\d{2})/);
      if (!coincidencia) return escapar(valor);
      const fechaCorta = `${coincidencia[3]}/${coincidencia[2]}/${coincidencia[1]}`;
      const hora = incluirHora ? valor.match(/T(\d{2}:\d{2})/)?.[1] : null;
      return hora ? `${fechaCorta} ${hora}` : fechaCorta;
    };
    const detalles = comprobante.detalles ?? [];
    const filas = detalles.map(detalle => voucher
      ? `<article class="item"><strong>${escapar(detalle.nombreProducto)}</strong><small>${escapar(detalle.codigoProducto || '')}</small><div class="item-linea"><span>${escapar(this.formatearCantidadConUnidad(detalle.cantidad, detalle.unidadMedida))} × S/ ${dinero(detalle.precioUnitario)}</span><b>S/ ${dinero(detalle.subtotal)}</b></div></article>`
      : `<tr><td>${escapar(detalle.nombreProducto)}<small>${escapar(detalle.codigoProducto || '')}</small></td><td>${escapar(detalle.nombreAlmacen)}</td><td class="num">${escapar(this.formatearCantidadConUnidad(detalle.cantidad, detalle.unidadMedida))}</td><td class="num">S/ ${dinero(detalle.precioUnitario)}</td><td class="num">S/ ${dinero(detalle.subtotal)}</td></tr>`
    ).join('');
    const tercero = comprobante.tipo === 'COMPRA'
      ? `<div class="dato"><span>Proveedor</span><strong>${escapar(comprobante.razonSocialProveedor || 'No especificado')}</strong></div><div class="dato"><span>RUC</span><strong>${escapar(comprobante.rucProveedor || '—')}</strong></div>`
      : `<div class="dato"><span>Cliente</span><strong>${escapar(comprobante.nombreCliente || 'No especificado')}</strong></div><div class="dato"><span>Documento</span><strong>${escapar(comprobante.documentoCliente || '—')}</strong></div>`;
    const cuerpoDetalle = voucher ? `<section class="items"><h2>Detalle</h2>${filas || '<p>Sin productos</p>'}</section>` : `<table><thead><tr><th>Producto</th><th>Almacén</th><th>Cantidad</th><th>Precio unitario</th><th>Subtotal</th></tr></thead><tbody>${filas}</tbody></table>`;
    const titulo = comprobante.tipo === 'COMPRA' ? 'Comprobante de compra' : 'Comprobante de venta';
    const observacion = comprobante.observacion ? `<div class="observacion"><span>Observación:</span> ${escapar(comprobante.observacion)}</div>` : '';

    return `<!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>${escapar(titulo)} ${escapar(comprobante.numero)}</title><style>
      *{box-sizing:border-box}body{margin:0;color:#17202a;font-family:Arial,Helvetica,sans-serif;font-size:12px}.hoja{max-width:190mm;margin:0 auto;padding:10mm}.encabezado{text-align:center;padding-bottom:14px;border-bottom:2px solid #25364a}.empresa{font-size:20px;font-weight:800;letter-spacing:.4px}.tipo{margin-top:5px;font-size:14px;font-weight:700;text-transform:uppercase}.numero{margin-top:4px;color:#34495e;font-size:13px;font-weight:700}.datos{display:grid;grid-template-columns:1fr 1fr;gap:0 24px;margin:16px 0}.dato{display:flex;gap:8px;padding:7px 0;border-bottom:1px solid #e2e6ea}.dato span{min-width:100px;color:#566573}.dato strong{overflow-wrap:anywhere}.dato.estado{grid-column:1/-1}table{width:100%;border-collapse:collapse;font-size:11px}th{padding:9px 7px;background:#edf2f7;text-align:left;text-transform:uppercase;font-size:9px;letter-spacing:.3px}td{padding:9px 7px;border-bottom:1px solid #e2e6ea;vertical-align:top}td small{display:block;margin-top:3px;color:#64748b;font-size:9px}.num{text-align:right;white-space:nowrap}.total{display:flex;justify-content:flex-end;gap:30px;padding:14px 7px;border-top:2px solid #25364a;font-size:16px;font-weight:800}.observacion{margin-top:12px;padding:9px;background:#f5f7f9}.pie{margin-top:24px;padding-top:10px;border-top:1px dashed #9aa4ae;text-align:center;color:#566573;font-size:10px}.voucher{width:74mm;margin:0 auto;padding:3mm;font-size:10px}.voucher .empresa{font-size:16px}.voucher .tipo{font-size:12px}.voucher .numero{font-size:11px}.voucher .datos{display:block;margin:10px 0}.voucher .dato{padding:4px 0;gap:5px;font-size:9px}.voucher .dato span{min-width:68px}.voucher .items h2{margin:12px 0 7px;padding-bottom:5px;border-bottom:1px dashed #566573;font-size:11px;text-transform:uppercase}.item{padding:7px 0;border-bottom:1px dashed #c3c9ce}.item strong{display:block;font-size:10px}.item small{display:block;margin-top:2px;color:#566573;font-size:8px}.item-linea{display:flex;justify-content:space-between;gap:5px;margin-top:5px;font-size:9px}.voucher .total{gap:12px;padding:10px 0;font-size:13px}.voucher .observacion{padding:6px;font-size:9px}.voucher .pie{margin-top:15px;font-size:9px}
      @page{size:A4;margin:8mm}@page voucher{size:80mm 297mm;margin:3mm}@media print{.hoja{padding:0}.voucher{page:voucher;width:74mm;padding:0}thead{display:table-header-group}tr,.item{break-inside:avoid}}
    </style></head><body><main class="hoja${voucher ? ' voucher' : ''}"><header class="encabezado"><div class="empresa">DISTRIBUIDORA AMAYA</div><div class="tipo">${escapar(titulo)}</div><div class="numero">${escapar(comprobante.numero)}</div></header><section class="datos"><div class="dato"><span>Fecha de emisión</span><strong>${fecha(comprobante.fechaEmision)}</strong></div><div class="dato"><span>Estado</span><strong>${comprobante.estado === 1 ? 'ACTIVO' : 'ANULADO'}</strong></div>${tercero}<div class="dato"><span>Registrado por</span><strong>${escapar(comprobante.usuario || '—')}</strong></div><div class="dato"><span>Fecha de registro</span><strong>${fecha(comprobante.fechaRegistro, true)}</strong></div></section>${cuerpoDetalle}<div class="total"><span>TOTAL</span><span>S/ ${dinero(comprobante.total)}</span></div>${observacion}<footer class="pie">Documento de control interno · Gracias</footer></main><script>window.onload=()=>setTimeout(()=>window.print(),250);window.onafterprint=()=>window.close();</script></body></html>`;
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
