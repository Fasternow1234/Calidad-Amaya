import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Comprobante, ComprobanteRequest } from '../models/comprobante.models';

@Injectable({ providedIn: 'root' })
export class ComprobanteService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/comprobantes`;

  listar(): Observable<Comprobante[]> {
    return this.http.get<Comprobante[]>(this.apiUrl);
  }

  obtener(id: number): Observable<Comprobante> {
    return this.http.get<Comprobante>(`${this.apiUrl}/${id}`);
  }

  crear(request: ComprobanteRequest): Observable<Comprobante> {
    return this.http.post<Comprobante>(this.apiUrl, request);
  }

  anular(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/anular`, {});
  }
}