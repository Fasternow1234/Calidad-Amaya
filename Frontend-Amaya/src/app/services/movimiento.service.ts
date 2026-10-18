import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Kardex, Movimiento } from '../models/movimiento.models';

@Injectable({ providedIn: 'root' })
export class MovimientoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/movimientos`;

  listar(): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(this.apiUrl);
  }

  consultarKardex(idProducto: number, idAlmacen: number | null, desde: string, hasta: string): Observable<Kardex> {
    const params = new URLSearchParams();
    if (idAlmacen !== null) params.set('idAlmacen', String(idAlmacen));
    if (desde) params.set('desde', desde);
    if (hasta) params.set('hasta', hasta);
    const query = params.toString();
    return this.http.get<Kardex>(`${this.apiUrl}/kardex/${idProducto}${query ? `?${query}` : ''}`);
  }
}
