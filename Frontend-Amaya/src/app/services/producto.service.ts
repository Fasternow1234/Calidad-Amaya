import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Producto, ProductoRequest, StockInfo } from '../models/producto.models';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/productos`;
  private readonly stockUrl = `${environment.apiUrl}/stock`;

  listarActivos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl);
  }

  listarTodos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/todos`);
  }

  buscar(q: string): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${this.apiUrl}/buscar?q=${encodeURIComponent(q)}`);
  }

  crear(request: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, request);
  }

  actualizar(id: number, request: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.apiUrl}/${id}`, request);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  reactivar(id: number): Observable<Producto> {
    return this.http.patch<Producto>(`${this.apiUrl}/${id}/reactivar`, {});
  }

  stockPorProducto(idProducto: number): Observable<StockInfo[]> {
    return this.http.get<StockInfo[]>(`${this.stockUrl}/producto/${idProducto}`);
  }
}