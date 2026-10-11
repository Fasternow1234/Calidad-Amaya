export interface Proveedor {
  id: number;
  ruc: string;
  razonSocial: string;
  nombreComercial: string | null;
  direccion: string | null;
  telefono: string | null;
  email: string | null;
  estado: number;
  fechaRegistro: string;
}

export interface ProveedorRequest {
  ruc: string;
  razonSocial: string;
  nombreComercial: string | null;
  direccion: string | null;
  telefono: string | null;
  email: string | null;
}

export interface RucInfo {
  ruc: string;
  razonSocial: string;
  nombreComercial: string | null;
  direccion: string | null;
  estado: string | null;
  condicion: string | null;
}