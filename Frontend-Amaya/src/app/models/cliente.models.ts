export type TipoDocumento = 'DNI' | 'RUC';

export interface Cliente {
  id: number;
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  nombres: string | null;
  apellidoPaterno: string | null;
  apellidoMaterno: string | null;
  razonSocial: string | null;
  nombreComercial: string | null;
  direccion: string | null;
  telefono: string | null;
  email: string | null;
  estado: number;
  fechaRegistro: string;
  nombreMostrar: string;
}

export interface ClienteRequest {
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  nombres?: string | null;
  apellidoPaterno?: string | null;
  apellidoMaterno?: string | null;
  razonSocial?: string | null;
  nombreComercial?: string | null;
  direccion?: string | null;
  telefono?: string | null;
  email?: string | null;
}

export interface DniInfo {
  numero: string;
  nombres: string | null;
  apellidoPaterno: string | null;
  apellidoMaterno: string | null;
  nombreCompleto: string | null;
}

export interface RucInfo {
  ruc: string;
  razonSocial: string | null;
  nombreComercial: string | null;
  direccion: string | null;
  estado: string | null;
  condicion: string | null;
}