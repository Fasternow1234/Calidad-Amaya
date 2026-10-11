export interface Almacen {
  id: number;
  nombre: string;
  direccion: string | null;
  estado: number;
  fechaRegistro: string;
  totalProductos: number;
}

export interface AlmacenRequest {
  nombre: string;
  direccion?: string | null;
}