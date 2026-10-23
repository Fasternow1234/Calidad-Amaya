import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AlmacenService } from '../../services/almacen.service';
import { MovimientoService } from '../../services/movimiento.service';
import { ProductoService } from '../../services/producto.service';
import { Almacen } from '../../models/almacen.models';
import { Kardex } from '../../models/movimiento.models';
import { Producto } from '../../models/producto.models';

@Component({
  selector: 'app-kardex',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './kardex.component.html',
  styleUrl: './kardex.component.css'
})
export class KardexComponent implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly almacenService = inject(AlmacenService);
  private readonly movimientoService = inject(MovimientoService);

  readonly productos = signal<Producto[]>([]);
  readonly almacenes = signal<Almacen[]>([]);
  readonly kardex = signal<Kardex | null>(null);
  readonly cargandoCatalogos = signal(true);
  readonly consultando = signal(false);
  readonly error = signal<string | null>(null);

  // Signals para los filtros
  readonly productoSeleccionado = signal<number | null>(null);
  readonly almacenSeleccionado = signal<number>(0);
  readonly desde = signal<string>('');
  readonly hasta = signal<string>('');

  ngOnInit(): void {
    this.productoService.listarActivos().subscribe({
      next: productos => this.productos.set(productos ?? []),
      error: () => {
        this.error.set('No se pudieron cargar los productos.');
        this.cargandoCatalogos.set(false);
      },
      complete: () => this.cargarAlmacenes()
    });
  }

  private cargarAlmacenes(): void {
    this.almacenService.listarActivos().subscribe({
      next: almacenes => this.almacenes.set(almacenes ?? []),
      error: () => {
        this.error.set('No se pudieron cargar los almacenes.');
        this.cargandoCatalogos.set(false);
      },
      complete: () => this.cargandoCatalogos.set(false)
    });
  }

  consultar(): void {
    this.error.set(null);
    this.kardex.set(null);
    const idProd = this.productoSeleccionado();
    
    if (!idProd) {
      this.error.set('Selecciona un producto para consultar su Kardex.');
      return;
    }
    const d = this.desde();
    const h = this.hasta();
    if (d && h && d > h) {
      this.error.set('La fecha inicial no puede ser posterior a la fecha final.');
      return;
    }
    this.consultando.set(true);
    this.movimientoService.consultarKardex(
      idProd,
      this.almacenSeleccionado() > 0 ? this.almacenSeleccionado() : null,
      d,
      h
    ).subscribe({
      next: resultado => this.kardex.set(resultado),
      error: err => this.error.set(err.error?.message ?? 'No se pudo consultar el Kardex.'),
      complete: () => this.consultando.set(false)
    });
  }
}