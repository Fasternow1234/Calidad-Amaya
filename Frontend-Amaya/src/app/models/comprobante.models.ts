export interface DetalleComprobante {
  id: number;
  idProducto: number;
  codigoProducto: string;
  nombreProducto: string;
  unidadMedida: string;
  idAlmacen: number;
  nombreAlmacen: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Comprobante {
  id: number;
  tipo: 'COMPRA' | 'VENTA';
  numero: string;
  fechaEmision: string;
  idProveedor: number | null;
  razonSocialProveedor: string | null;
  rucProveedor: string | null;
  idCliente: number | null;
  nombreCliente: string | null;
  documentoCliente: string | null;
  observacion: string | null;
  total: number;
  estado: number;
  usuario: string | null;
  fechaRegistro: string;
  detalles: DetalleComprobante[] | null;
}

export interface DetalleComprobanteRequest {
  idProducto: number;
  idAlmacen: number | null;
  cantidad: number;
  precioUnitario: number;
}

export interface ComprobanteRequest {
  tipo: 'COMPRA' | 'VENTA';
  numero: string;
  fechaEmision: string;
  idProveedor: number | null;
  idCliente: number | null;
  observacion: string | null;
  detalles: DetalleComprobanteRequest[];
}
