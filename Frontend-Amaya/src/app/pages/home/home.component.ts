import { Component, inject, signal, computed, HostListener, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { CategoriasComponent } from '../categorias/categorias.component';
import { ProveedoresComponent } from '../proveedores/proveedores.component';
import { ProductosComponent } from '../productos/productos.component';
import { AlmacenesComponent } from '../almacenes/almacenes.component';
import { MovimientosComponent } from '../movimientos/movimientos.component';
import { ComprobantesComponent } from '../comprobantes/comprobantes.component';
import { ClientesComponent } from '../clientes/clientes.component';
import { UsuariosComponent } from '../usuarios/usuarios.component';
import { KardexComponent } from '../kardex/kardex.component';
import { DashboardComponent } from './dashboard.component';

interface MenuItem {
  codigo: string;
  nombre: string;
  orden: number;
  ruta: string;
  icono: string;
}

const MAPA_OPCIONES: Record<string, { ruta: string; icono: string }> = {
  inicio:       { ruta: 'inicio',       icono: '🏠' },
  proveedores:  { ruta: 'proveedores',  icono: '🚚' },
  categorias:   { ruta: 'categorias',   icono: '📁' },
  productos:    { ruta: 'productos',    icono: '📦' },
  almacenes:    { ruta: 'almacenes',    icono: '🏭' },
  movimientos:  { ruta: 'movimientos',  icono: '🔁' },
  comprobantes: { ruta: 'comprobantes', icono: '🧾' },
  clientes:     { ruta: 'clientes',     icono: '👥' },
  usuarios:     { ruta: 'usuarios',     icono: '👤' },
  kardex:        { ruta: 'kardex',        icono: '📊' }
};

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CategoriasComponent,
    ProveedoresComponent,
    ProductosComponent,
    AlmacenesComponent,
    MovimientosComponent,
    ComprobantesComponent,
    ClientesComponent,
    UsuariosComponent,
    KardexComponent,
    DashboardComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private readonly authService = inject(AuthService);

  readonly usuario = this.authService.usuario;
  readonly menuAbierto = signal(false);
  readonly seccionActiva = signal<string>('inicio');
  readonly modoOscuro = signal(false);
  private toggleTimeout: any = null;

  readonly menu = computed<MenuItem[]>(() => {
    const opciones = this.usuario()?.opciones ?? [];
    return opciones
      .map((o) => {
        const meta = MAPA_OPCIONES[o.codigo];
        if (!meta) return null;
        return {
          codigo: o.codigo,
          nombre: o.nombre,
          orden: o.orden,
          ruta: meta.ruta,
          icono: meta.icono
        };
      })
      .filter((item): item is MenuItem => item !== null);
  });

  ngOnInit(): void {
    this.cargarModoOscuro();
  }

  seleccionar(codigo: string): void {
    this.seccionActiva.set(codigo);
  }

  toggleMenu(event: Event): void {
    event.stopPropagation();
    this.menuAbierto.update((v) => !v);
  }

  cerrarSesion(): void {
    this.authService.logout();
  }

  toggleModoOscuro(): void {
    if (this.toggleTimeout) {
      clearTimeout(this.toggleTimeout);
    }
    const nuevo = !this.modoOscuro();
    this.modoOscuro.set(nuevo);
    this.toggleTimeout = setTimeout(() => {
      localStorage.setItem('amaya_modo_oscuro', nuevo ? '1' : '0');
    }, 300);
  }

  private cargarModoOscuro(): void {
    const guardado = localStorage.getItem('amaya_modo_oscuro');
    this.modoOscuro.set(guardado === '1');
  }

  @HostListener('document:click')
  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }
}
