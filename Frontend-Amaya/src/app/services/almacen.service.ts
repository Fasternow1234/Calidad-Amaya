import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Almacen, AlmacenRequest } from '../models/almacen.models';
import { StockInfo } from '../models/producto.models';

@Injectable({ providedIn: 'root' })
export class AlmacenService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/almacenes`;
  private readonly stockUrl = `${environment.apiUrl}/stock`;

  listarActivos(): Observable<Almacen[]> {
    return this.http.get<Almacen[]>(this.apiUrl);
  }

  listarTodos(): Observable<Almacen[]> {
    return this.http.get<Almacen[]>(`${this.apiUrl}/todos`);
  }

  crear(request: AlmacenRequest): Observable<Almacen> {
    return this.http.post<Almacen>(this.apiUrl, request);
  }

  actualizar(id: number, request: AlmacenRequest): Observable<Almacen> {
    return this.http.put<Almacen>(`${this.apiUrl}/${id}`, request);
  }

  desactivar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  reactivar(id: number): Observable<Almacen> {
    return this.http.patch<Almacen>(`${this.apiUrl}/${id}/reactivar`, {});
  }

  stockPorAlmacen(idAlmacen: number): Observable<StockInfo[]> {
    return this.http.get<StockInfo[]>(`${this.stockUrl}/almacen/${idAlmacen}`);
  }
}