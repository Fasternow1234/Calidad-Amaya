export interface Categoria {
  id: number;
  nombre: string;
  estado: number;
  fechaRegistro: string;
}

export interface CategoriaRequest {
  nombre: string;
}