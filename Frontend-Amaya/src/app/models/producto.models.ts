export interface Producto {
  id: number;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  precioCompra: number;
  precioVenta: number;
  idCategoria: number;
  categoriaNombre: string;
  unidadMedida: string;
  stockTotal: number;
  idAlmacenDefault: number | null;
  nombreAlmacenDefault: string | null;
  estado: number;
  fechaRegistro: string;
}

export interface ProductoRequest {
  nombre: string;
  descripcion?: string | null;
  precioCompra: number;
  precioVenta: number;
  idCategoria: number;
  unidadMedida: string;
  idAlmacen: number;
}

export interface StockInfo {
  id: number;
  idProducto: number;
  codigoProducto: string;
  nombreProducto: string;
  unidadMedida: string;
  idAlmacen: number;
  nombreAlmacen: string;
  cantidad: number;
  fechaActualizacion: string;
}