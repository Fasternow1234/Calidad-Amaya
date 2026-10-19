import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { CategoriaService } from '../services/categoria.service';
import { AlmacenesComponent } from './almacenes/almacenes.component';
import { CategoriasComponent } from './categorias/categorias.component';
import { NuevoComprobanteComponent } from './comprobantes/nuevo-comprobante.component';
import { ProductosComponent } from './productos/productos.component';
import { ProveedoresComponent } from './proveedores/proveedores.component';

describe('Contadores reactivos de formularios', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AlmacenesComponent,
        CategoriasComponent,
        NuevoComprobanteComponent,
        ProductosComponent,
        ProveedoresComponent
      ],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        {
          provide: CategoriaService,
          useValue: {
            listarActivas: () => of([]),
            listarTodas: () => of([])
          }
        }
      ]
    }).compileComponents();
  });

  it('actualiza en pantalla el contador de categorías al escribir', async () => {
    const fixture = TestBed.createComponent(CategoriasComponent);
    fixture.detectChanges();
    fixture.componentInstance.abrirFormularioNuevo();
    await fixture.whenStable();

    const input = fixture.nativeElement.querySelector('#nombre') as HTMLInputElement;
    input.value = 'Bebidas';
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    const contador = fixture.nativeElement.querySelector('.contador') as HTMLElement;

    expect(contador.textContent?.trim()).toBe('7/100');
  });

  it('actualiza los contadores de productos', () => {
    const component = TestBed.createComponent(ProductosComponent).componentInstance;

    component.form.controls.nombre.setValue('Arroz');
    component.form.controls.descripcion.setValue('Bolsa de un kilo');

    expect(component.nombreLargo()).toBe(5);
    expect(component.descripcionLargo()).toBe(16);
  });

  it('actualiza los contadores de proveedores', () => {
    const component = TestBed.createComponent(ProveedoresComponent).componentInstance;

    component.form.controls.ruc.setValue('20123456789');
    component.form.controls.razonSocial.setValue('Amaya');
    component.form.controls.nombreComercial.setValue('Distribuidora');
    component.form.controls.direccion.setValue('Lima');
    component.form.controls.telefono.setValue('987654321');
    component.form.controls.email.setValue('ventas@amaya.pe');

    expect(component.rucLargo()).toBe(11);
    expect(component.razonLargo()).toBe(5);
    expect(component.nombreComercialLargo()).toBe(13);
    expect(component.direccionLargo()).toBe(4);
    expect(component.telefonoLargo()).toBe(9);
    expect(component.emailLargo()).toBe(15);
  });

  it('actualiza los contadores de almacenes', () => {
    const component = TestBed.createComponent(AlmacenesComponent).componentInstance;

    component.form.controls.nombre.setValue('Central');
    component.form.controls.direccion.setValue('Lima');

    expect(component.nombreLargo()).toBe(7);
    expect(component.direccionLargo()).toBe(4);
  });

  it('actualiza el contador de observación del comprobante', () => {
    const component = TestBed.createComponent(NuevoComprobanteComponent).componentInstance;

    component.form.controls.observacion.setValue('Venta de prueba');

    expect(component.observacionLargo()).toBe(15);
  });
});
