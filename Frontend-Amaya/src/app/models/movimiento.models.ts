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

export interface KardexMovimiento {
  id: number;
  fecha: string;
  tipo: 'ENTRADA' | 'SALIDA';
  entrada: number;
  salida: number;
  saldo: number;
  nombreAlmacen: string;
  referencia: string | null;
  usuario: string | null;
}

export interface Kardex {
  idProducto: number;
  codigoProducto: string;
  nombreProducto: string;
  unidadMedida: string;
  idAlmacen: number | null;
  nombreAlmacen: string;
  desde: string | null;
  hasta: string | null;
  saldoInicial: number;
  totalEntradas: number;
  totalSalidas: number;
  saldoFinal: number;
  movimientos: KardexMovimiento[];
}
