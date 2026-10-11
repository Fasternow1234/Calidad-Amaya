export interface Movimiento {
  id: number;
  idProducto: number;
  codigoProducto: string;
  nombreProducto: string;
  unidadMedida: string;
  idAlmacen: number;
  nombreAlmacen: string;
  tipo: 'ENTRADA' | 'SALIDA';
  cantidad: number;
  stockAnterior: number;
  stockNuevo: number;
  observacion: string | null;
  fecha: string;
  usuario: string | null;
}